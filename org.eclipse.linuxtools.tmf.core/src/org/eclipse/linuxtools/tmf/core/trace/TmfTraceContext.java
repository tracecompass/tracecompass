/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Support selection range
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;

/**
 * Context of a trace, which is the representation of the "view" the user
 * currently has on this trace (window time range, selected time or time range).
 *
 * TODO could be extended to support the notion of current location too.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
final class TmfTraceContext {

    static final TmfTraceContext NULL_CONTEXT =
            new TmfTraceContext(TmfTimestamp.BIG_CRUNCH, TmfTimestamp.BIG_CRUNCH, TmfTimeRange.NULL_RANGE);

    private final TmfTimeRange fSelection;
    private final TmfTimeRange fWindowRange;

    public TmfTraceContext(ITmfTimestamp beginTs, ITmfTimestamp endTs, TmfTimeRange tr) {
        fSelection = new TmfTimeRange(beginTs, endTs);
        fWindowRange = tr;
    }

    public TmfTraceContext(TmfTraceContext prevCtx, ITmfTimestamp beginTs, ITmfTimestamp endTs) {
        fSelection = new TmfTimeRange(beginTs, endTs);
        fWindowRange = prevCtx.fWindowRange;
    }

    public TmfTraceContext(TmfTraceContext prevCtx, TmfTimeRange tr) {
        fSelection = prevCtx.fSelection;
        fWindowRange = tr;
    }

    public ITmfTimestamp getSelectionBegin() {
        return fSelection.getStartTime();
    }

    public ITmfTimestamp getSelectionEnd() {
        return fSelection.getEndTime();
    }

    public TmfTimeRange getWindowRange() {
        return fWindowRange;
    }

    public boolean isValid() {
        if (fSelection.getStartTime().compareTo(TmfTimestamp.ZERO) <= 0 ||
                fSelection.getEndTime().compareTo(TmfTimestamp.ZERO) <= 0 ||
                fWindowRange.getEndTime().compareTo(fWindowRange.getStartTime()) <= 0) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[fSelection=" + fSelection + //$NON-NLS-1$
                ", fWindowRange=" + fWindowRange + ']'; //$NON-NLS-1$
    }
}
