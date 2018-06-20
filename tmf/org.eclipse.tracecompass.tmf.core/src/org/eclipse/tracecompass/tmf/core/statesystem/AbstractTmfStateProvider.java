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

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.collect.BufferedBlockingQueue;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.annotations.VisibleForTesting;

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

    private static final class InitialValue {
        private final long fTime;
        private final @Nullable Object fValue;
        private final int fQuark;

        public InitialValue(long time, @Nullable Object initialState, int quark) {
            fTime = time;
            fValue = initialState;
            fQuark = quark;
        }

        public long getTime() {
            return fTime;
        }
    }

    private static final int DEFAULT_EVENTS_QUEUE_SIZE = 127;
    private static final int DEFAULT_EVENTS_CHUNK_SIZE = 127;

    private final ITmfTrace fTrace;
    private final BufferedBlockingQueue<ITmfEvent> fEventsQueue;
    private final Thread fEventHandlerThread;

    private boolean fStateSystemAssigned;
    /** State system in which to insert the state changes */
    private @Nullable ITmfStateSystemBuilder fSS = null;
    private @Nullable Throwable fFailureCause = null;

    /* The last safe time at which this state provider can be queried */
    private volatile long fSafeTime;

    /*
     * An exception propagation runnable. If an exception occurred in Event
     * Processor thread, this field should be updated so that the "main" thread
     * will propagate the exception
     */
    private Runnable fPropagateExceptions = () -> {
        // Do nothing, a new Runnable will be defined if exceptions occur in the
        // threads
    };

    private final Queue<InitialValue> fInitialValues = new PriorityQueue<>(Comparator.comparingLong(InitialValue::getTime));

    /**
     * Instantiate a new state provider plugin.
     *
     * @param trace
     *            The LTTng 2.0 kernel trace directory
     * @param id
     *            Name given to this state change input. Only used internally.
     */
    public AbstractTmfStateProvider(ITmfTrace trace, String id) {
        this(trace, id, DEFAULT_EVENTS_QUEUE_SIZE, DEFAULT_EVENTS_CHUNK_SIZE);
    }

    /**
     * Instantiate a new state provider. This constructor allows to fine-tune
     * the size of the event processing queue. This can be useful to unit tests
     * various situations.
     *
     * @param trace
     *            The trace
     * @param id
     *            Name given to this state change input. Only used internally.
     * @param queueSize
     *            The size of the queue, a.k.a the number of chunks that fit
     *            into the buffered queue.
     * @param chunkSize
     *            The number of events that fit inside a single chunk of the
     *            queue
     * @since 2.3
     */
    @VisibleForTesting
    protected AbstractTmfStateProvider(ITmfTrace trace, String id, int queueSize, int chunkSize) {
        if (queueSize <= 0 || chunkSize <= 0) {
            throw new IllegalArgumentException("Cannot have negative sized buffer" + //$NON-NLS-1$
                    formatError("queueSize", queueSize) + //$NON-NLS-1$
                    formatError("chunkSize", chunkSize)); //$NON-NLS-1$
        }
        fTrace = trace;
        fEventsQueue = new BufferedBlockingQueue<>(queueSize, chunkSize);
        fStateSystemAssigned = false;
        // set the safe time to before the trace start, the analysis has not yet
        // started
        fSafeTime = trace.getStartTime().toNanos() - 1;
        fEventHandlerThread = new Thread(() -> SafeRunner.run(new EventProcessor()), id + " Event Handler"); //$NON-NLS-1$
    }

    private static String formatError(String name, int value) {
        return (value <= 0) ? " " + name + " = " + value : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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

    /**
     * @since 2.0
     */
    @Override
    public long getLatestSafeTime() {
        return fSafeTime;
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
        /*
         * Insert a null event in the queue to stop the event handler's thread.
         */
        try {
            fEventsQueue.put(END_EVENT);
            fEventsQueue.flushInputBuffer();
            fEventHandlerThread.join();
        } catch (InterruptedException e) {
            Activator.logError("Error disposing state provider", e); //$NON-NLS-1$
        }
        fStateSystemAssigned = false;
        fSS = null;
    }

    @Override
    public void processEvent(ITmfEvent event) {
        /* Make sure the target state system has been assigned */
        if (!fStateSystemAssigned) {
            throw new IllegalStateException("Cannot process event without a target state system. ID: " + getClass().getSimpleName()); //$NON-NLS-1$
        }
        fPropagateExceptions.run();

        /* Insert the event we're received into the events queue */
        ITmfEvent curEvent = event;
        fEventsQueue.put(curEvent);
    }

    /**
     * @since 3.0
     */
    @Override
    public void fail(Throwable cause) {
        fFailureCause = cause;
    }

    /**
     * @since 3.0
     */
    @Override
    public @Nullable Throwable getFailureCause() {
        return fFailureCause;
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

    /**
     * Fake event indicating the build is over, and the provider should close
     */
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
    private class EventProcessor implements ISafeRunnable {

        private @Nullable ITmfEvent currentEvent;
        private boolean fDone = false;

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
            ITmfEvent event = fEventsQueue.take();
            /* This is a singleton, we want to do != instead of !x.equals */
            while (event != END_EVENT) {
                if (event == EMPTY_QUEUE_EVENT) {
                    /* Synchronization event, should be ignored */
                    event = fEventsQueue.take();
                    continue;
                }
                currentEvent = event;
                long currentTime = event.getTimestamp().toNanos();
                fSafeTime = currentTime - 1;
                ITmfStateSystemBuilder stateSystemBuilder = getStateSystemBuilder();
                if (stateSystemBuilder == null) {
                    return;
                }
                InitialValue initialValue = fInitialValues.peek();
                while (initialValue != null && (currentTime > initialValue.fTime)) {
                    initialValue = fInitialValues.poll();
                    if (initialValue != null) {
                        stateSystemBuilder.modifyAttribute(initialValue.fTime, initialValue.fValue, initialValue.fQuark);
                    }
                }
                eventHandle(event);
                event = fEventsQueue.take();
            }
            fDone = true;
            /*
             * flush remaining states
             */
            ITmfStateSystemBuilder stateSystemBuilder = getStateSystemBuilder();
            if (stateSystemBuilder == null) {
                return;
            }
            while (!fInitialValues.isEmpty()) {
                InitialValue interval = fInitialValues.remove();
                stateSystemBuilder.modifyAttribute(interval.fTime, interval.fValue, interval.fQuark);
            }
            /* We've received the last event, clean up */
            done();
            closeStateSystem();
        }

        private void closeStateSystem() {
            ITmfEvent event = currentEvent;
            final long endTime = (event == null) ? 0 : event.getTimestamp().toNanos();

            if (fSS != null) {
                fSS.closeHistory(endTime);
            }
        }

        @Override
        public void handleException(@Nullable Throwable exception) {
            // Update the propagation runnable
            final RuntimeException rException = (exception instanceof RuntimeException) ? (RuntimeException) exception : new RuntimeException("Error in threaded state history backend", exception); //$NON-NLS-1$
            fail(rException);
            fPropagateExceptions = () -> {
                // This exception should be caught by the thread that does the
                // insertions and trigger the cancellation mechanism
                throw rException;
            };
            if (fDone) {
                /*
                 * The last event was already processed, the exception was
                 * thrown from the closing of the state system, just return
                 */
                return;
            }
            /* drain */
            ITmfEvent event = fEventsQueue.take();
            while (event != END_EVENT) {
                if (event == EMPTY_QUEUE_EVENT) {
                    /* Synchronization event, should be ignored */
                    event = fEventsQueue.take();
                    continue;
                }
                event = fEventsQueue.take();
            }

            /* We've received the last event, clean up */
            closeStateSystem();
        }
    }

    @Override
    public void addFutureEvent(long time, @Nullable Object initialState, int attribute) {
        fInitialValues.add(new InitialValue(time, initialState, attribute));
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
