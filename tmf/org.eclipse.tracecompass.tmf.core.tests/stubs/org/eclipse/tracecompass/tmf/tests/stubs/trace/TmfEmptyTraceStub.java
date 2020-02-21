/*******************************************************************************
 * Copyright (c) 2009, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.trace;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfContext;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;

/**
 * <b><u>TmfEmptyTraceStub</u></b>
 * <p>
 * Dummy test trace. Use in conjunction with TmfEventParserStub.
 */
public class TmfEmptyTraceStub extends TmfTraceStub {

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param path
     *            the empty trace path
     *
     * @throws TmfTraceException
     *             if an exception occurs
     */
    public TmfEmptyTraceStub(String path) throws TmfTraceException {
        super(path, ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, 0L);
    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

    @Override
    public TmfContext seekEvent(final ITmfLocation location) {
        return new TmfContext();
    }

    @Override
    public TmfContext seekEvent(final double ratio) {
        return new TmfContext();
    }

    @Override
    public double getLocationRatio(ITmfLocation location) {
        return 0;
    }

    @Override
    public ITmfLocation getCurrentLocation() {
        return null;
    }

    @Override
    public ITmfEvent parseEvent(final ITmfContext context) {
        return null;
    }

}