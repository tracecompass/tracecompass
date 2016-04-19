/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.tests.callstack;

import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;

/**
 * Test suite for the UST callstack state provider, using the trace of a program
 * instrumented with lttng-ust-cyg-profile.so tracepoints.
 *
 * @author Alexandre Montplaisir
 */
public class LttngUstCallStackProviderTest extends AbstractProviderTest {

    private static final long[] timestamps = { 1378850463600000000L,
                                               1378850463770000000L,
                                               1378850463868753000L };

    @Override
    protected CtfTestTrace getTestTrace() {
        return CtfTestTrace.CYG_PROFILE;
    }

    @Override
    protected int getProcessId() {
        /* This particular trace does not have PID contexts. */
        return -1;
    }

    @Override
    protected String getThreadName() {
        return "glxgears-16073";
    }

    @Override
    protected long getTestTimestamp(int index) {
        return timestamps[index];
    }

}
