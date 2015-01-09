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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
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
 * @since 2.0
 */
public abstract class AbstractTmfStateProvider implements ITmfStateProvider {

    private static final int DEFAULT_EVENTS_QUEUE_SIZE = 10000;

    private final ITmfTrace fTrace;
    private final Class<? extends ITmfEvent> fEventType;
    private final BlockingQueue<ITmfEvent> fEventsQueue;
    private final Thread fEventHandlerThread;

    private boolean fStateSystemAssigned;

    /** State system in which to insert the state changes */
    private @Nullable ITmfStateSystemBuilder fSS = null;

    /**
     * Instantiate a new state provider plugin.
     *
     * @param trace
     *            The LTTng 2.0 kernel trace directory
     * @param eventType
     *            The specific class for the event type that will be used within
     *            the subclass
     * @param id
     *            Name given to this state change input. Only used internally.
     */
    public AbstractTmfStateProvider(ITmfTrace trace,
            Class<? extends ITmfEvent> eventType, String id) {
        fTrace = trace;
        fEventType = eventType;
        fEventsQueue = new ArrayBlockingQueue<>(DEFAULT_EVENTS_QUEUE_SIZE);
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

    /**
     * @since 3.0
     */
    @Override
    public long getStartTime() {
        return fTrace.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
    }

    /**
     * @since 3.0
     */
    @Override
    public void assignTargetStateSystem(ITmfStateSystemBuilder ssb) {
        fSS = ssb;
        fStateSystemAssigned = true;
        fEventHandlerThread.start();
    }

    /**
     * @since 3.0
     */
    @Override
    public @Nullable ITmfStateSystem getAssignedStateSystem() {
        return fSS;
    }

    @Override
    public void dispose() {
        /* Insert a null event in the queue to stop the event handler's thread. */
        try {
            fEventsQueue.put(END_EVENT);
            fEventHandlerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        fStateSystemAssigned = false;
        fSS = null;
    }

    @Override
    public final Class<? extends ITmfEvent> getExpectedEventType() {
        return fEventType;
    }

    @Override
    public final void processEvent(ITmfEvent event) {
        /* Make sure the target state system has been assigned */
        if (!fStateSystemAssigned) {
            System.err.println("Cannot process event without a target state system"); //$NON-NLS-1$
            return;
        }

        /* Insert the event we're received into the events queue */
        ITmfEvent curEvent = event;
        try {
            fEventsQueue.put(curEvent);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
                System.err.println("Cannot run event manager without assigning a target state system first!"); //$NON-NLS-1$
                return;
            }
            ITmfEvent event;

            try {
                event = fEventsQueue.take();
                /* This is a singleton, we want to do != instead of !x.equals */
                while (event != END_EVENT) {
                    if (event == EMPTY_QUEUE_EVENT) {
                        /* Synchronization event, should be ignored */
                        event = fEventsQueue.take();
                        continue;
                    }

                    currentEvent = event;

                    /* Make sure this is an event the sub-class can process */
                    if (fEventType.isInstance(event) && event.getType() != null) {
                        eventHandle(event);
                    }
                    event = fEventsQueue.take();
                }
                /* We've received the last event, clean up */
                closeStateSystem();
            } catch (InterruptedException e) {
                /* We've been interrupted abnormally */
                System.out.println("Event handler interrupted!"); //$NON-NLS-1$
                e.printStackTrace();
            }
        }

        private void closeStateSystem() {
            ITmfEvent event = currentEvent;
            final long endTime = (event == null) ? 0 :
                    event.getTimestamp().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();

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
