/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.signal;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.linuxtools.tmf.core.component.ITmfComponent;
import org.eclipse.linuxtools.tmf.core.component.TmfComponent;

/**
 * "Buffer" between a TmfComponent and the signal manager. You can use this if
 * you want to throttle the amount of signals your component will send.
 *
 * It works by specifying a delay, then calling {@link #queue}. The signals will
 * only be really sent if no other call to {@link #queue} happens within $delay
 * milliseconds afterwards. This guarantees that only the *last* signal is
 * actually broadcasted.
 *
 * Note that this class does not discriminate for signal types, sources, or
 * whatever. If you want to throttle different signals in different ways, you
 * can use multiple signal throttlers in your component and call them
 * accordingly.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class TmfSignalThrottler {

    private final ITmfComponent fComponent;
    private final long fDelay;
    private final Timer fTimer;
    private TimerTask fCurrentTask;

    /**
     * Constructor
     *
     * @param component
     *            The source component of the signals
     * @param delay
     *            Time to wait before actually sending signals (in ms)
     */
    public TmfSignalThrottler(ITmfComponent component, long delay) {
        this.fComponent = component;
        this.fDelay = delay;
        this.fTimer = new Timer();

        /*
         * Initialize currentTask to something, so we don't have to do a null
         * check every time
         */
        fCurrentTask = new TimerTask() { @Override public void run() {} };
    }

    /**
     * Queue a signal for sending. It will only be forward to the centralized
     * signal handler if 'delay' elapses without another signal being sent
     * through this method.
     *
     * You call this instead of calling {@link TmfComponent#broadcast}.
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
            fComponent.broadcast(signal);
        }
    }
}
