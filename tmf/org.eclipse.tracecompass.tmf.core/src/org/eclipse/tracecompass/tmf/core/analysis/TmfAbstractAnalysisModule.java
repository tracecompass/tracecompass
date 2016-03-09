/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.internal.tmf.core.TmfCoreTracer;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisRequirement.ValuePriorityLevel;
import org.eclipse.tracecompass.tmf.core.component.TmfComponent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfStartAnalysisSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;

/**
 * Base class that analysis modules main class may extend. It provides default
 * behavior to some methods of the analysis module
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public abstract class TmfAbstractAnalysisModule extends TmfComponent implements IAnalysisModule {

    private @Nullable String fId;
    private boolean fAutomatic = false, fStarted = false;
    private volatile @Nullable ITmfTrace fTrace;
    private final Map<String, @Nullable Object> fParameters = new HashMap<>();
    private final List<String> fParameterNames = new ArrayList<>();
    private final List<IAnalysisOutput> fOutputs = new ArrayList<>();
    private Set<IAnalysisParameterProvider> fParameterProviders = new HashSet<>();
    private @Nullable Job fJob = null;

    private final Object syncObj = new Object();

    /* Latch tracking if the analysis is completed or not */
    private CountDownLatch fFinishedLatch = new CountDownLatch(0);

    private boolean fAnalysisCancelled = false;

    @Override
    public boolean isAutomatic() {
        return fAutomatic;
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    @Override
    public void setId(String id) {
        fId = id;
    }

    @Override
    public String getId() {
        String id = fId;
        if (id == null) {
            id = this.getClass().getCanonicalName();
            if (id == null) {
                /*
                 * Some types, like anonymous classes, don't have a canonical
                 * name. Just use the default name instead.
                 */
                id = this.getClass().getName();
            }
            fId = id;
        }
        return id;
    }

    @Override
    public void setAutomatic(boolean auto) {
        fAutomatic = auto;
    }

    /**
     * @since 1.0
     */
    @Override
    public boolean setTrace(ITmfTrace trace) throws TmfAnalysisException {
        if (fTrace != null) {
            throw new TmfAnalysisException(NLS.bind(Messages.TmfAbstractAnalysisModule_TraceSetMoreThanOnce, getName()));
        }

        TmfCoreTracer.traceAnalysis(getId(), trace, "setting trace for analysis"); //$NON-NLS-1$

        /* Check that analysis can be executed */
        fTrace = trace;
        if (!canExecute(trace)) {
            fTrace = null;
            return false;
        }

        /* Get the parameter providers for this trace */
        fParameterProviders = TmfAnalysisManager.getParameterProvidersForModule(this, trace);
        for (IAnalysisParameterProvider provider : fParameterProviders) {
            TmfCoreTracer.traceAnalysis(getId(), trace, "registered to parameter provider " + provider.getName()); //$NON-NLS-1$
            provider.registerModule(this);
        }
        resetAnalysis();
        fStarted = false;
        return true;
    }

    /**
     * Gets the trace
     *
     * @return The trace
     */
    protected @Nullable ITmfTrace getTrace() {
        return fTrace;
    }

    @Override
    public void addParameter(String name) {
        fParameterNames.add(name);
    }

    @Override
    public synchronized void setParameter(String name, @Nullable Object value) {
        if (!fParameterNames.contains(name)) {
            throw new RuntimeException(NLS.bind(Messages.TmfAbstractAnalysisModule_InvalidParameter, name, getName()));
        }
        Object oldValue = fParameters.get(name);
        fParameters.put(name, value);
        if ((value != null) && !(value.equals(oldValue))) {
            parameterChanged(name);
        }
    }

    @Override
    public synchronized void notifyParameterChanged(String name) {
        if (!fParameterNames.contains(name)) {
            throw new RuntimeException(NLS.bind(Messages.TmfAbstractAnalysisModule_InvalidParameter, name, getName()));
        }
        Object oldValue = fParameters.get(name);
        Object value = getParameter(name);
        if ((value != null) && !(value.equals(oldValue))) {
            parameterChanged(name);
        }
    }

    /**
     * Used to indicate that a parameter value has been changed
     *
     * @param name
     *            The name of the modified parameter
     */
    protected void parameterChanged(String name) {

    }

    @Override
    public @Nullable Object getParameter(String name) {
        Object paramValue = fParameters.get(name);
        /* The parameter is not set, maybe it can be provided by someone else */
        if ((paramValue == null) && (fTrace != null)) {
            for (IAnalysisParameterProvider provider : fParameterProviders) {
                paramValue = provider.getParameter(name);
                if (paramValue != null) {
                    break;
                }
            }
        }
        return paramValue;
    }

    @Override
    public boolean canExecute(ITmfTrace trace) {
        for (TmfAnalysisRequirement requirement : getAnalysisRequirements()) {
            if (!requirement.isFulfilled(trace)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Set the countdown latch back to 1 so the analysis can be executed again
     */
    protected void resetAnalysis() {
        TmfCoreTracer.traceAnalysis(getId(), getTrace(), "reset: ready for execution"); //$NON-NLS-1$
        fFinishedLatch.countDown();
        fFinishedLatch = new CountDownLatch(1);
    }

    /**
     * Actually executes the analysis itself
     *
     * @param monitor
     *            Progress monitor
     * @return Whether the analysis was completed successfully or not
     * @throws TmfAnalysisException
     *             Method may throw an analysis exception
     */
    protected abstract boolean executeAnalysis(final IProgressMonitor monitor) throws TmfAnalysisException;

    /**
     * Indicate the analysis has been canceled. It is abstract to force
     * implementing class to cleanup what they are running. This is called by
     * the job's canceling. It does not need to be called directly.
     */
    protected abstract void canceling();

    /**
     * To be called when the analysis is completed, whether normally or because
     * it was cancelled or for any other reason.
     *
     * It has to be called inside a synchronized block
     */
    private void setAnalysisCompleted() {
        synchronized (syncObj) {
            fStarted = false;
            fJob = null;
            fFinishedLatch.countDown();
        }
    }

    /**
     * Cancels the analysis if it is executing
     */
    @Override
    public final void cancel() {
        synchronized (syncObj) {
            TmfCoreTracer.traceAnalysis(getId(), getTrace(), "cancelled by application"); //$NON-NLS-1$
            if (fJob != null && fJob.cancel()) {
                fAnalysisCancelled = true;
                setAnalysisCompleted();
            }
            fStarted = false;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        cancel();
    }

    /**
     * Get an iterable list of all analyzes this analysis depends on. These
     * analyzes will be scheduled before this analysis starts and the current
     * analysis will not be considered completed until all the dependent
     * analyzes are finished.
     *
     * @return An iterable list of analysis this analyzes depends on.
     */
    protected Iterable<IAnalysisModule> getDependentAnalyses() {
        return Collections.EMPTY_LIST;
    }

    private void execute(final ITmfTrace trace) {
        /*
         * TODO: The analysis in a job should be done at the analysis manager
         * level instead of depending on this abstract class implementation,
         * otherwise another analysis implementation may block the main thread
         */

        /* Do not execute if analysis has already run */
        if (fFinishedLatch.getCount() == 0) {
            TmfCoreTracer.traceAnalysis(getId(), getTrace(), "already executed"); //$NON-NLS-1$
            return;
        }

        /* Do not execute if analysis already running */
        synchronized (syncObj) {
            if (fStarted) {
                TmfCoreTracer.traceAnalysis(getId(), getTrace(), "already started, not starting again"); //$NON-NLS-1$
                return;
            }
            fStarted = true;
        }

        /* Execute dependent analyses before creating the job for this one */
        final Iterable<IAnalysisModule> dependentAnalyses = getDependentAnalyses();
        for (IAnalysisModule module : dependentAnalyses) {
            module.schedule();
        }

        /*
         * Actual analysis will be run on a separate thread
         */
        String jobName = checkNotNull(NLS.bind(Messages.TmfAbstractAnalysisModule_RunningAnalysis, getName()));
        fJob = new Job(jobName) {
            @Override
            protected @Nullable IStatus run(final @Nullable IProgressMonitor monitor) {
                IProgressMonitor mon = monitor;
                if (mon == null) {
                    mon = new NullProgressMonitor();
                }
                try {
                    mon.beginTask("", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                    broadcast(new TmfStartAnalysisSignal(TmfAbstractAnalysisModule.this, TmfAbstractAnalysisModule.this));
                    TmfCoreTracer.traceAnalysis(TmfAbstractAnalysisModule.this.getId(), TmfAbstractAnalysisModule.this.getTrace(), "started"); //$NON-NLS-1$
                    fAnalysisCancelled = !executeAnalysis(mon);
                    for (IAnalysisModule module : dependentAnalyses) {
                        module.waitForCompletion(mon);
                    }
                    TmfCoreTracer.traceAnalysis(TmfAbstractAnalysisModule.this.getId(), TmfAbstractAnalysisModule.this.getTrace(), "finished"); //$NON-NLS-1$
                } catch (TmfAnalysisException e) {
                    Activator.logError("Error executing analysis with trace " + trace.getName(), e); //$NON-NLS-1$
                } finally {
                    synchronized (syncObj) {
                        mon.done();
                        setAnalysisCompleted();
                    }
                    TmfTraceManager.refreshSupplementaryFiles(trace);
                }
                if (!fAnalysisCancelled) {
                    return Status.OK_STATUS;
                }
                // Reset analysis so that it can be executed again.
                resetAnalysis();
                return Status.CANCEL_STATUS;
            }

            @Override
            protected void canceling() {
                TmfCoreTracer.traceAnalysis(getId(), getTrace(), "job cancelled"); //$NON-NLS-1$
                TmfAbstractAnalysisModule.this.canceling();
            }

        };
        fJob.schedule();
    }

    @Override
    public IStatus schedule() {
        synchronized (syncObj) {
            final ITmfTrace trace = getTrace();
            if (trace == null) {
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, String.format("No trace specified for analysis %s", getName())); //$NON-NLS-1$
            }
            TmfCoreTracer.traceAnalysis(getId(), getTrace(), "scheduled"); //$NON-NLS-1$
            execute(trace);
        }

        return Status.OK_STATUS;
    }

    @Override
    public Iterable<IAnalysisOutput> getOutputs() {
        return fOutputs;
    }

    @Override
    public void registerOutput(IAnalysisOutput output) {
        if (!fOutputs.contains(output)) {
            fOutputs.add(output);
        }
    }

    @Override
    public boolean waitForCompletion() {
        try {
            fFinishedLatch.await();
        } catch (InterruptedException e) {
            Activator.logError("Error while waiting for module completion", e); //$NON-NLS-1$
        }
        return !fAnalysisCancelled;
    }

    @Override
    public boolean waitForCompletion(IProgressMonitor monitor) {
        try {
            while (!fFinishedLatch.await(500, TimeUnit.MILLISECONDS)) {
                if (fAnalysisCancelled || monitor.isCanceled()) {
                    fAnalysisCancelled = true;
                    return false;
                }
            }
        } catch (InterruptedException e) {
            Activator.logError("Error while waiting for module completion", e); //$NON-NLS-1$
        }
        return !fAnalysisCancelled;
    }

    /**
     * Signal handler for trace closing
     *
     * @param signal
     *            Trace closed signal
     */
    @TmfSignalHandler
    public void traceClosed(TmfTraceClosedSignal signal) {
        /* Is the closing trace the one that was requested? */
        synchronized (syncObj) {
            if (signal.getTrace() == fTrace) {
                cancel();
                fTrace = null;
            }
        }
    }

    /**
     * Signal handler for when the trace becomes active
     *
     * @param signal
     *            Trace selected signal
     */
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        /*
         * Since some parameter providers may handle many traces, we need to
         * register the current trace to it
         */
        if (signal.getTrace() == fTrace) {
            for (IAnalysisParameterProvider provider : fParameterProviders) {
                provider.registerModule(this);
            }
        }
    }

    /**
     * Returns a full help text to display
     *
     * @return Full help text for the module
     */
    protected String getFullHelpText() {
        return NonNullUtils.nullToEmptyString(NLS.bind(
                Messages.TmfAbstractAnalysisModule_AnalysisModule,
                getName()));
    }

    /**
     * Gets a short help text, to display as header to other help text
     *
     * @param trace
     *            The trace to show help for
     *
     * @return Short help text describing the module
     */
    protected String getShortHelpText(ITmfTrace trace) {
        return NonNullUtils.nullToEmptyString(NLS.bind(
                Messages.TmfAbstractAnalysisModule_AnalysisForTrace,
                getName(), trace.getName()));
    }

    /**
     * Gets the help text specific for a trace who does not have required
     * characteristics for module to execute. The default implementation uses
     * the analysis requirements.
     *
     * @param trace
     *            The trace to apply the analysis to
     * @return Help text
     */
    protected String getTraceCannotExecuteHelpText(ITmfTrace trace) {
        StringBuilder builder = new StringBuilder();
        builder.append(NLS.bind(Messages.TmfAbstractAnalysisModule_AnalysisCannotExecute, getName()));
        for (TmfAnalysisRequirement requirement : getAnalysisRequirements()) {
            if (!requirement.isFulfilled(trace)) {
                builder.append("\n\n"); //$NON-NLS-1$
                builder.append(NLS.bind(Messages.TmfAnalysis_RequirementNotFulfilled, requirement.getType()));
                builder.append("\n"); //$NON-NLS-1$
                builder.append(NLS.bind(Messages.TmfAnalysis_RequirementMandatoryValues, requirement.getValues(ValuePriorityLevel.MANDATORY)));
                Set<String> information = requirement.getInformation();
                if (!information.isEmpty()) {
                    builder.append("\n"); //$NON-NLS-1$
                    builder.append(NLS.bind(Messages.TmfAnalysis_RequirementInformation, information));
                }
            }
        }
        return builder.toString();
    }

    @Override
    public String getHelpText() {
        return getFullHelpText();
    }

    @Override
    public String getHelpText(ITmfTrace trace) {
        String text = getShortHelpText(trace);
        if (!canExecute(trace)) {
            text = text + "\n\n" + getTraceCannotExecuteHelpText(trace); //$NON-NLS-1$
        }
        return text;
    }

    @Override
    public Iterable<TmfAnalysisRequirement> getAnalysisRequirements() {
        return Collections.EMPTY_SET;
    }
}
