/*******************************************************************************
 * Copyright (c) 2012 Ericsson
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

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
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
public abstract class AbstractStateChangeInput implements IStateChangeInput {

    private static final int DEFAULT_EVENTS_QUEUE_SIZE = 10000;

    private final ITmfTrace trace;
    private final Class<? extends ITmfEvent> eventType;
    private final BlockingQueue<ITmfEvent> eventsQueue;
    private final Thread eventHandlerThread;

    private boolean ssAssigned;
    protected IStateSystemBuilder ss;
    private ITmfEvent currentEvent;

    /**
     * Instantiate a new state provider plugin.
     *
     * @param trace
     *            The LTTng 2.0 kernel trace directory
     * @param eventType
     *            The specific class for the event type that will be used within
     *            the subclass
     */
    public AbstractStateChangeInput(ITmfTrace trace, Class<? extends ITmfEvent> eventType) {
        this.trace = trace;
        this.eventType = eventType;
        eventsQueue = new ArrayBlockingQueue<ITmfEvent>(DEFAULT_EVENTS_QUEUE_SIZE);
        eventHandlerThread = new Thread(new EventProcessor(), "CTF Kernel Event Handler"); //$NON-NLS-1$
        ssAssigned = false;
    }

    @Override
    public ITmfTrace getTrace() {
        return trace;
    }

    @Override
    public long getStartTime() {
        return trace.getStartTime().getValue();
    }

    @Override
    public void assignTargetStateSystem(IStateSystemBuilder ssb) {
        ss = ssb;
        ssAssigned = true;
        eventHandlerThread.start();
    }

    @Override
    public void dispose() {
        /* Insert a null event in the queue to stop the event handler's thread. */
        try {
            eventsQueue.put(org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent.getNullEvent());
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
                    currentEvent = event;

                    /* Make sure this is an event the sub-class can process */
                    if (eventType.isInstance(event)) {
                        eventHandle(event);
                    }
                    event = eventsQueue.take();
                }
                /* We've received the last event, clean up */
                closeStateSystem();
                return;
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
                ss.closeHistory(currentEvent.getTimestamp().getValue());
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
