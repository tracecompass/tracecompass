/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceModelSignal;

/**
 * Signal handler for all the {@link TmfTraceModelSignal} handling
 *
 * @author Matthew Khouzam
 * @since 2.0
 *
 */
public interface ITraceContextSignalHandler {

    /**
     * Receive a signal and handle it
     *
     * @param signal
     *            signal to receive
     */
    default void receive(@NonNull TmfTraceModelSignal signal) {
        // do nothing
    }

}