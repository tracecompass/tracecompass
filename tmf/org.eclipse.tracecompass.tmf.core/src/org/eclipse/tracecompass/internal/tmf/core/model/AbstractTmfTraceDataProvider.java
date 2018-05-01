/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.model;

import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Every concrete data provider which relies on a trace is highly recommended to
 * extend this class. Instead of duplicating the trace as a property in every
 * data provider, this class is intended to limit code duplication.
 *
 * @author Yonni Chen
 * @since 4.0
 */
public abstract class AbstractTmfTraceDataProvider {

    /** The trace that will be used by data providers */
    private final ITmfTrace fTrace;

    /**
     * Constructor
     *
     * @param trace
     *            A trace that will be used to perform analysis
     */
    public AbstractTmfTraceDataProvider(ITmfTrace trace) {
        fTrace = trace;
    }

    /**
     * Gets the trace that is encapsulated by this provider
     *
     * @return An {@link ITmfTrace} instance
     */
    protected ITmfTrace getTrace() {
        return fTrace;
    }
}
