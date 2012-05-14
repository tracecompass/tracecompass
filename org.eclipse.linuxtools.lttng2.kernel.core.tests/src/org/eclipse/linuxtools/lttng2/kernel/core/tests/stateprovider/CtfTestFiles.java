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
public abstract class CtfTestFiles {

    public final static long NANOSECS_PER_SEC = 1000000000L;

    /*
     * To run these tests, you will first need to run the get-traces.sh script
     * located under lttng/org.eclipse.linuxtools.ctf.core.tests/traces/ .
     */
    public final static String traceFile = "../org.eclipse.linuxtools.ctf.core.tests/traces/trace2"; //$NON-NLS-1$
    public final static long startTime = 1331668247314038062L;
    public final static long endTime = 1331668259054285979L; /* Expected end time of history */

    public synchronized static CtfTmfTrace getTestTrace() throws TmfTraceException {
        CtfTmfTrace trace = new CtfTmfTrace();
        trace.initTrace(null, traceFile, CtfTmfEvent.class);
        return trace;
    }

}
