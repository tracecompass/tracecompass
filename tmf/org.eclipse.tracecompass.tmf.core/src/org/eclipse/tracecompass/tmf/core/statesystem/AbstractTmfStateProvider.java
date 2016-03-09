/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.statesystem;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.collect.BufferedBlockingQueue;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Instead of using IStateChangeInput directly, one can extend this class, which
 * defines a lot of the common functions of the state change input plugin.
 *
 * It will handle the state-system-processing in a separate thread, which is
 * normally not a bad idea for traces of some size.
 *
 * processEvent() is replaced with eventHandle(), so that all the multi-thread
 * logic is abstracted away.
 *
 * @author Alexandre Montplaisir
 */
public abstract class AbstractTmfStateProvider implements ITmfStateProvider {

    private static final int DEFAULT_EVENTS_QUEUE_SIZE = 127;
    private static final int DEFAULT_EVENTS_CHUNK_SIZE = 127;

    private final ITmfTrace fTrace;
    private final BufferedBlockingQueue<ITmfEvent> fEventsQueue;
    private final Thread fEventHandlerThread;

    private boolean fStateSystemAssigned;

    /** State system in which to insert the state changes */
    private @Nullable ITmfStateSystemBuilder fSS = null;

    /**
     * Instantiate a new state provider plugin.
     *
     * @param trace
     *            The LTTng 2.0 kernel trace directory
     * @param id
     *            Name given to this state change input. Only used internally.
     */
    public AbstractTmfStateProvider(ITmfTrace trace, String id) {
        fTrace = trace;
        fEventsQueue = new BufferedBlockingQueue<>(DEFAULT_EVENTS_QUEUE_SIZE, DEFAULT_EVENTS_CHUNK_SIZE);
        fStateSystemAssigned = false;

        fEventHandlerThread = new Thread(new EventProcessor(), id + " Event Handler"); //$NON-NLS-1$
    }

    /**
     * Get the state system builder of this provider (to insert states in).
     *
     * @return The state system object to be filled
     */
    protected @Nullable ITmfStateSystemBuilder getStateSystemBuilder() {
        return fSS;
    }

    @Override
    public ITmfTrace getTrace() {
        return fTrace;
    }

    @Override
    public long getStartTime() {
        return fTrace.getStartTime().toNanos();
    }

    @Override
    public void assignTargetStateSystem(ITmfStateSystemBuilder ssb) {
        fSS = ssb;
        fStateSystemAssigned = true;
        fEventHandlerThread.start();
    }

    @Override
    public @Nullable ITmfStateSystem getAssignedStateSystem() {
        return fSS;
    }

    @Override
    public void dispose() {
        /* Insert a null event in the queue to stop the event handler's thread. */
        try {
            fEventsQueue.put(END_EVENT);
            fEventsQueue.flushInputBuffer();
            fEventHandlerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        fStateSystemAssigned = false;
        fSS = null;
    }

    @Override
    public final void processEvent(ITmfEvent event) {
        /* Make sure the target state system has been assigned */
        if (!fStateSystemAssigned) {
            Activator.logError("Cannot process event without a target state system"); //$NON-NLS-1$
            return;
        }

        /* Insert the event we're received into the events queue */
        ITmfEvent curEvent = event;
        fEventsQueue.put(curEvent);
    }

    /**
     * Block the caller until the events queue is empty.
     */
    public void waitForEmptyQueue() {
        /*
         * We will first insert a dummy event that is guaranteed to not modify
         * the state. That way, when that event leaves the queue, we will know
         * for sure that the state system processed the preceding real event.
         */
        try {
            fEventsQueue.put(EMPTY_QUEUE_EVENT);
            fEventsQueue.flushInputBuffer();
            while (!fEventsQueue.isEmpty()) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------------
    // Special event types
    // ------------------------------------------------------------------------

    /** Fake event indicating the build is over, and the provider should close */
    private static class EndEvent extends TmfEvent {
        public EndEvent() {
            super(null, ITmfContext.UNKNOWN_RANK, null, null, null);
        }
    }

    /** Fake event indicating we want to clear the current queue */
    private static class EmptyQueueEvent extends TmfEvent {
        public EmptyQueueEvent() {
            super(null, ITmfContext.UNKNOWN_RANK, null, null, null);
        }
    }

    private static final EndEvent END_EVENT = new EndEvent();
    private static final EmptyQueueEvent EMPTY_QUEUE_EVENT = new EmptyQueueEvent();

    // ------------------------------------------------------------------------
    // Inner classes
    // ------------------------------------------------------------------------

    /**
     * This is the runner class for the second thread, which will take the
     * events from the queue and pass them through the state system.
     */
    private class EventProcessor implements Runnable {

        private @Nullable ITmfEvent currentEvent;

        @Override
        public void run() {
            if (!fStateSystemAssigned) {
                Activator.logError("Cannot run event manager without assigning a target state system first!"); //$NON-NLS-1$
                return;
            }


            /*
             * We never insert null in the queue. Cannot be checked at
             * compile-time until Java 8 annotations...
             */
            @NonNull ITmfEvent event = fEventsQueue.take();
            /* This is a singleton, we want to do != instead of !x.equals */
            while (event != END_EVENT) {
                if (event == EMPTY_QUEUE_EVENT) {
                    /* Synchronization event, should be ignored */
                    event = fEventsQueue.take();
                    continue;
                }
                currentEvent = event;
                eventHandle(event);
                event = fEventsQueue.take();
            }
            /* We've received the last event, clean up */
            closeStateSystem();
        }

        private void closeStateSystem() {
            ITmfEvent event = currentEvent;
            final long endTime = (event == null) ? 0 :
                    event.getTimestamp().toNanos();

            if (fSS != null) {
                fSS.closeHistory(endTime);
            }
        }
    }

    // ------------------------------------------------------------------------
    // Abstract methods
    // ------------------------------------------------------------------------

    /**
     * Handle the given event and send the appropriate state transitions into
     * the the state system.
     *
     * This is basically the same thing as IStateChangeInput.processEvent(),
     * except here processEvent() and eventHandle() are run in two different
     * threads (and the AbstractStateChangeInput takes care of processEvent()
     * already).
     *
     * @param event
     *            The event to process. If you need a specific event type, you
     *            should check for its instance right at the beginning.
     */
    protected abstract void eventHandle(ITmfEvent event);

}
