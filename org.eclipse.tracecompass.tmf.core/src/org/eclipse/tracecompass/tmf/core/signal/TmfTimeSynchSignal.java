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

import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;

/**
 * A new time or time range selection has been made.
 *
 * This is the selected time or time range. To synchronize on the visible
 * (zoom) range, use {@link TmfRangeSynchSignal}.
 *
 * @author Francois Chouinard
*/
public class TmfTimeSynchSignal extends TmfSignal {

    private final ITmfTimestamp fBeginTime;
    private final ITmfTimestamp fEndTime;

    /**
     * Constructor
     *
     * @param source
     *            Object sending this signal
     * @param ts
     *            Timestamp of selection
     */
    public TmfTimeSynchSignal(Object source, ITmfTimestamp ts) {
        super(source);
        fBeginTime = ts;
        fEndTime = ts;
    }

    /**
     * Constructor
     *
     * @param source
     *            Object sending this signal
     * @param begin
     *            Timestamp of begin of selection range
     * @param end
     *            Timestamp of end of selection range
     */
    public TmfTimeSynchSignal(Object source, ITmfTimestamp begin, ITmfTimestamp end) {
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
        sb.append("[TmfTimeSynchSignal ("); //$NON-NLS-1$
        if (fBeginTime != null) {
            sb.append(fBeginTime.toString());
            if (!fBeginTime.equals(fEndTime) && fEndTime != null) {
                sb.append('-');
                sb.append(fEndTime.toString());
            }
        }
        sb.append(")]"); //$NON-NLS-1$
        return sb.toString();
    }

}
