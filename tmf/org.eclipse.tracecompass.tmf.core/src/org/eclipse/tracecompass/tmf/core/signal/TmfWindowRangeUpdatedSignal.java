/*******************************************************************************
 * Copyright (c) 2009, 2017 Ericsson
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
 *   Patrick Tasse - Deprecate current time
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.signal;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;

/**
 * A new range has been selected for the visible (zoom) time range.
 *
 * To update the selection range instead, use
 * {@link TmfSelectionRangeUpdatedSignal}.
 *
 * @author Francois Chouinard
 * @since 1.0
 */
public class TmfWindowRangeUpdatedSignal extends TmfSignal {

    private final TmfTimeRange fCurrentRange;
    private final @Nullable ITmfTrace fTrace;

    /**
     * Constructor
     *
     * @param source
     *            Object sending this signal
     * @param range
     *            The new time range
     */
    public TmfWindowRangeUpdatedSignal(Object source, TmfTimeRange range) {
        this(source, range, TmfTraceManager.getInstance().getActiveTrace());
    }

    /**
     * Constructor
     *
     * @param source
     *            Object sending this signal
     * @param range
     *            The new time range
     * @param trace
     *            The trace that triggered the window range update, or null
     * @since 3.2
     */
    public TmfWindowRangeUpdatedSignal(Object source, TmfTimeRange range, ITmfTrace trace) {
        super(source);
        fCurrentRange = range;
        fTrace = trace;
    }

    /**
     * @return This signal's time range
     */
    public TmfTimeRange getCurrentRange() {
        return fCurrentRange;
    }

    /**
     * Gets the trace that triggered the window range update
     *
     * @return The trace, or null
     * @since 3.2
     */
    public @Nullable ITmfTrace getTrace() {
        return fTrace;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append(" [source="); //$NON-NLS-1$

        if (getSource() != null) {
            sb.append(getSource().toString());
        } else {
            sb.append("null"); //$NON-NLS-1$
        }

        sb.append(", range="); //$NON-NLS-1$

        if (fCurrentRange != null) {
            sb.append(fCurrentRange.toString());
        } else {
            sb.append("null"); //$NON-NLS-1$
        }
        sb.append(']');
        return sb.toString();
    }
}
