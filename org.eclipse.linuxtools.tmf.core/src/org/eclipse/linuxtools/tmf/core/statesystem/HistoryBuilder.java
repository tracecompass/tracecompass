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

package org.eclipse.linuxtools.tmf.core.statesystem;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.eclipse.linuxtools.internal.tmf.core.statesystem.StateHistorySystem;
import org.eclipse.linuxtools.tmf.core.component.TmfComponent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;

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

    private static final Semaphore hbSem = new Semaphore(1);

    private ITmfEventRequest<CtfTmfEvent> currentRequest = null;
    private boolean isRunning = false;

    private final IStateChangeInput sci;
    private final StateHistorySystem shs;
    private final IStateHistoryBackend hb;

    /**
     * Instantiate a new HistoryBuilder helper.
     * 
     * @param stateChangeInput
     *            The input plugin to use. This is required.
     * @param backend
     *            The backend storage to use. Use "null" here if you want a
     *            state system with no history.
     * @throws IOException
     *             Is thrown if anything went wrong (usually with the storage
     *             backend)
     */
    public HistoryBuilder(IStateChangeInput stateChangeInput,
            IStateHistoryBackend backend) throws IOException {
        if (stateChangeInput == null || backend == null) {
            throw new IllegalArgumentException();
        }
        sci = stateChangeInput;
        hb = backend;
        shs = new StateHistorySystem(hb, true);

        sci.assignTargetStateSystem(shs);
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

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Signal handler to start building the configured state history.
     * 
     * This could also be called by anyone to trigger the building of the
     * history without using any TMF signals. Simply pass 'null' as a parameter
     * then.
     * 
     * @param signal The signal that triggered the build
     */
    @TmfSignalHandler
    public void startBuilding(final TmfExperimentRangeUpdatedSignal signal) {
        /* Start the construction of the history if it's not started yet */
        if (!this.isRunning) {
            hbSem.acquireUninterruptibly();
            currentRequest = new StateSystemBuildRequest(this);
            isRunning = true;
            sci.getTrace().sendRequest(currentRequest);
        }
    }

    /**
     * Signal handler to cancel any currently running requests. It will delete
     * any incomplete file that might have been created.
     * 
     * This could also be called by anyone to cancel the current request without
     * using any TMF signals. Simply pass 'null' as a parameter then.
     * 
     * @param signal
     */
    @TmfSignalHandler
    public void cancelCurrentBuild(final TmfExperimentSelectedSignal<? extends ITmfEvent> signal) {
        /*
         * We've switched experiments (or re-opened the current one), so stop
         * whatever request is currently running.
         */
        if (this.isRunning && currentRequest != null) {
            currentRequest.cancel();
        }
    } 

    
    // ------------------------------------------------------------------------
    // Methods reserved for the request object below
    // ------------------------------------------------------------------------

    /** Get the input plugin object */
    IStateChangeInput getInputPlugin() {
        return sci;
    }

    /** Shutdown this builder object when the request is over */
    void finish(boolean deleteFile) {
        sci.dispose();
        if (deleteFile) {
            hb.removeFiles();
        }
        currentRequest = null;
        isRunning = false;
        TmfSignalManager.deregister(this);
        hbSem.release();
    }
}


class StateSystemBuildRequest extends TmfEventRequest<CtfTmfEvent> {

    /** The amount of events queried at a time through the requests */
    private final static int chunkSize = 50000;

    private final HistoryBuilder builder;
    private final IStateChangeInput sci;

    StateSystemBuildRequest(HistoryBuilder builder) {
        super((Class<CtfTmfEvent>) builder.getInputPlugin().getExpectedEventType().getClass(),
                TmfTimeRange.ETERNITY, TmfDataRequest.ALL_DATA, chunkSize,
                ITmfDataRequest.ExecutionType.BACKGROUND);
        this.builder = builder;
        this.sci = builder.getInputPlugin();
    }

    @Override
    public void handleData(final CtfTmfEvent event) {
        super.handleData(event);
        if (event != null) {
            sci.processEvent(event);
        }
    }

    @Override
    public void handleSuccess() {
        super.handleSuccess();
        builder.finish(false);
    }

    @Override
    public void handleCancel() {
        super.handleCancel();
        builder.finish(true);
    }

    @Override
    public void handleFailure() {
        super.handleFailure();
        builder.finish(true);
    }
}