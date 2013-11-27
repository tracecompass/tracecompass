/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statesystem;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEventFactory;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;


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

    private final ITmfTrace trace;
    private final Class<? extends ITmfEvent> eventType;
    private final BlockingQueue<ITmfEvent> eventsQueue;
    private final Thread eventHandlerThread;

    private boolean ssAssigned;
    private ITmfEvent currentEvent;

    /** State system in which to insert the state changes */
    protected ITmfStateSystemBuilder ss = null;

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
        this.trace = trace;
        this.eventType = eventType;
        eventsQueue = new ArrayBlockingQueue<ITmfEvent>(DEFAULT_EVENTS_QUEUE_SIZE);
        ssAssigned = false;

        String id2 = (id == null ? "Unamed" : id); //$NON-NLS-1$
        eventHandlerThread = new Thread(new EventProcessor(), id2 + " Event Handler"); //$NON-NLS-1$
    }

    @Override
    public ITmfTrace getTrace() {
        return trace;
    }

    @Override
    public long getStartTime() {
        return trace.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
    }

    @Override
    public void assignTargetStateSystem(ITmfStateSystemBuilder ssb) {
        ss = ssb;
        ssAssigned = true;
        eventHandlerThread.start();
    }

    @Override
    public ITmfStateSystem getAssignedStateSystem() {
        return ss;
    }

    @Override
    public void dispose() {
        /* Insert a null event in the queue to stop the event handler's thread. */
        try {
            eventsQueue.put(CtfTmfEventFactory.getNullEvent());
            eventHandlerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ssAssigned = false;
        ss = null;
    }

    @Override
    public final Class<? extends ITmfEvent> getExpectedEventType() {
        return eventType;
    }

    @Override
    public final void processEvent(ITmfEvent event) {
        /* Make sure the target state system has been assigned */
        if (!ssAssigned) {
            System.err.println("Cannot process event without a target state system"); //$NON-NLS-1$
            return;
        }

        /* Insert the event we're received into the events queue */
        ITmfEvent curEvent = event;
        try {
            eventsQueue.put(curEvent);
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
        TmfEvent ev = new EmptyEvent();

        try {
            eventsQueue.put(ev);
            while (!eventsQueue.isEmpty()) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------------
    // Inner classes
    // ------------------------------------------------------------------------

    /**
     * Empty event that should be totally ignored by the event handler. It can
     * by used for synchronisation purposes.
     */
    private class EmptyEvent extends TmfEvent {
        public EmptyEvent() {
            super(null, new TmfTimestamp(0), null, null, null, null);
        }
    }

    /**
     * This is the runner class for the second thread, which will take the
     * events from the queue and pass them through the state system.
     */
    private class EventProcessor implements Runnable {

        @Override
        public void run() {
            if (ss == null) {
                System.err.println("Cannot run event manager without assigning a target state system first!"); //$NON-NLS-1$
                return;
            }
            ITmfEvent event;

            try {
                event = eventsQueue.take();
                while (event.getTimestamp().getValue() != -1) {
                    if (event instanceof EmptyEvent) {
                        /* Synchronization event, should be ignored */
                        event = eventsQueue.take();
                        continue;
                    }

                    currentEvent = event;

                    /* Make sure this is an event the sub-class can process */
                    if (eventType.isInstance(event) && event.getType() != null) {
                        eventHandle(event);
                    }
                    event = eventsQueue.take();
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
            /* Close the History system, if there is one */
            if (currentEvent == null) {
                return;
            }
            try {
                ss.closeHistory(currentEvent.getTimestamp().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue());
            } catch (TimeRangeException e) {
                /*
                 * Since we're using currentEvent.getTimestamp, this shouldn't
                 * cause any problem
                 */
                e.printStackTrace();
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
