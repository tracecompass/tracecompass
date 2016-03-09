/*******************************************************************************
 * Copyright (c) 2013, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Bernd Hufmann - Integrated history builder functionality
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.statesystem;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.core.statesystem.backends.partial.PartialHistoryBackend;
import org.eclipse.tracecompass.internal.tmf.core.statesystem.backends.partial.PartialStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.StateSystemFactory;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.backend.StateHistoryBackendFactory;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.project.model.ITmfPropertiesProvider;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTraceCompleteness;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

/**
 * Abstract analysis module to generate a state system. It is a base class that
 * can be used as a shortcut by analysis who just need to build a single state
 * system with a state provider.
 *
 * Analysis implementing this class should only need to provide a state system
 * and optionally a backend (default to NULL) and, if required, a filename
 * (defaults to the analysis'ID)
 *
 * @author Geneviève Bastien
 */
public abstract class TmfStateSystemAnalysisModule extends TmfAbstractAnalysisModule
        implements ITmfAnalysisModuleWithStateSystems, ITmfPropertiesProvider {

    private static final String EXTENSION = ".ht"; //$NON-NLS-1$

    private final CountDownLatch fInitialized = new CountDownLatch(1);
    private final Object fRequestSyncObj = new Object();

    @Nullable private ITmfStateSystemBuilder fStateSystem;
    @Nullable private ITmfStateProvider fStateProvider;
    @Nullable private IStateHistoryBackend fHtBackend;
    @Nullable private ITmfEventRequest fRequest;
    @Nullable private TmfTimeRange fTimeRange = null;

    private int fNbRead = 0;
    private boolean fInitializationSucceeded;

    /**
     * State system backend types
     *
     * @author Geneviève Bastien
     */
    protected enum StateSystemBackendType {
        /** Full history in file */
        FULL,
        /** In memory state system */
        INMEM,
        /** Null history */
        NULL,
        /** State system backed with partial history */
        PARTIAL
    }

    /**
     * Retrieve a state system belonging to trace, by passing the ID of the
     * relevant analysis module.
     *
     * This will start the execution of the analysis module, and start the
     * construction of the state system, if needed.
     *
     * @param trace
     *            The trace for which you want the state system
     * @param moduleId
     *            The ID of the state system analysis module
     * @return The state system, or null if there was no match or the module was
     *         not initialized correctly
     */
    public static @Nullable ITmfStateSystem getStateSystem(ITmfTrace trace, String moduleId) {
        TmfStateSystemAnalysisModule module =
                TmfTraceUtils.getAnalysisModuleOfClass(trace, TmfStateSystemAnalysisModule.class, moduleId);
        if (module != null) {
            ITmfStateSystem ss = module.getStateSystem();
            if (ss != null) {
                return ss;
            }
            IStatus status = module.schedule();
            if (status.isOK()) {
                return module.waitForInitialization() ? module.getStateSystem() : null;
            }
        }
        return null;
    }

    /**
     * Get the state provider for this analysis module
     *
     * @return the state provider
     */
    protected abstract ITmfStateProvider createStateProvider();

    /**
     * Get the state system backend type used by this module
     *
     * @return The {@link StateSystemBackendType}
     */
    protected StateSystemBackendType getBackendType() {
        /* Using full history by default, sub-classes can override */
        return StateSystemBackendType.FULL;
    }

    /**
     * Get the supplementary file name where to save this state system. The
     * default is the ID of the analysis followed by the extension.
     *
     * @return The supplementary file name
     */
    protected String getSsFileName() {
        return getId() + EXTENSION;
    }

    /**
     * Get the state system generated by this analysis, or null if it is not yet
     * created.
     *
     * @return The state system
     */
    @Nullable
    public ITmfStateSystem getStateSystem() {
        return fStateSystem;
    }

    /**
     * @since 2.0
     */
    @Override
    public boolean waitForInitialization() {
        try {
            fInitialized.await();
        } catch (InterruptedException e) {
            return false;
        }
        return fInitializationSucceeded;
    }

    // ------------------------------------------------------------------------
    // TmfAbstractAnalysisModule
    // ------------------------------------------------------------------------

    private @Nullable File getSsFile() {
        ITmfTrace trace = getTrace();
        if (trace == null) {
            return null;
        }
        String directory = TmfTraceManager.getSupplementaryFileDir(trace);
        File htFile = new File(directory + getSsFileName());
        return htFile;
    }

    @Override
    protected boolean executeAnalysis(@Nullable final  IProgressMonitor monitor) {
        IProgressMonitor mon = (monitor == null ? new NullProgressMonitor() : monitor);
        final ITmfStateProvider provider = createStateProvider();

        String id = getId();

        /* FIXME: State systems should make use of the monitor, to be cancelled */
        try {
            /* Get the state system according to backend */
            StateSystemBackendType backend = getBackendType();


            ITmfTrace trace = getTrace();
            if (trace == null) {
                // Analysis was cancelled in the meantime
                analysisReady(false);
                return false;
            }
            switch (backend) {
            case FULL: {
                File htFile = getSsFile();
                if (htFile == null) {
                    return false;
                }
                createFullHistory(id, provider, htFile);
            }
                break;
            case PARTIAL: {
                File htFile = getSsFile();
                if (htFile == null) {
                    return false;
                }
                createPartialHistory(id, provider, htFile);
            }
                break;
            case INMEM:
                createInMemoryHistory(id, provider);
                break;
            case NULL:
                createNullHistory(id, provider);
                break;
            default:
                break;
            }
        } catch (TmfTraceException e) {
            analysisReady(false);
            return false;
        }
        return !mon.isCanceled();
    }

    /**
     * Make the module available and set whether the initialization succeeded or
     * not. If not, no state system is available and
     * {@link #waitForInitialization()} should return false.
     *
     * @param success
     *            True if the initialization succeeded, false otherwise
     */
    private void analysisReady(boolean succeeded) {
        fInitializationSucceeded = succeeded;
        fInitialized.countDown();
    }

    @Override
    protected void canceling() {
        ITmfEventRequest req = fRequest;
        if ((req != null) && (!req.isCompleted())) {
            req.cancel();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (fStateSystem != null) {
            fStateSystem.dispose();
        }
    }

    // ------------------------------------------------------------------------
    // History creation methods
    // ------------------------------------------------------------------------

    /*
     * Load the history file matching the target trace. If the file already
     * exists, it will be opened directly. If not, it will be created from
     * scratch.
     */
    private void createFullHistory(String id, ITmfStateProvider provider, File htFile) throws TmfTraceException {

        /* If the target file already exists, do not rebuild it uselessly */
        // TODO for now we assume it's complete. Might be a good idea to check
        // at least if its range matches the trace's range.

        if (htFile.exists()) {
           /* Load an existing history */
            final int version = provider.getVersion();
            try {
                IStateHistoryBackend backend = StateHistoryBackendFactory.createHistoryTreeBackendExistingFile(
                        id, htFile, version);
                fHtBackend = backend;
                fStateSystem = StateSystemFactory.newStateSystem(backend, false);
                analysisReady(true);
                return;
            } catch (IOException e) {
                /*
                 * There was an error opening the existing file. Perhaps it was
                 * corrupted, perhaps it's an old version? We'll just
                 * fall-through and try to build a new one from scratch instead.
                 */
            }
        }

        /* Size of the blocking queue to use when building a state history */
        final int QUEUE_SIZE = 10000;

        try {
            IStateHistoryBackend backend = StateHistoryBackendFactory.createHistoryTreeBackendNewFile(
                    id, htFile, provider.getVersion(), provider.getStartTime(), QUEUE_SIZE);
            fHtBackend = backend;
            fStateSystem = StateSystemFactory.newStateSystem(backend);
            provider.assignTargetStateSystem(fStateSystem);
            build(provider);
        } catch (IOException e) {
            /*
             * If it fails here however, it means there was a problem writing to
             * the disk, so throw a real exception this time.
             */
            throw new TmfTraceException(e.toString(), e);
        }
    }

    /*
     * Create a new state system backed with a partial history. A partial
     * history is similar to a "full" one (which you get with
     * {@link #newFullHistory}), except that the file on disk is much smaller,
     * but queries are a bit slower.
     *
     * Also note that single-queries are implemented using a full-query
     * underneath, (which are much slower), so this might not be a good fit for
     * a use case where you have to do lots of single queries.
     */
    private void createPartialHistory(String id, ITmfStateProvider provider, File htPartialFile)
            throws TmfTraceException {
        /*
         * The order of initializations is very tricky (but very important!)
         * here. We need to follow this pattern:
         * (1 is done before the call to this method)
         *
         * 1- Instantiate realStateProvider
         * 2- Instantiate realBackend
         * 3- Instantiate partialBackend, with prereqs:
         *  3a- Instantiate partialProvider, via realProvider.getNew()
         *  3b- Instantiate nullBackend (partialSS's backend)
         *  3c- Instantiate partialSS
         *  3d- partialProvider.assignSS(partialSS)
         * 4- Instantiate realSS
         * 5- partialSS.assignUpstream(realSS)
         * 6- realProvider.assignSS(realSS)
         * 7- Call HistoryBuilder(realProvider, realSS, partialBackend) to build the thing.
         */

        /* Size of the blocking queue to use when building a state history */
        final int QUEUE_SIZE = 10000;

        final long granularity = 50000;

        /* 2 */
        IStateHistoryBackend realBackend = null;
        try {
            realBackend = StateHistoryBackendFactory.createHistoryTreeBackendNewFile(
                    id, htPartialFile, provider.getVersion(), provider.getStartTime(), QUEUE_SIZE);
        } catch (IOException e) {
            throw new TmfTraceException(e.toString(), e);
        }

        /* 3a */
        ITmfStateProvider partialProvider = provider.getNewInstance();

        /* 3b-3c, constructor automatically uses a NullBackend */
        PartialStateSystem pss = new PartialStateSystem();

        /* 3d */
        partialProvider.assignTargetStateSystem(pss);

        /* 3 */
        IStateHistoryBackend partialBackend = new PartialHistoryBackend(id + ".partial", partialProvider, pss, realBackend, granularity); //$NON-NLS-1$

        /* 4 */
        @SuppressWarnings("restriction")
        org.eclipse.tracecompass.internal.statesystem.core.StateSystem realSS =
        (org.eclipse.tracecompass.internal.statesystem.core.StateSystem) StateSystemFactory.newStateSystem(partialBackend);

        /* 5 */
        pss.assignUpstream(realSS);

        /* 6 */
        provider.assignTargetStateSystem(realSS);

        /* 7 */
        fHtBackend = partialBackend;
        fStateSystem = realSS;

        build(provider);
    }

    /*
     * Create a new state system using a null history back-end. This means that
     * no history intervals will be saved anywhere, and as such only
     * {@link ITmfStateSystem#queryOngoingState} will be available.
     */
    private void createNullHistory(String id, ITmfStateProvider provider) {
        IStateHistoryBackend backend = StateHistoryBackendFactory.createNullBackend(id);
        fHtBackend = backend;
        fStateSystem = StateSystemFactory.newStateSystem(backend);
        provider.assignTargetStateSystem(fStateSystem);
        build(provider);
    }

    /*
     * Create a new state system using in-memory interval storage. This should
     * only be done for very small state system, and will be naturally limited
     * to 2^31 intervals.
     */
    private void createInMemoryHistory(String id, ITmfStateProvider provider) {
        IStateHistoryBackend backend = StateHistoryBackendFactory.createInMemoryBackend(id, provider.getStartTime());
        fHtBackend = backend;
        fStateSystem = StateSystemFactory.newStateSystem(backend);
        provider.assignTargetStateSystem(fStateSystem);
        build(provider);
    }

    private void disposeProvider(boolean deleteFiles) {
        ITmfStateProvider provider = fStateProvider;
        if (provider != null) {
            provider.dispose();
        }
        if (deleteFiles && (fHtBackend != null)) {
            fHtBackend.removeFiles();
        }
    }

    private void build(ITmfStateProvider provider) {
        if ((fStateSystem == null) || (fHtBackend == null)) {
            throw new IllegalArgumentException();
        }

        ITmfEventRequest request = fRequest;
        if ((request != null) && (!request.isCompleted())) {
            request.cancel();
        }

        fTimeRange = TmfTimeRange.ETERNITY;
        final ITmfTrace trace = provider.getTrace();
        if (!isCompleteTrace(trace)) {
            fTimeRange = trace.getTimeRange();
        }

        fStateProvider = provider;
        synchronized (fRequestSyncObj) {
            startRequest();
        }

        /*
         * The state system object is now created, we can consider this module
         * "initialized" (components can retrieve it and start doing queries).
         */
        analysisReady(true);

        /*
         * Block the executeAnalysis() construction is complete (so that the
         * progress monitor displays that it is running).
         */
        try {
            if (fRequest != null) {
                fRequest.waitForCompletion();
            }
        } catch (InterruptedException e) {
             e.printStackTrace();
        }
    }

    private class StateSystemEventRequest extends TmfEventRequest {
        private final ITmfStateProvider sci;
        private final ITmfTrace trace;

        public StateSystemEventRequest(ITmfStateProvider sp, TmfTimeRange timeRange, int index) {
            super(ITmfEvent.class,
                    timeRange,
                    index,
                    ITmfEventRequest.ALL_DATA,
                    ITmfEventRequest.ExecutionType.BACKGROUND);
            this.sci = sp;
            trace = sci.getTrace();

        }

        @Override
        public void handleData(final ITmfEvent event) {
            super.handleData(event);
            if (event.getTrace() == trace) {
                sci.processEvent(event);
            } else if (trace instanceof TmfExperiment) {
                /*
                 * If the request is for an experiment, check if the event is
                 * from one of the child trace
                 */
                for (ITmfTrace childTrace : ((TmfExperiment) trace).getTraces()) {
                    if (childTrace == event.getTrace()) {
                        sci.processEvent(event);
                    }
                }
            }
        }

        @Override
        public void handleSuccess() {
            super.handleSuccess();
            if (isCompleteTrace(trace)) {
                disposeProvider(false);
            } else {
                fNbRead += getNbRead();
                synchronized (fRequestSyncObj) {
                    final TmfTimeRange timeRange = fTimeRange;
                    if (timeRange != null) {
                        if (getRange().getEndTime().getValue() < timeRange.getEndTime().getValue()) {
                            startRequest();
                        }
                    }
                }
            }
        }

        @Override
        public void handleCancel() {
            super.handleCancel();
            if (isCompleteTrace(trace)) {
                disposeProvider(true);
            }
        }

        @Override
        public void handleFailure() {
            super.handleFailure();
            disposeProvider(true);
        }
    }

    // ------------------------------------------------------------------------
    // ITmfAnalysisModuleWithStateSystems
    // ------------------------------------------------------------------------

    @Override
    @Nullable
    public ITmfStateSystem getStateSystem(String id) {
        if (id.equals(getId())) {
            return fStateSystem;
        }
        return null;
    }

    @Override
    public @NonNull Iterable<@NonNull ITmfStateSystem> getStateSystems() {
        ITmfStateSystemBuilder stateSystem = fStateSystem;
        if (stateSystem == null) {
            return Collections.EMPTY_SET;
        }
        return Collections.singleton(stateSystem);
    }

    /**
     * Signal handler for the TmfTraceRangeUpdatedSignal signal
     *
     * @param signal The incoming signal
     */
    @TmfSignalHandler
    public void traceRangeUpdated(final TmfTraceRangeUpdatedSignal signal) {
        fTimeRange = signal.getRange();
        ITmfStateProvider stateProvider = fStateProvider;
        synchronized (fRequestSyncObj) {
            if (signal.getTrace() == getTrace() && stateProvider != null && stateProvider.getAssignedStateSystem() != null) {
                ITmfEventRequest request = fRequest;
                if ((request == null) || request.isCompleted()) {
                    startRequest();
                }
            }
        }
    }

    private void startRequest() {
        ITmfStateProvider stateProvider = fStateProvider;
        TmfTimeRange timeRange = fTimeRange;
        if (stateProvider == null || timeRange == null) {
            return;
        }
        ITmfEventRequest request = new StateSystemEventRequest(stateProvider, timeRange, fNbRead);
        stateProvider.getTrace().sendRequest(request);
        fRequest = request;
    }

    private static boolean isCompleteTrace(ITmfTrace trace) {
        return !(trace instanceof ITmfTraceCompleteness) || ((ITmfTraceCompleteness) trace).isComplete();
    }

    // ------------------------------------------------------------------------
    // ITmfPropertiesProvider
    // ------------------------------------------------------------------------

    /**
     * @since 2.0
     */
    @Override
    public @NonNull Map<@NonNull String, @NonNull String> getProperties() {
        Map<@NonNull String, @NonNull String> properties = new HashMap<>();

        StateSystemBackendType backend = getBackendType();
        properties.put(NonNullUtils.checkNotNull(Messages.TmfStateSystemAnalysisModule_PropertiesBackend), backend.name());
        switch (backend) {
        case FULL:
        case PARTIAL:
            File htFile = getSsFile();
            if (htFile != null) {
                if (htFile.exists()) {
                    properties.put(NonNullUtils.checkNotNull(Messages.TmfStateSystemAnalysisModule_PropertiesFileSize), FileUtils.byteCountToDisplaySize(htFile.length()));
                } else {
                    properties.put(NonNullUtils.checkNotNull(Messages.TmfStateSystemAnalysisModule_PropertiesFileSize), NonNullUtils.checkNotNull(Messages.TmfStateSystemAnalysisModule_PropertiesAnalysisNotExecuted));
                }
            }
            break;
        case INMEM:
        case NULL:
        default:
            break;

        }
        return properties;
    }
}
