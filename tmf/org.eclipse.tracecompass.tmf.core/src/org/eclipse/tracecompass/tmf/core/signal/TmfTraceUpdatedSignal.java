/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.signal;

import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Signal indicating a trace has been updated.
 *
 * The trace has been indexed up to the specified range.
 *
 * @author Francois Chouinard
 */
public class TmfTraceUpdatedSignal extends TmfSignal {

    private final ITmfTrace fTrace;
    private final TmfTimeRange fTimeRange;
    private final long fNbEvents;

    /**
     * Constructor
     *
     * @param source
     *            Object sending this signal
     * @param trace
     *            The trace that was updated
     * @param range
     *            The new time range of the trace
     * @param nbEvents
     *            The number of events in the trace
     */
    public TmfTraceUpdatedSignal(Object source, ITmfTrace trace, TmfTimeRange range, long nbEvents) {
        super(source);
        fTrace = trace;
        fTimeRange = range;
        fNbEvents = nbEvents;
    }

    /**
     * @return The trace referred to by this signal
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * @return The time range indicated by this signal
     */
    public TmfTimeRange getRange() {
        return fTimeRange;
    }

    /**
     * Returns the number of events indicated by this signal
     *
     * @return the number of events indicated by this signal
     */
    public long getNbEvents() {
        return fNbEvents;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "[TmfTraceUpdatedSignal (" + fTrace.toString() + ", "
                + fTimeRange.toString() + ")]";
    }

}
