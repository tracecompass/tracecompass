/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Added support for pre-emption
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.request;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.linuxtools.internal.tmf.core.TmfCoreTracer;
import org.eclipse.linuxtools.internal.tmf.core.component.TmfEventThread;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest.ExecutionType;

/**
 * A simple, straightforward request executor.
 *
 * @author Francois Chouinard
 * @version 1.1
 */
public class TmfRequestExecutor implements Executor {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The request executor
    private final ExecutorService fExecutor = Executors.newFixedThreadPool(2);
    private final String fExecutorName;

    // The request queues
    private final Queue<TmfEventThread> fHighPriorityTasks = new ArrayBlockingQueue<TmfEventThread>(100);
    private final Queue<TmfEventThread> fLowPriorityTasks = new ArrayBlockingQueue<TmfEventThread>(100);

    // The tasks
    private TmfEventThread fActiveTask;
    private TmfEventThread fSuspendedTask;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfRequestExecutor() {
        String canonicalName = fExecutor.getClass().getCanonicalName();
        fExecutorName = canonicalName.substring(canonicalName.lastIndexOf('.') + 1);
        if (TmfCoreTracer.isComponentTraced()) {
            TmfCoreTracer.trace(fExecutor + " created"); //$NON-NLS-1$
        }
    }

    /**
     * Standard constructor
     *
     * @param executor
     *            The executor service to use
     */
    @Deprecated
    public TmfRequestExecutor(ExecutorService executor) {
        this();
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the number of pending requests
     */
    @Deprecated
    public synchronized int getNbPendingRequests() {
        return fHighPriorityTasks.size() + fLowPriorityTasks.size();
    }

    /**
     * @return the shutdown state (i.e. if it is accepting new requests)
     */
    public synchronized boolean isShutdown() {
        return fExecutor.isShutdown();
    }

    /**
     * @return the termination state
     */
    public synchronized boolean isTerminated() {
        return fExecutor.isTerminated();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public synchronized void execute(final Runnable command) {

        // We are expecting MyEventThread:s
        if (!(command instanceof TmfEventThread)) {
            // TODO: Log an error
            return;
        }

        // Wrap the thread in a MyThread
        TmfEventThread thread = (TmfEventThread) command;
        TmfEventThread wrapper = new TmfEventThread(thread) {
            @Override
            public void run() {
                try {
                    command.run();
                } finally {
                    scheduleNext();
                }
            }
        };

        // Add the thread to the appropriate queue
        ExecutionType priority = thread.getExecType();
        (priority == ExecutionType.FOREGROUND ? fHighPriorityTasks : fLowPriorityTasks).offer(wrapper);

        // Schedule or preempt as appropriate
        if (fActiveTask == null) {
            scheduleNext();
        } else if (priority == ExecutionType.FOREGROUND && priority != fActiveTask.getExecType()) {
            fActiveTask.getThread().suspend();
            fSuspendedTask = fActiveTask;
            scheduleNext();
        }
    }

    /**
     * Executes the next pending request, if applicable.
     */
    protected synchronized void scheduleNext() {
        if (!isShutdown()) {
            if ((fActiveTask = fHighPriorityTasks.poll()) != null) {
                fExecutor.execute(fActiveTask);
            } else if (fSuspendedTask != null) {
                fActiveTask = fSuspendedTask;
                fSuspendedTask = null;
                fActiveTask.getThread().resume();
            } else if ((fActiveTask = fLowPriorityTasks.poll()) != null) {
                fExecutor.execute(fActiveTask);
            }
        }
    }

    /**
     * Stops the executor
     */
    public synchronized void stop() {
        if (fActiveTask != null) {
            fActiveTask.cancel();
        }

        while ((fActiveTask = fHighPriorityTasks.poll()) != null) {
            fActiveTask.cancel();
        }

        fExecutor.shutdown();
        if (TmfCoreTracer.isComponentTraced()) {
            TmfCoreTracer.trace(fExecutor + " terminated"); //$NON-NLS-1$
        }
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "[TmfRequestExecutor(" + fExecutorName + ")]";
    }

}
