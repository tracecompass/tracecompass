/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexandre Montplaisir - Initial API and implementation
 *     Patrick Tasse - Update queue handling
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Display;

/**
 * This handler offers "coalescing" of UI updates.
 *
 * When displaying live experiments containing a high number of traces, every
 * trace will want to regularly update views with their new available data. This
 * can cause a high number of threads calling {@link Display#asyncExec}
 * repeatedly, which can really impede UI responsiveness.
 * <p>
 * Instead of calling {@link Display#asyncExec} directly, threads that want to
 * queue updates to the UI can instead call
 * {@link TmfUiRefreshHandler#queueUpdate}. If the handler is not currently
 * executing another update it will be scheduled immediately. Otherwise the
 * update will be queued.
 * <p>
 * The handler will only execute one update at a time. While it is busy, new
 * requests received from a source that is already in the queue will replace the
 * previous one (as we assume the latest UI update request is the most
 * up-to-date and interesting one), preserving the previous request order. New
 * requests received from other sources will be added to the end of the queue
 * (keeping only the latest request from each source).
 * <p>
 * Once the current update is completed, the oldest request in the queue will be
 * sent to the UI thread via one single call to {@link Display#syncExec}.
 *
 * @author Alexandre Montplaisir
 * @since 3.1
 */
public class TmfUiRefreshHandler {

    /** Singleton instance */
    private static TmfUiRefreshHandler fInstance = null;

    private final Map<Object, Runnable> fUpdates = new LinkedHashMap<>();
    private Thread fCurrentTask;


    /**
     * Internal constructor
     */
    private TmfUiRefreshHandler() {
        fCurrentTask = null;
    }

    /**
     * Get the handler's instance
     *
     * @return The singleton instance
     */
    public static synchronized TmfUiRefreshHandler getInstance() {
        if (fInstance == null) {
            fInstance = new TmfUiRefreshHandler();
        }
        return fInstance;
    }

    /**
     * Cancel all current requests and dispose the handler.
     */
    public synchronized void dispose() {
        fUpdates.clear();
        fCurrentTask = null;
    }

    /**
     * Queue a UI update. Threads that want to benefit from "UI coalescing"
     * should send their {@link Runnable} to this method, instead of
     * {@link Display#asyncExec(Runnable)}.
     *
     * @param source
     *            The source sending the request. Typically callers should use
     *            "this". When multiple requests are queued before being
     *            executed, only the latest request per source is actually sent.
     * @param task
     *            The {@link Runnable} to execute in the UI thread.
     */
    public synchronized void queueUpdate(Object source, Runnable task) {
        fUpdates.put(source, task);
        if (fCurrentTask == null) {
            fCurrentTask = new RunAllUpdates();
            fCurrentTask.start();
        }
    }

    /**
     * Task to empty the update queue, and send each task to the UI thread.
     */
    private class RunAllUpdates extends Thread {
        @Override
        public void run() {
            while (true) {
                Runnable nextTask = null;
                synchronized (TmfUiRefreshHandler.this) {
                    if (!fUpdates.isEmpty()) {
                        Object firstKey = fUpdates.keySet().iterator().next();
                        nextTask = fUpdates.get(firstKey);
                        fUpdates.remove(firstKey);
                    }
                    if (nextTask == null) {
                        /*
                         * No updates remaining in the queue, put fCurrentTask
                         * back to null so that a new task can be scheduled.
                         */
                        fCurrentTask = null;
                        break;
                    }
                }
                Display.getDefault().syncExec(nextTask);
            }
        }
    }
}
