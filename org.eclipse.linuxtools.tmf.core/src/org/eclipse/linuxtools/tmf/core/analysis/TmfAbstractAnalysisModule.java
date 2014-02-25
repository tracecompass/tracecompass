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

package org.eclipse.linuxtools.tmf.core.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.component.TmfComponent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfStartAnalysisSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.osgi.util.NLS;

/**
 * Base class that analysis modules main class may extend. It provides default
 * behavior to some methods of the analysis module
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public abstract class TmfAbstractAnalysisModule extends TmfComponent implements IAnalysisModule {

    private String fName, fId;
    private boolean fAutomatic = false, fStarted = false;
    private ITmfTrace fTrace;
    private final Map<String, Object> fParameters = new HashMap<>();
    private final List<String> fParameterNames = new ArrayList<>();
    private final List<IAnalysisOutput> fOutputs = new ArrayList<>();
    private List<IAnalysisParameterProvider> fParameterProviders = new ArrayList<>();
    private Job fJob = null;

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
        return fName;
    }

    @Override
    public void setName(String name) {
        fName = name;
    }

    @Override
    public void setId(String id) {
        fId = id;
    }

    @Override
    public String getId() {
        return fId;
    }

    @Override
    public void setAutomatic(boolean auto) {
        fAutomatic = auto;
    }

    @Override
    public void setTrace(ITmfTrace trace) throws TmfAnalysisException {
        if (fTrace != null) {
            throw new TmfAnalysisException(NLS.bind(Messages.TmfAbstractAnalysisModule_TraceSetMoreThanOnce, getName()));
        }

        /* Check that analysis can be executed */
        if (!canExecute(trace)) {
            throw new TmfAnalysisException(NLS.bind(Messages.TmfAbstractAnalysisModule_AnalysisCannotExecute, getName()));
        }
        fTrace = trace;
        /* Get the parameter providers for this trace */
        fParameterProviders = TmfAnalysisManager.getParameterProviders(this, fTrace);
        for (IAnalysisParameterProvider provider : fParameterProviders) {
            provider.registerModule(this);
        }
        resetAnalysis();
        fStarted = false;
    }

    /**
     * Gets the trace
     *
     * @return The trace
     */
    protected ITmfTrace getTrace() {
        return fTrace;
    }

    @Override
    public void addParameter(String name) {
        fParameterNames.add(name);
    }

    @Override
    public synchronized void setParameter(String name, Object value) {
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
    public Object getParameter(String name) {
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
        return true;
    }

    /**
     * Set the countdown latch back to 1 so the analysis can be executed again
     */
    protected void resetAnalysis() {
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
        fStarted = false;
        fJob = null;
        fFinishedLatch.countDown();
        if (fTrace instanceof TmfTrace) {
            ((TmfTrace) fTrace).refreshSupplementaryFiles();
        }
    }

    /**
     * Cancels the analysis if it is executing
     */
    @Override
    public final void cancel() {
        synchronized (syncObj) {
            if (fJob != null) {
                if (fJob.cancel()) {
                    fAnalysisCancelled = true;
                    setAnalysisCompleted();
                }
            }
            fStarted = false;
        }
    }

    private void execute() {

        /*
         * TODO: The analysis in a job should be done at the analysis manager
         * level instead of depending on this abstract class implementation,
         * otherwise another analysis implementation may block the main thread
         */

        /* Do not execute if analysis has already run */
        if (fFinishedLatch.getCount() == 0) {
            return;
        }

        /* Do not execute if analysis already running */
        synchronized (syncObj) {
            if (fStarted) {
                return;
            }
            fStarted = true;
        }

        /*
         * Actual analysis will be run on a separate thread
         */
        fJob = new Job(NLS.bind(Messages.TmfAbstractAnalysisModule_RunningAnalysis, getName())) {
            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                try {
                    monitor.beginTask("", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                    broadcast(new TmfStartAnalysisSignal(TmfAbstractAnalysisModule.this, TmfAbstractAnalysisModule.this));
                    fAnalysisCancelled = !executeAnalysis(monitor);
                } catch (TmfAnalysisException e) {
                    Activator.logError("Error executing analysis with trace " + getTrace().getName(), e); //$NON-NLS-1$
                } finally {
                    synchronized (syncObj) {
                        monitor.done();
                        setAnalysisCompleted();
                    }
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
                TmfAbstractAnalysisModule.this.canceling();
            }

        };
        fJob.schedule();
    }

    @Override
    public IStatus schedule() {
        if (fTrace == null) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, String.format("No trace specified for analysis %s", getName())); //$NON-NLS-1$
        }
        execute();

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
        if (signal.getTrace() == fTrace) {
            cancel();
            fTrace = null;
        }
    }

    /**
     * Returns a full help text to display
     *
     * @return Full help text for the module
     */
    protected String getFullHelpText() {
        return NLS.bind(Messages.TmfAbstractAnalysisModule_AnalysisModule, getName());
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
        return NLS.bind(Messages.TmfAbstractAnalysisModule_AnalysisForTrace, getName(), trace.getName());
    }

    /**
     * Gets the help text specific for a trace who does not have required
     * characteristics for module to execute
     *
     * @param trace
     *            The trace to apply the analysis to
     * @return Help text
     */
    protected String getTraceCannotExecuteHelpText(ITmfTrace trace) {
        return Messages.TmfAbstractAnalysisModule_AnalysisCannotExecute;
    }

    @Override
    public String getHelpText() {
        return getFullHelpText();
    }

    @Override
    public String getHelpText(ITmfTrace trace) {
        if (trace == null) {
            return getHelpText();
        }
        String text = getShortHelpText(trace);
        if (!canExecute(trace)) {
            text = text + getTraceCannotExecuteHelpText(trace);
        }
        return text;
    }

}
