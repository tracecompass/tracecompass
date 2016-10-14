/*******************************************************************************
 * Copyright (c) 2013, 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.analysis;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Stub test provider for test state system analysis module
 *
 * @author Geneviève Bastien
 */
public class TestStateSystemProvider extends AbstractTmfStateProvider {

    /**
     * This interface allows unit tests to provide only the event handling part
     * of the state provider, without having to extend the analysis and the
     * classes
     */
    @FunctionalInterface
    public static interface TestStateProviderHandler {
        /**
         * Handles the event
         *
         * @param ss
         *            The state system builder
         * @param event
         *            The event to handler
         * @return <code>true</code> if everything went fine, or <code>false</code> to cancel
         */
        boolean eventHandle(@NonNull ITmfStateSystemBuilder ss, ITmfEvent event);
    }

    private static final int VERSION = 1;
    private static final String fString = "[]";
    private static int fCount = 0;
    private static final @NonNull TestStateProviderHandler DEFAULT_HANDLER = (ss, event) -> {
        /* Just need something to fill the state system */
        if (fString.equals(event.getContent().getValue())) {
            try {
                int quarkId = ss.getQuarkAbsoluteAndAdd("String");
                int quark = ss.getQuarkRelativeAndAdd(quarkId, fString);
                ss.modifyAttribute(event.getTimestamp().getValue(), TmfStateValue.newValueInt(fCount++), quark);
            } catch (TimeRangeException | StateValueTypeException e) {

            }
        }
        return true;
    };
    private static @NonNull TestStateProviderHandler sfHandler = DEFAULT_HANDLER;

    /**
     * Set the event handler for the state provider
     *
     * @param handler
     *            The class containing the event handler for this state provider
     */
    public static void setEventHandler(TestStateProviderHandler handler) {
        if (handler == null) {
            sfHandler = DEFAULT_HANDLER;
            return;
        }
        sfHandler = handler;
    }

    private final Lock fLock = new ReentrantLock();
    private @Nullable Condition fNextEventSignal = null;

    /**
     * Constructor
     *
     * @param trace
     *            The trace this state provider is for
     */
    public TestStateSystemProvider(@NonNull ITmfTrace trace) {
        super(trace, "Stub State System");
    }

    /**
     * Constructor
     *
     * @param trace
     *            The trace this state provider is for
     * @param size
     *            The size of the queue, a.k.a the number of chunks that fit
     *            into the buffered queue.
     * @param chunk
     *            the size of a events that fit inside a chunk of the queue
     */
    public TestStateSystemProvider(@NonNull ITmfTrace trace, int size, int chunk) {
        super(trace, "Stub State System", size, chunk);
    }

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public ITmfStateProvider getNewInstance() {
        return new TestStateSystemProvider(this.getTrace());
    }

    @Override
    protected void eventHandle(ITmfEvent event) {
        ITmfStateSystemBuilder ss = checkNotNull(getStateSystemBuilder());
        sfHandler.eventHandle(ss, event);
    }

    @Override
    public void processEvent(@NonNull ITmfEvent event) {
        fLock.lock();
        try {
            Condition cond = fNextEventSignal;
            if (cond != null) {
                cond.await();
            }
        } catch (InterruptedException e) {

        } finally {
            super.processEvent(event);
            fLock.unlock();
        }
    }

    /**
     * Set the processing of event to be one event at a time instead of the
     * default behavior. It will block until the next call to
     * {@link #signalNextEvent()} method call.
     *
     * @param throttleEvent
     *            Whether to wait for a signal to process the next event
     */
    public void setThrottling(boolean throttleEvent) {
        fLock.lock();
        try {
            if (throttleEvent) {
                Condition cond = fNextEventSignal;
                // If called for the first time, create a condition
                if (cond == null) {
                    cond = fLock.newCondition();
                    fNextEventSignal = cond;
                }

            } else {
                Condition cond = fNextEventSignal;
                if (cond != null) {
                    fNextEventSignal = null;
                    cond.signalAll();
                }
            }
        } finally {
            fLock.unlock();
        }

    }

    /**
     * Signal for the next event to be processed. Calling this method makes
     * sense only if {@link #setThrottling(boolean)} has been set to true
     */
    public void signalNextEvent() {
        fLock.lock();
        try {
            Condition cond = fNextEventSignal;
            if (cond != null) {
                cond.signalAll();
            }
        } finally {
            fLock.unlock();
        }
    }

}
