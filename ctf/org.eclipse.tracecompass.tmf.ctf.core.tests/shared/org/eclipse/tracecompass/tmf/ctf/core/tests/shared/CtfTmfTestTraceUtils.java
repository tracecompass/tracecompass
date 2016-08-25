/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson, EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.shared;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.ctf.core.tests.shared.LttngTraceGenerator;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.stubs.CtfTmfTraceStub;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;

/**
 * Wrapper for the CTF test traces, instantiating {@link CtfTmfTrace} objects
 * from them.
 *
 * @author Alexandre Montplaisir
 */
@NonNullByDefault
public final class CtfTmfTestTraceUtils {

    private static final Map<CtfTestTrace, CtfTmfTrace> CTF_TMF_TRACES = new HashMap<>();

    private CtfTmfTestTraceUtils() {}

    /**
     * Return a CtfTmfTraceStub object of this test trace. It will be already
     * initTrace()'ed.
     *
     * After being used by unit tests, traces should be properly disposed by
     * calling the {@link #dispose(CtfTestTrace)} method.
     *
     * @param ctfTrace
     *            The test trace to initialize
     * @return A CtfTmfTrace reference to this trace
     */
    public static synchronized CtfTmfTrace getTrace(CtfTestTrace ctfTrace) {
        String tracePath;
        try {
            tracePath = FileLocator.toFileURL(ctfTrace.getTraceURL()).getPath();
        } catch (IOException e) {
            throw new IllegalStateException();
        }

        dispose(ctfTrace);
        CtfTmfTrace trace = new CtfTmfTraceStub();
        try {
            trace.initTrace(null, tracePath, CtfTmfEvent.class);
        } catch (TmfTraceException e) {
            /* Should not happen if tracesExist() passed */
            throw new RuntimeException(e);
        }
        CTF_TMF_TRACES.put(ctfTrace, trace);
        return trace;
    }

    /**
     * Dispose of the trace
     *
     * @param ctfTrace
     *            Trace to dispose
     */
    public static synchronized void dispose(CtfTestTrace ctfTrace) {
        CtfTmfTrace trace = CTF_TMF_TRACES.remove(ctfTrace);
        if (trace != null) {
            trace.dispose();
        }
    }

    /**
     * Get an initialized version of the synthetic trace.
     *
     * @return A reference to the synthetic trace
     */
    public static CtfTmfTrace getSyntheticTrace() {
        CtfTmfTrace trace = new CtfTmfTrace();
        try {
            trace.initTrace(null, LttngTraceGenerator.getPath(), CtfTmfEvent.class);
        } catch (TmfTraceException e) {
            throw new IllegalStateException();
        }
        return trace;
    }
}
