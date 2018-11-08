/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Signal indicating a global filter has been applied.
 *
 * @author Geneviève Bastien
 */
public class TmfFilterAppliedSignal extends TmfSignal {

    private final ITmfTrace fTrace;
    private final TraceCompassFilter fFilter;

    /**
     * Constructor for a new signal.
     *
     * @param source
     *            The object sending this signal
     * @param trace
     *            The trace to which filter is applied
     * @param filter
     *            The filter to apply
     */
    public TmfFilterAppliedSignal(Object source, ITmfTrace trace, @NonNull TraceCompassFilter filter) {
        super(source);
        fTrace = trace;
        fFilter = filter;
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
     * Get the filter that is being applied
     *
     * @return The filter being applied
     */
    public TraceCompassFilter getFilter() {
        return fFilter;
    }

    @Override
    public String toString() {
        return "[TmfEventFilterAppliedSignal (" + fTrace.getName() + " : " + fFilter + ")]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
