/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.signal;

import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * A trace was updated (typically its time range)
 * 
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfTraceUpdatedSignal extends TmfSignal {

    private final ITmfTrace<?> fTrace;
    private final TmfTimeRange fTimeRange;

    /**
     * Constructor
     *
     * @param source
     *            Object sending this signal
     * @param trace
     *            The trace that was updated
     * @param range
     *            The new time range of the trace
     */
    public TmfTraceUpdatedSignal(Object source, ITmfTrace<?> trace, TmfTimeRange range) {
        super(source);
        fTrace = trace;
        fTimeRange = range;
    }

    /**
     * @return The trace referred to by this signal
     */
    public ITmfTrace<?> getTrace() {
        return fTrace;
    }

    /**
     * @return The time range indicated by this signal
     */
    public TmfTimeRange getRange() {
        return fTimeRange;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "[TmfTraceUpdatedSignal (" + fTrace.toString() + ", "
                + fTimeRange.toString() + ")]";
    }

}
