/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.tests.shared;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.lttng2.ust.core.tests.stubs.LttngUstTraceStub;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;

/**
 * Wrapper for the LTTng UST test traces, instantiating
 * {@link LttngUstTrace} objects from them.
 */
@NonNullByDefault
public final class LttngUstTestTraceUtils extends CtfTmfTestTraceUtils {

    private static final Map<CtfTestTrace, CtfTmfTrace> LTTNG_UST_TRACES = new HashMap<>();

    private LttngUstTestTraceUtils() {
        super();
    }

    /**
     * Return a LttngUstTraceStub object of this test trace. It will be
     * already initTrace()'ed.
     *
     * After being used by unit tests, traces should be properly disposed by
     * calling the {@link #dispose(CtfTestTrace)} method.
     *
     * @param ctfTrace
     *            The test trace to initialize
     * @return A LttngUstTrace reference to this trace
     */
    public static synchronized LttngUstTrace getTrace(CtfTestTrace ctfTrace) {
        return (LttngUstTrace) new LttngUstTestTraceUtils().internalGetTrace(ctfTrace, LTTNG_UST_TRACES, new LttngUstTraceStub());
    }

    /**
     * Dispose of the trace
     *
     * @param ctfTrace
     *            Trace to dispose
     */
    public static synchronized void dispose(CtfTestTrace ctfTrace) {
        new LttngUstTestTraceUtils().internalDispose(ctfTrace, LTTNG_UST_TRACES);
    }
}
