/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.lttng.kernel.core.tests.shared;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.lttng2.kernel.core.tests.stubs.LttngKernelTraceStub;
import org.eclipse.tracecompass.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;

/**
 * Wrapper for the LTTng Kernel test traces, instantiating
 * {@link LttngKernelTrace} objects from them.
 */
@NonNullByDefault
public final class LttngKernelTestTraceUtils extends CtfTmfTestTraceUtils {

    private static final Map<CtfTestTrace, CtfTmfTrace> LTTNG_KERNEL_TRACES = new HashMap<>();

    private LttngKernelTestTraceUtils() {
        super();
    }

    /**
     * Return a LttngKernelTraceStub object of this test trace. It will be
     * already initTrace()'ed.
     *
     * After being used by unit tests, traces should be properly disposed by
     * calling the {@link #dispose(CtfTestTrace)} method.
     *
     * @param ctfTrace
     *            The test trace to initialize
     * @return A LttngKernelTrace reference to this trace
     */
    public static synchronized LttngKernelTrace getTrace(CtfTestTrace ctfTrace) {
        return (LttngKernelTrace) new LttngKernelTestTraceUtils().internalGetTrace(ctfTrace, LTTNG_KERNEL_TRACES, new LttngKernelTraceStub());
    }

    /**
     * Dispose of the trace
     *
     * @param ctfTrace
     *            Trace to dispose
     */
    public static synchronized void dispose(CtfTestTrace ctfTrace) {
        new LttngKernelTestTraceUtils().internalDispose(ctfTrace, LTTNG_KERNEL_TRACES);
    }
}
