/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
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

package org.eclipse.tracecompass.tmf.core.signal;

import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Signal indicating a trace range has been updated.
 *
 * Receivers can safely perform event requests for the specified time range.
 * The signal acts as a trigger for coalescing such requests.
 *
 * @author Patrick Tasse
 */
public class TmfTraceRangeUpdatedSignal extends TmfSignal {

    private final ITmfTrace fTrace;
    private final TmfTimeRange fTimeRange;

    /**
     * Constructor
     *
     * @param source
     *            Object sending this signal
     * @param trace
     *            Trace whose range was updated
     * @param range
     *            The new time range of the trace
     */
    public TmfTraceRangeUpdatedSignal(Object source, ITmfTrace trace, TmfTimeRange range) {
        super(source);
        fTrace = trace;
        fTimeRange = range;
    }

    /**
     * @return The trace
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * @return The time range
     */
    public TmfTimeRange getRange() {
        return fTimeRange;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "[TmfTraceRangeUpdatedSignal (" + fTrace.getName() + ", " + fTimeRange.toString() + ")]";
    }

}
