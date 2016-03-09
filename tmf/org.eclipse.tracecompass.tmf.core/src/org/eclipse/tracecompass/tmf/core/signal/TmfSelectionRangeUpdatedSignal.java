/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Support selection range
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.signal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;

/**
 * A new time range selection has been made.
 *
 * This is the selected time or time range. A single-timestamp selection is
 * represented by a range where the start time is equal to the end time.
 *
 * To update the visible (zoom) range instead, use
 * {@link TmfWindowRangeUpdatedSignal}.
 *
 * @author Francois Chouinard
 * @since 1.0
 */
@NonNullByDefault
public class TmfSelectionRangeUpdatedSignal extends TmfSignal {

    private final ITmfTimestamp fBeginTime;
    private final ITmfTimestamp fEndTime;

    /**
     * Constructor for a single timestamp selection (start and end times will be
     * the same).
     *
     * @param source
     *            Object sending this signal
     * @param ts
     *            Timestamp of selection
     */
    public TmfSelectionRangeUpdatedSignal(@Nullable Object source, ITmfTimestamp ts) {
        super(source);
        fBeginTime = ts;
        fEndTime = ts;
    }

    /**
     * Constructor for a time range selection.
     *
     * @param source
     *            Object sending this signal
     * @param begin
     *            Timestamp of begin of selection range
     * @param end
     *            Timestamp of end of selection range
     */
    public TmfSelectionRangeUpdatedSignal(@Nullable Object source, ITmfTimestamp begin, ITmfTimestamp end) {
        super(source);
        fBeginTime = begin;
        fEndTime = end;
    }

    /**
     * @return The begin timestamp of selection
     */
    public ITmfTimestamp getBeginTime() {
        return fBeginTime;
    }

    /**
     * @return The end timestamp of selection
     */
    public ITmfTimestamp getEndTime() {
        return fEndTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" ["); //$NON-NLS-1$
        sb.append(fBeginTime.toString());
        if (!fBeginTime.equals(fEndTime)) {
            sb.append('-');
            sb.append(fEndTime.toString());
        }
        sb.append("]"); //$NON-NLS-1$
        return sb.toString();
    }

}
