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

import java.io.FileNotFoundException;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;

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
     * http://www.dorsal.polymtl.ca/~alexmont/data/trace1.tar.bz2
     * 
     * and extract it at the root of the project. You can also set up a custom
     * path below.
     */
    public final static String traceFile1 = "trace1/kernel"; //$NON-NLS-1$
    public final static long startTime1 = 17620320801208L;

    public static CtfTmfTrace trace1 = null;

    public static CtfTmfTrace getTestTrace() {
        if (trace1 == null) {
            trace1 = new CtfTmfTrace();
            try {
                trace1.initTrace("test-trace1", traceFile1, CtfTmfEvent.class); //$NON-NLS-1$
            } catch (FileNotFoundException e) {
                /* If we don't have the file, we shouldn't even try the tests... */
                e.printStackTrace();
                return null;
            }
        }
        return trace1;
    }

}
