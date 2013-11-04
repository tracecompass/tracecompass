/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.component;

import java.util.concurrent.CountDownLatch;

import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.internal.tmf.core.TmfCoreTracer;
import org.eclipse.linuxtools.tmf.core.component.ITmfDataProvider;
import org.eclipse.linuxtools.tmf.core.component.TmfDataProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;

/**
 * Provides the core event request processor. It also has support for suspending
 * and resuming a request in a thread-safe manner.
 *
 * @author Francois Chouinard
 * @version 1.0
 */
public class TmfEventThread implements Runnable {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The event provider
     */
    private final TmfDataProvider fProvider;

    /**
     * The wrapped event request
     */
    private final ITmfDataRequest fRequest;

    /**
     * The request execution priority
     */
    private final ExecutionType   fExecType;

    /**
     * The wrapped thread (if applicable)
     */
    private final TmfEventThread  fThread;

    /**
     * The thread execution state
     */
    private volatile boolean isCompleted = false;

    /** Latch indicating if the thread is currently paused (>0 means paused) */
    private CountDownLatch pausedLatch = new CountDownLatch(0);

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Basic constructor
     *
     * @param provider the event provider
     * @param request the request to process
     */
    public TmfEventThread(TmfDataProvider provider, ITmfDataRequest request) {
        assert provider != null;
        assert request  != null;
        fProvider = provider;
        fRequest  = request;
        fExecType = request.getExecType();
        fThread   = null;
    }

    /**
     * Wrapper constructor
     *
     * @param thread the thread to wrap
     */
    public TmfEventThread(TmfEventThread thread) {
        fProvider = thread.fProvider;
        fRequest  = thread.fRequest;
        fExecType = thread.fExecType;
        fThread   = thread;
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return The wrapped thread
     */
    public TmfEventThread getThread() {
        return fThread;
    }

    /**
     * @return The event provider
     */
    public ITmfDataProvider getProvider() {
        return fProvider;
    }

    /**
     * @return The event request
     */
    public ITmfDataRequest getRequest() {
        return fRequest;
    }

    /**
     * @return The request execution priority
     */
    public ExecutionType getExecType() {
        return fExecType;
    }

    /**
     * @return The request execution state
     */
    public boolean isRunning() {
        return fRequest.isRunning() && !isPaused();
    }

    /**
     * @return The request execution state
     */
    public synchronized boolean isPaused(){
        return (pausedLatch.getCount() > 0);
    }

    /**
     * @return The request execution state
     */
    public boolean isCompleted() {
        return isCompleted;
    }

    // ------------------------------------------------------------------------
    // Runnable
    // ------------------------------------------------------------------------

    @Override
    public void run() {

        TmfCoreTracer.traceRequest(fRequest, "is being serviced by " + fProvider.getName()); //$NON-NLS-1$

        // Extract the generic information
        fRequest.start();
        int nbRequested = fRequest.getNbRequested();
        int nbRead = 0;
        isCompleted = false;

        // Initialize the execution
        ITmfContext context = fProvider.armRequest(fRequest);
        if (context == null) {
            fRequest.cancel();
            return;
        }

        try {
            // Get the ordered events
            ITmfEvent event = fProvider.getNext(context);
            TmfCoreTracer.traceRequest(fRequest, "read first event"); //$NON-NLS-1$

            while (event != null && !fProvider.isCompleted(fRequest, event, nbRead)) {

                TmfCoreTracer.traceEvent(fProvider, fRequest, event);
                if (fRequest.getDataType().isInstance(event)) {
                    fRequest.handleData(event);
                }

                // Pause execution if requested
                pausedLatch.await();

                // To avoid an unnecessary read passed the last event requested
                if (++nbRead < nbRequested) {
                    event = fProvider.getNext(context);
                }
            }

            isCompleted = true;

            if (fRequest.isCancelled()) {
                fRequest.cancel();
            } else {
                fRequest.done();
            }

        } catch (Exception e) {
            Activator.logError("Error in " + fProvider.getName() + " handling " + fRequest, e); //$NON-NLS-1$ //$NON-NLS-2$
            fRequest.fail();
        }

        // Cleanup
        context.dispose();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Suspend the thread
     */
    public synchronized void suspend() {
        pausedLatch = new CountDownLatch(1);
        TmfCoreTracer.traceRequest(fRequest, "SUSPENDED"); //$NON-NLS-1$
    }

    /**
     * Resume the thread
     */
    public synchronized void resume() {
        pausedLatch.countDown();
        TmfCoreTracer.traceRequest(fRequest, "RESUMED"); //$NON-NLS-1$
    }

    /**
     * Cancel the request
     */
    public void cancel() {
        if (!fRequest.isCompleted()) {
            fRequest.cancel();
        }
    }
}
