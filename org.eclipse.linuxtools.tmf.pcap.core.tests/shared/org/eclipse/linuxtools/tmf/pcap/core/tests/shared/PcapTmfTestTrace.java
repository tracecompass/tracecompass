/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.pcap.core.tests.shared;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.pcap.core.tests.shared.PcapTestTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.pcap.core.event.PcapEvent;
import org.eclipse.linuxtools.tmf.pcap.core.tests.stubs.PcapTmfTraceStub;
import org.eclipse.linuxtools.tmf.pcap.core.trace.PcapTrace;

/**
 * Available Pcap TMF test traces. Kind-of-extends {@link PcapTestTrace}.
 *
 * To run tests using these, you first need to run the "get-traces.[xml|sh]"
 * script located under lttng/org.eclipse.linuxtools.pcap.core.tests/rsc/ .
 *
 * @author Vincent Perot
 */
@NonNullByDefault
public enum PcapTmfTestTrace {
    /** A bad pcap file. */
    BAD_PCAPFILE,

    /** A Valid Pcap that is empty. */
    EMPTY_PCAP,

    /** A Pcap that mostly contains TCP packets. */
    MOSTLY_TCP,

    /** A Pcap that mostly contains UDP packets. */
    MOSTLY_UDP,

    /** A big-endian trace that contains two packets. */
    SHORT_BIG_ENDIAN,

    /** A little-endian trace that contains two packets. */
    SHORT_LITTLE_ENDIAN,

    /** A trace used for benchmarking. */
    BENCHMARK_TRACE,

    /** A Kernel trace directory. */
    KERNEL_DIRECTORY,

    /** A Kernel trace file. */
    KERNEL_TRACE;

    private final String fPath;
    private @Nullable PcapTmfTraceStub fTrace = null;

    private PcapTmfTestTrace() {
        @NonNull String path = PcapTestTrace.valueOf(this.name()).getPath();
        fPath = path;
    }

    /**
     * @return The path of this trace
     */
    public String getPath() {
        return fPath;
    }

    /**
     * Return a PcapTmfTraceStub object of this test trace. It will be already
     * initTrace()'ed.
     *
     * Make sure you call {@link #exists()} before calling this!
     *
     * After being used by unit tests, traces must be properly disposed of by
     * calling the {@link PcapTmfTestTrace#dispose()} method.
     *
     * @return A PcapTmfTrace reference to this trace
     */
    public synchronized PcapTrace getTrace() {
        PcapTmfTraceStub trace = fTrace;
        if (trace != null) {
            trace.dispose();
        }
        trace = new PcapTmfTraceStub();
        try {
            trace.initTrace(null, fPath, PcapEvent.class);
        } catch (TmfTraceException e) {
            /* Should not happen if tracesExist() passed */
            throw new RuntimeException(e);
        }
        fTrace = trace;
        return trace;
    }

    /**
     * Check if the trace actually exists on disk or not.
     *
     * @return If the trace is present
     */
    public boolean exists() {
        return PcapTestTrace.valueOf(this.name()).exists();
    }

    /**
     * Dispose of the trace
     */
    public void dispose() {
        if (fTrace != null) {
            fTrace.dispose();
            fTrace = null;
        }
    }
}
