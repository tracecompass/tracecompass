/*******************************************************************************
 * Copyright (c) 2012 Ericsson
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

import org.eclipse.linuxtools.tmf.core.component.TmfComponent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfStateSystemBuildCompleted;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateChangeInput;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateSystemBuilder;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateSystemQuerier;
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
    private final StateHistorySystem shs;
    private final IStateHistoryBackend hb;
    private boolean started = true; /* Don't handle signals until we're ready */

    /**
     * Instantiate a new HistoryBuilder helper.
     *
     * @param stateChangeInput
     *            The input plugin to use. This is required.
     * @param backend
     *            The backend storage to use.
     * @param buildManually
     *            Should we build this history in-band or not. True means we
     *            will start the building ourselves and block the caller until
     *            construction is done. False (out-of-band) means we will
     *            start listening for the signal and return immediately. Another
     *            signal will be sent when finished.
     * @throws IOException
     *             Is thrown if anything went wrong (usually with the storage
     *             backend)
     */
    public HistoryBuilder(IStateChangeInput stateChangeInput,
            IStateHistoryBackend backend, boolean buildManually)
            throws IOException {
        if (stateChangeInput == null || backend == null) {
            throw new IllegalArgumentException();
        }
        sci = stateChangeInput;
        hb = backend;
        shs = new StateHistorySystem(hb, true);

        sci.assignTargetStateSystem(shs);

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
    public static IStateSystemBuilder openExistingHistory(
            IStateHistoryBackend hb) throws IOException {
        return new StateHistorySystem(hb, false);
    }

    /**
     * Return a read/write reference to the state system object that was
     * created.
     *
     * @return Reference to the state system, with access to everything.
     */
    public IStateSystemBuilder getStateSystemBuilder() {
        return shs;
    }

    /**
     * Return a read-only reference to the state system object that was created.
     *
     * @return Reference to the state system, but only with the query methods
     *         available.
     */
    public IStateSystemQuerier getStateSystemQuerier() {
        return shs;
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
     * Listen to the "experiment selected" signal to start the state history
     * construction.
     *
     * @param signal
     *            The "experiment range updated" signal. Listening to this
     *            signal will coalesce this request with the one from the
     *            indexer and histogram.
     */
    @TmfSignalHandler
    public void experimentRangeUpdated(final TmfExperimentRangeUpdatedSignal signal) {
        StateSystemBuildRequest request;
        TmfExperiment<ITmfEvent> exp;

        if (!started) {
            started = true;
            request = new StateSystemBuildRequest(this);
            exp = (TmfExperiment<ITmfEvent>) TmfExperiment.getCurrentExperiment();
            if (exp == null) {
                return;
            }
            exp.sendRequest(request);
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
        TmfSignal doneSig;

        sci.dispose();
        if (deleteFiles) {
            hb.removeFiles();
            /* We won't broadcast the signal if the request was cancelled */
        } else {
            /* Broadcast the signal saying the history is done building */
            doneSig = new TmfStateSystemBuildCompleted(this, sci.getTrace());
            TmfSignalManager.dispatchSignal(doneSig);
        }

        TmfSignalManager.deregister(this);
    }
}

class StateSystemBuildRequest extends TmfEventRequest<ITmfEvent> {

    /** The amount of events queried at a time through the requests */
    private final static int chunkSize = 50000;

    private final HistoryBuilder builder;
    private final IStateChangeInput sci;
    private final ITmfTrace<ITmfEvent> trace;

    StateSystemBuildRequest(HistoryBuilder builder) {
        super((Class<ITmfEvent>) builder.getInputPlugin().getExpectedEventType().getClass(),
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