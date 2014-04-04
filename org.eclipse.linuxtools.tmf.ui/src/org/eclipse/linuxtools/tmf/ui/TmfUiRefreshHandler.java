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
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.linuxtools.tmf.core.component.ITmfComponent;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.ImmutableList;

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
 * {@link TmfUiRefreshHandler#queueUpdate}. This will schedule a UI update,
 * which will happen after *at least* {@link #UPDATE_PERIOD} milliseconds.
 * <ul><li>
 * During that time, new requests received from the same component will replace
 * the previous one (as we assume the latest UI update request is the most
 * up-to-date and interesting one).
 * </li><li>
 * Requests received from other components will be added to the queue (keeping
 * only the latest request from each component), and once the timeout expires,
 * they will all be sent to the UI thread via one single call to
 * {@link Display#syncExec}.
 * </li></ul>
 *
 * @author Alexandre Montplaisir
 * @since 3.1
 */
public class TmfUiRefreshHandler {

    /** Throttle update requests to this amount of ms */
    public static final long UPDATE_PERIOD = 1000;

    /** Singleton instance */
    private static TmfUiRefreshHandler fInstance = null;

    private final Map<ITmfComponent, Runnable> fUpdates = new HashMap<>();
    private final Timer fTimer;
    private TimerTask fCurrentTask;


    /**
     * Internal constructor
     */
    private TmfUiRefreshHandler() {
        fTimer = new Timer();
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
        fCurrentTask = null;
        fTimer.cancel();
        fTimer.purge();
    }

    /**
     * Queue a UI update. Threads that want to benefit from "UI coalescing"
     * should send their {@link Runnable} to this method, instead of
     * {@link Display#asyncExec(Runnable)}.
     *
     * @param source
     *            The component sending the request. Typically callers should
     *            use "this". Only the latest request per component is actually
     *            sent.
     * @param task
     *            The {@link Runnable} to execute in the UI thread.
     */
    public synchronized void queueUpdate(ITmfComponent source, Runnable task) {
        fUpdates.put(source, task);
        if (fCurrentTask == null) {
            fCurrentTask = new RunAllUpdates();
            fTimer.schedule(fCurrentTask, UPDATE_PERIOD);
        }
    }

    /**
     * Task to empty the current map of updates, and send them to the UI thread.
     */
    private class RunAllUpdates extends TimerTask {
        @Override
        public void run() {
            /* Extract the currently-queued tasks in a local variable */
            final Collection<Runnable> updates;
            synchronized (TmfUiRefreshHandler.this) {
                updates = ImmutableList.copyOf(fUpdates.values());
                fUpdates.clear();
            }
            /*
             * Release the lock on "this" before we wait on the UI thread below.
             * This is to prevent deadlocks in case components send their
             * "queueUpdate()" via the UI thread.
             */
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    for (Runnable update : updates) {
                        update.run();
                    }
                }
            });

            synchronized (TmfUiRefreshHandler.this) {
                if (fUpdates.isEmpty()) {
                    /*
                     * No updates were queued in the meantime, put fCurrentTask
                     * back to null so that a new task can be scheduled.
                     */
                    fCurrentTask = null;
                } else {
                    /*
                     * Updates were queued during the syncExec, schedule a new
                     * task to eventually run them.
                     */
                    fCurrentTask = new RunAllUpdates();
                    fTimer.schedule(fCurrentTask, UPDATE_PERIOD);
                }
            }
        }
    }
}
