/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.tests.stateprovider;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;

/**
 * Definitions used by all tests using CTF trace files
 * 
 * @author alexmont
 * 
 */
public abstract class CTFTestFiles {

    public final static long NANOSECS_PER_SEC = 1000000000L;

    /*
     * To run these tests, you will need to download the following trace, at:
     * http://www.dorsal.polymtl.ca/~alexmont/data/trace2.tar.bz2
     * 
     * and extract it in the
     * .../org.eclipse.linuxtools.lttng2.kernel.core.tests/traces/ directory.
     * You can also set up a custom path below.
     */
    public final static String traceFile = "traces/trace2"; //$NON-NLS-1$
    public final static long startTime = 18669367225825L;

    public static CtfTmfTrace trace = null;

    public static CtfTmfTrace getTestTrace() throws TmfTraceException {
        if (trace == null) {
            trace = new CtfTmfTrace();
            trace.initTrace(null, traceFile, CtfTmfEvent.class);
        }
        return trace;
    }

}
