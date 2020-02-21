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

import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Signal indicating an event search has been applied.
 *
 * @author Patrick Tasse
 */
public class TmfEventSearchAppliedSignal extends TmfSignal {

    private final ITmfTrace fTrace;
    private final ITmfFilter fSearchFilter;

    /**
     * Constructor for a new signal.
     *
     * @param source
     *            The object sending this signal
     * @param trace
     *            The trace to which search is applied
     * @param filter
     *            The applied search filter or null
     */
    public TmfEventSearchAppliedSignal(Object source, ITmfTrace trace, ITmfFilter filter) {
        super(source);
        fTrace = trace;
        fSearchFilter = filter;
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
     * Get the search filter being applied
     *
     * @return The search filter
     */
    public ITmfFilter getSearchFilter() {
        return fSearchFilter;
    }

    @Override
    public String toString() {
        return "[TmfSearchFilterAppliedSignal (" + fTrace.getName() + " : " + fSearchFilter + ")]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
