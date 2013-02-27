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
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.stubs.trace;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;

/**
 * <b><u>TmfEmptyTraceStub</u></b>
 * <p>
 * Dummy test trace. Use in conjunction with TmfEventParserStub.
 */
public class TmfEmptyTraceStub extends TmfTraceStub {

    /**
     *
     */
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     *
     */
    public TmfEmptyTraceStub() {
        super();
        setParser(new TmfEventParserStub(this));
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