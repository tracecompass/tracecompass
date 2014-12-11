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
 *   Patrick Tasse - Deprecate current time
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.signal;

import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;

/**
 * A new time range has been selected.
 *
 * This is the visible (zoom) time range. To synchronize on the selection range,
 * use {@link TmfTimeSynchSignal}.
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfRangeSynchSignal extends TmfSignal {

    private final TmfTimeRange fCurrentRange;

    /**
     * Constructor
     *
     * @param source
     *            Object sending this signal
     * @param range
     *            The new time range
     * @since 2.1
     */
    public TmfRangeSynchSignal(Object source, TmfTimeRange range) {
        super(source);
        fCurrentRange = range;
    }

    /**
     * @return This signal's time range
     * @since 2.0
     */
    public TmfTimeRange getCurrentRange() {
        return fCurrentRange;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TmfRangeSynchSignal [source="); //$NON-NLS-1$

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
