/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
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

package org.eclipse.linuxtools.tmf.core.signal;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;

/**
 * A new time or time range selection has been made.
 *
 * This is the selected time or time range. To synchronize on the visible
 * (zoom) range, use {@link TmfRangeSynchSignal}.
 *
 * @version 1.0
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
     * @since 2.0
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
     * @since 2.1
     */
    public TmfTimeSynchSignal(Object source, ITmfTimestamp begin, ITmfTimestamp end) {
        super(source);
        fBeginTime = begin;
        fEndTime = end;
    }

    /**
     * @return The synchronization timestamp of this signal
     * @since 2.0
     * @deprecated As of 2.1, use {@link #getBeginTime()} and {@link #getEndTime()}
     */
    @Deprecated
    public ITmfTimestamp getCurrentTime() {
        return fBeginTime;
    }

    /**
     * @return The begin timestamp of selection
     * @since 2.1
     */
    public ITmfTimestamp getBeginTime() {
        return fBeginTime;
    }

    /**
     * @return The end timestamp of selection
     * @since 2.1
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
