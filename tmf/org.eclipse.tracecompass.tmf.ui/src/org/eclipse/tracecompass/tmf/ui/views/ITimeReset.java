/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views;

import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;

/**
 * Interface to be implemented by classes who can reset their time ranges to
 * some values, for instance the range of the trace.
 *
 * @author Matthew Khouzam
 * @since 3.2
 */
public interface ITimeReset {

    /**
     * Reset the start and end times and broadcast the signal to all classes with
     * {@link TmfSignalHandler} for {@link TmfWindowRangeUpdatedSignal}
     */
    default void resetStartFinishTime() {
        resetStartFinishTime(true);
    }

    /**
     * Reset the start and end times.
     *
     * @param broadcast
     *            if true, broadcast the signal, else only apply to this view
     */
    void resetStartFinishTime(boolean broadcast);

}
