/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.signal;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.tmf.core.component.ITmfComponent;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalThrottler;

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
 * Unlike {@link TmfSignalThrottler}, this throttler will broadcast
 * the signal from the UI thread.
 *
 * @since 5.0
 */
public class TmfUiSignalThrottler extends TmfSignalThrottler {

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
    public TmfUiSignalThrottler(@Nullable ITmfComponent component, long delay) {
        super(component, delay);
    }

    @Override
    protected void dispatchSignal(@NonNull TmfSignal signal) {
        Display.getDefault().asyncExec(() -> super.dispatchSignal(signal));
    }
}
