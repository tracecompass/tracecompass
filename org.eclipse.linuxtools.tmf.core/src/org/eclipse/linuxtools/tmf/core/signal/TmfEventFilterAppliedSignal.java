/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.signal;

import org.eclipse.linuxtools.tmf.core.filter.ITmfFilter;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Signal indicating an event filter has been applied.
 *
 * @version 1.0
 * @author Patrick Tasse
 * @since 2.0
 */
public class TmfEventFilterAppliedSignal extends TmfSignal {

    private final ITmfTrace fTrace;
    private final ITmfFilter fEventFilter;

    /**
     * Constructor for a new signal.
     *
     * @param source
     *            The object sending this signal
     * @param trace
     *            The trace to which filter is applied
     * @param filter
     *            The applied event filter or null
     */
    public TmfEventFilterAppliedSignal(Object source, ITmfTrace trace, ITmfFilter filter) {
        super(source);
        fTrace = trace;
        fEventFilter = filter;
    }

    /**
     * Get the trace object concerning this signal
     *
     * @return The trace
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Get the event filter being applied
     *
     * @return The filter
     */
    public ITmfFilter getEventFilter() {
        return fEventFilter;
    }

    @Override
    public String toString() {
        return "[TmfEventFilterAppliedSignal (" + fTrace.getName() + " : " + fEventFilter + ")]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
