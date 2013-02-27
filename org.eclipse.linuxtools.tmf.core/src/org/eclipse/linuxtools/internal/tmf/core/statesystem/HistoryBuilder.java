/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.statesystem;

import java.io.IOException;

import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.IStateHistoryBackend;
import org.eclipse.linuxtools.tmf.core.component.TmfComponent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateChangeInput;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystemBuilder;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;

/**
 * This is the high-level wrapper around the State History and its input and
 * storage plugins. Just create the object using the constructor then .run()
 *
 * You can use one HistoryBuilder and it will instantiate everything underneath.
 * If you need more fine-grained control you can still ignore this and
 * instantiate everything manually.
 *
 * @author alexmont
 *
 */
public class HistoryBuilder extends TmfComponent {

    private final IStateChangeInput sci;
    private final StateSystem ss;
    private final IStateHistoryBackend hb;
    private boolean started = true; /* Don't handle signals until we're ready */

    /**
     * Instantiate a new HistoryBuilder helper. The input -> ss -> backend
     * relationships should have been set up already.
     *
     * @param stateChangeInput
     *            The input plugin to use
     * @param ss
     *            The state system object that will receive the state changes
     *            from the input
     * @param backend
     *            The back-end storage to use, which will receive the intervals
     *            from the ss
     * @param buildManually
     *            Should we build this history in-band or not. True means we
     *            will start the building ourselves and block the caller until
     *            construction is done. False (out-of-band) means we will start
     *            listening for the signal and return immediately.
     */
    public HistoryBuilder(IStateChangeInput stateChangeInput, StateSystem ss,
            IStateHistoryBackend backend, boolean buildManually) {
        if (stateChangeInput == null || backend == null || ss == null) {
            throw new IllegalArgumentException();
        }
        if (stateChangeInput.getAssignedStateSystem() != ss) {
            /* Logic check to make sure the input is setup properly */
            throw new RuntimeException();
        }

        sci = stateChangeInput;
        hb = backend;
        this.ss = ss;

        if (buildManually) {
            TmfSignalManager.deregister(this);
            this.buildManually();
        } else {
            started = false;
            /* We'll now wait for the signal to start building */
        }
    }

    /**
     * Factory-style method to open an existing history, you only have to
     * provide the already-instantiated IStateHistoryBackend object.
     *
     * @param hb
     *            The history-backend object
     * @return A IStateSystemBuilder reference to the new state system. If you
     *         will only run queries on this history, you should *definitely*
     *         cast it to IStateSystemQuerier.
     * @throws IOException
     *             If there was something wrong.
     */
    public static ITmfStateSystemBuilder openExistingHistory(
            IStateHistoryBackend hb) throws IOException {
        return new StateSystem(hb, false);
    }

    /**
     * Return a read/write reference to the state system object that was
     * created.
     *
     * @return Reference to the state system, with access to everything.
     */
    public ITmfStateSystemBuilder getStateSystemBuilder() {
        return ss;
    }

    /**
     * Return a read-only reference to the state system object that was created.
     *
     * @return Reference to the state system, but only with the query methods
     *         available.
     */
    public ITmfStateSystem getStateSystemQuerier() {
        return ss;
    }

    /**
     * Build the state history without waiting for signals or anything
     */
    private void buildManually() {
        StateSystemBuildRequest request = new StateSystemBuildRequest(this);

        /* Send the request to the trace here, since there is probably no
         * experiment. */
        sci.getTrace().sendRequest(request);
        try {
            request.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Listen to the "trace range updated" signal to start the state history
     * construction.
     *
     * @param signal
     *            The "trace range updated" signal. Listening to this
     *            signal will coalesce this request with the one from the
     *            indexer and histogram.
     */
    @TmfSignalHandler
    public void traceRangeUpdated(final TmfTraceRangeUpdatedSignal signal) {
        ITmfTrace trace = signal.getTrace();
        if (signal.getTrace() instanceof TmfExperiment) {
            TmfExperiment experiment = (TmfExperiment) signal.getTrace();
            for (ITmfTrace expTrace : experiment.getTraces()) {
                if (expTrace == sci.getTrace()) {
                    trace = expTrace;
                    break;
                }
            }
        }
        if (trace != sci.getTrace()) {
            return;
        }
        /* the signal is for this trace or for an experiment containing this trace */

        if (!started) {
            started = true;
            StateSystemBuildRequest request = new StateSystemBuildRequest(this);
            trace = signal.getTrace();
            trace.sendRequest(request);
        }
    }

    /**
     * Listen to the "trace closed" signal to clean up if necessary.
     *
     * @param signal
     *            The "trace closed" signal.
     */
    @TmfSignalHandler
    public void traceClosed(TmfTraceClosedSignal signal) {
        ITmfTrace trace = signal.getTrace();
        if (signal.getTrace() instanceof TmfExperiment) {
            TmfExperiment experiment = (TmfExperiment) signal.getTrace();
            for (ITmfTrace expTrace : experiment.getTraces()) {
                if (expTrace == sci.getTrace()) {
                    trace = expTrace;
                    break;
                }
            }
        }
        if (trace != sci.getTrace()) {
            return;
        }
        /* the signal is for this trace or for an experiment containing this trace */

        if (!started) {
            close(true);
        }
    }

    // ------------------------------------------------------------------------
    // Methods reserved for the request object below
    // ------------------------------------------------------------------------

    /** Get the input plugin object */
    IStateChangeInput getInputPlugin() {
        return sci;
    }

    void close(boolean deleteFiles) {
        sci.dispose();
        if (deleteFiles) {
            hb.removeFiles();
        }
        dispose();
    }
}

class StateSystemBuildRequest extends TmfEventRequest {

    /** The amount of events queried at a time through the requests */
    private final static int chunkSize = 50000;

    private final HistoryBuilder builder;
    private final IStateChangeInput sci;
    private final ITmfTrace trace;

    StateSystemBuildRequest(HistoryBuilder builder) {
        super(builder.getInputPlugin().getExpectedEventType(),
                TmfTimeRange.ETERNITY,
                TmfDataRequest.ALL_DATA,
                chunkSize,
                ITmfDataRequest.ExecutionType.BACKGROUND);
        this.builder = builder;
        this.sci = builder.getInputPlugin();
        this.trace = sci.getTrace();
    }

    @Override
    public void handleData(final ITmfEvent event) {
        super.handleData(event);
        if (event != null) {
            if (event.getTrace() == trace) {
                sci.processEvent(event);
            }
        }
    }

    @Override
    public void handleSuccess() {
        super.handleSuccess();
        builder.close(false);
    }

    @Override
    public void handleCancel() {
        super.handleCancel();
        builder.close(true);
    }

    @Override
    public void handleFailure() {
        super.handleFailure();
        builder.close(true);
    }
}