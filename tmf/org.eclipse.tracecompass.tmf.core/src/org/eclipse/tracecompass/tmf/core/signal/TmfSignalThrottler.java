/*******************************************************************************
 * Copyright (c) 2013, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.signal;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.component.ITmfComponent;

/**
 * "Buffer" between a TmfComponent and the signal manager. You can use this if
 * you want to throttle the amount of signals your component will send.
 * <p>
 * It works by specifying a delay, then calling {@link #queue}. The signals will
 * only be really sent if no other call to {@link #queue} happens within $delay
 * milliseconds afterwards. This guarantees that only the *last* signal is
 * actually broadcasted.
 * <p>
 * Note that this class does not discriminate for signal types, sources, or
 * whatever. If you want to throttle different signals in different ways, you
 * can use multiple signal throttlers in your component and call them
 * accordingly.
 * <p>
 * This throttler will broadcast the signal from a TimerThread.
 *
 * @author Alexandre Montplaisir
 */
@NonNullByDefault
public class TmfSignalThrottler {

    private final @Nullable ITmfComponent fComponent;
    private final long fDelay;
    private final Timer fTimer;

    private TimerTask fCurrentTask;

    /**
     * Constructor
     *
     * @param component
     *            The optional source component of the signals. If non-null, its
     *            {@link ITmfComponent#broadcast} method will be used to finally
     *            send the signal. If null, the generic
     *            {@link TmfSignalManager#dispatchSignal} is used.
     * @param delay
     *            Time to wait before actually sending signals (in ms)
     */
    public TmfSignalThrottler(@Nullable ITmfComponent component, long delay) {
        this.fComponent = component;
        this.fDelay = delay;
        this.fTimer = new Timer();

        /*
         * Initialize currentTask to something, so we don't have to do a null
         * check every time.
         */
        fCurrentTask = new TimerTask() {
            @Override
            public void run() {
                // Do nothing
            }
        };
    }

    /**
     * Queue a signal for sending. It will only be forward to the centralized
     * signal handler if 'delay' elapses without another signal being sent
     * through this method.
     *
     * You call this instead of {@link ITmfComponent#broadcast} or
     * {@link TmfSignalManager#dispatchSignal}.
     *
     * @param signal
     *            The signal to queue for broadcasting
     */
    public synchronized void queue(TmfSignal signal) {
        fCurrentTask.cancel();
        fCurrentTask = new BroadcastRequest(signal);
        fTimer.schedule(fCurrentTask, fDelay);
    }

    /**
     * Dispose method. Will prevent any pending signal from being sent, and this
     * throttler from be used again.
     */
    public synchronized void dispose() {
        fTimer.cancel();
        fTimer.purge();
    }

    private class BroadcastRequest extends TimerTask {

        private final TmfSignal signal;

        BroadcastRequest(TmfSignal signal) {
            this.signal = signal;
        }

        @Override
        public void run() {
            dispatchSignal(signal);
        }
    }

    /**
     * Dispatch the signal
     *
     * @param signal
     *            the signal
     * @since 5.0
     */
    protected void dispatchSignal(TmfSignal signal) {
        if (fComponent != null) {
            fComponent.broadcast(signal);
        } else {
            TmfSignalManager.dispatchSignal(signal);
        }
    }
}
