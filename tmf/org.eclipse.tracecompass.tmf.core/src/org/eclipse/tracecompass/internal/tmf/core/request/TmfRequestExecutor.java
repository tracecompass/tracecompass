/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Added support for pre-emption
 *   Simon Delisle - Added scheduler for requests
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.request;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLogBuilder;
import org.eclipse.tracecompass.internal.tmf.core.TmfCoreTracer;
import org.eclipse.tracecompass.internal.tmf.core.component.TmfEventThread;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest.ExecutionType;

/**
 * The request scheduler works with 5 slots with a specific time. It has 4 slots
 * for foreground requests and 1 slot for background requests, and it passes
 * through all the slots (foreground first and background after).
 *
 * Example: if we have one foreground and one background request, the foreground
 * request will be executed four times more often than the background request.
 *
 * @author Francois Chouinard
 * @author Simon Delisle
 * @version 1.1
 */
public class TmfRequestExecutor implements Executor {

    private static final @NonNull Logger LOGGER = TraceCompassLog.getLogger(TmfRequestExecutor.class);
    private static final @NonNull String LOG_CATEGORY = "RequestExecutor"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final long REQUEST_TIME = 100;
    private static final int FOREGROUND_SLOT = 4;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The request executor
    private final ExecutorService fExecutor = Executors.newCachedThreadPool();
    private final String fExecutorName;

    // The request queues
    private final Queue<TmfEventThread> fForegroundTasks = new ArrayBlockingQueue<>(10);
    private final Queue<TmfEventThread> fBackgroundTasks = new ArrayBlockingQueue<>(10);

    // The tasks
    private TmfEventThread fActiveTask;

    private Timer fTimer;
    private TimerTask fTimerTask;

    private int fForegroundCycle = 0;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfRequestExecutor() {
        // We know the canonical name is not null because we use ExecutorService
        // only
        String canonicalName = checkNotNull(fExecutor.getClass().getCanonicalName());
        fExecutorName = canonicalName.substring(canonicalName.lastIndexOf('.') + 1);
        if (TmfCoreTracer.isComponentTraced()) {
            TmfCoreTracer.trace(fExecutor + " created"); //$NON-NLS-1$
        }
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the shutdown state (i.e. if it is accepting new requests)
     */
    public boolean isShutdown() {
        return fExecutor.isShutdown();
    }

    /**
     * @return the termination state
     */
    public boolean isTerminated() {
        return fExecutor.isTerminated();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Initialize the executor
     */
    public void init() {
        if (fTimer != null) {
            return;
        }
        // Initialize the timer for the schedSwitch
        fTimerTask = new SchedSwitch();
        fTimer = new Timer(true);
        fTimer.schedule(fTimerTask, 0, REQUEST_TIME);
    }

    @Override
    public synchronized void execute(final Runnable command) {

        // We are expecting MyEventThread:s
        if (!(command instanceof TmfEventThread)) {
            TraceCompassLogUtils.traceInstant(LOGGER, Level.WARNING, "RequestExecutor:NotATmfEventThread", "cmd", command.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        try (FlowScopeLog scope = new FlowScopeLogBuilder(LOGGER, Level.FINE, "RequestExecutor:CreatingThread").setCategory(LOG_CATEGORY).build()) { //$NON-NLS-1$
            // Wrap the thread in a MyThread
            TmfEventThread thread = (TmfEventThread) command;
            TmfEventThread wrapper = new TmfEventThread(thread) {
                @Override
                public void run() {
                    try (FlowScopeLog log = new FlowScopeLogBuilder(LOGGER, Level.FINE, "RequestExecutor:RunningRequest", "thread", thread.getThread(), "execution type", thread.getExecType()).setParentScope(scope).build()) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        thread.run();
                    } finally {
                        scheduleNext();
                    }
                }
            };

            // Add the thread to the appropriate queue
            ExecutionType priority = thread.getExecType();

            if (priority == ExecutionType.FOREGROUND) {
                if (!fForegroundTasks.offer(wrapper)) {
                    wrapper.cancel();
                }
            } else {
                if (!fBackgroundTasks.offer(wrapper)) {
                    wrapper.cancel();
                }
            }
        }
    }

    /**
     * Timer task to trigger scheduleNext()
     */
    private class SchedSwitch extends TimerTask {

        SchedSwitch() {
        }

        @Override
        public void run() {
            scheduleNext();
        }
    }

    /**
     * Executes the next pending request, if applicable.
     */
    protected synchronized void scheduleNext() {
        if (!isShutdown()) {
            if (fActiveTask == null) {
                schedule();
            } else if (fActiveTask.getExecType() == ExecutionType.FOREGROUND) {
                if (fActiveTask.getThread().isCompleted()) {
                    fActiveTask = null;
                    schedule();
                } else {
                    if (hasTasks()) {
                        fActiveTask.getThread().suspend();
                        if (!fForegroundTasks.offer(fActiveTask)) {
                            fActiveTask.cancel();
                            fActiveTask = null;
                        }
                        schedule();
                    }
                }

            } else if (fActiveTask.getExecType() == ExecutionType.BACKGROUND) {
                if (fActiveTask.getThread().isCompleted()) {
                    fActiveTask = null;
                    schedule();
                } else {
                    if (hasTasks()) {
                        fActiveTask.getThread().suspend();
                        if (!fBackgroundTasks.offer(fActiveTask)) {
                            fActiveTask.cancel();
                            fActiveTask = null;
                        }
                        schedule();
                    }
                }
            }
        }
    }

    /**
     * Stops the executor
     */
    public synchronized void stop() {
        if (fTimerTask != null) {
            fTimerTask.cancel();
        }

        if (fTimer != null) {
            fTimer.cancel();
        }

        if (fActiveTask != null) {
            fActiveTask.cancel();
        }

        while ((fActiveTask = fForegroundTasks.poll()) != null) {
            fActiveTask.cancel();
        }
        while ((fActiveTask = fBackgroundTasks.poll()) != null) {
            fActiveTask.cancel();
        }

        fExecutor.shutdown();
        if (TmfCoreTracer.isComponentTraced()) {
            TmfCoreTracer.trace(fExecutor + " terminated"); //$NON-NLS-1$
        }
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    /**
     * Determine which type of request (foreground or background) we schedule
     * next
     */
    private void schedule() {
        if (!fForegroundTasks.isEmpty()) {
            scheduleNextForeground();
        } else {
            scheduleNextBackground();
        }
    }

    /**
     * Schedule the next foreground request
     */
    private void scheduleNextForeground() {
        if (fForegroundCycle < FOREGROUND_SLOT || fBackgroundTasks.isEmpty()) {
            ++fForegroundCycle;
            fActiveTask = fForegroundTasks.poll();
            executefActiveTask();
        } else {
            fActiveTask = null;
            scheduleNextBackground();
        }
    }

    /**
     * Schedule the next background request
     */
    private void scheduleNextBackground() {
        fForegroundCycle = 0;
        if (!fBackgroundTasks.isEmpty()) {
            fActiveTask = fBackgroundTasks.poll();
            executefActiveTask();
        }
    }

    /**
     * Execute or resume the active task
     */
    private void executefActiveTask() {
        if (fActiveTask.getThread().isPaused()) {
            fActiveTask.getThread().resume();
        } else {
            fExecutor.execute(fActiveTask);
        }
    }

    /**
     * Check if the scheduler has tasks
     */
    private boolean hasTasks() {
        return !(fForegroundTasks.isEmpty() && fBackgroundTasks.isEmpty());
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return "[TmfRequestExecutor(" + fExecutorName + ")]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
