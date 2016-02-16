/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.trace;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.signals.TmfCpuSelectedSignal;
import org.eclipse.tracecompass.analysis.os.linux.core.signals.TmfThreadSelectedSignal;
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceModelSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;

/**
 * A Linux trace context is a context that stores OS related actions as well as
 * the regular context of a trace (window time range, selected time or time
 * range).
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class LinuxTraceContext extends TmfTraceContext {

    /** An invalid CPU */
    public static final int INVALID_CPU = -1;
    /** An invalid thread id */
    public static final int INVALID_THREAD_ID = -1;

    private int fCpu = INVALID_CPU;
    private int fTid = INVALID_THREAD_ID;
    private final ITmfTrace fTrace;

    /**
     * Build a new trace context.
     *
     * @param selection
     *            The selected time range
     * @param windowRange
     *            The visible window's time range
     * @param editorFile
     *            The file representing the selected editor
     * @param filter
     *            The currently applied filter. 'null' for none.
     * @param trace
     *            the trace
     * @since 2.0
     */
    public LinuxTraceContext(TmfTimeRange selection, TmfTimeRange windowRange, @Nullable IFile editorFile, @Nullable ITmfFilter filter, ITmfTrace trace) {
        super(selection, windowRange, editorFile, filter);
        fTrace = trace;
    }

    @Override
    public void receive(@NonNull TmfTraceModelSignal signal) {
        if (signal.getHostId().equals(fTrace.getHostId())) {
            if (signal instanceof TmfThreadSelectedSignal) {
                fTid = ((TmfThreadSelectedSignal) signal).getThreadId();
            } else if (signal instanceof TmfCpuSelectedSignal) {
                fCpu = ((TmfCpuSelectedSignal) signal).getCore();
            }
        }
    }

    /**
     * Get the current CPU
     *
     * @return the current CPU, can be {@link #INVALID_CPU}
     */
    public int getCpu() {
        return fCpu;
    }

    /**
     * Get the current thread ID
     *
     * @return the current thread ID, can be {@link #INVALID_THREAD_ID}
     */
    public int getTid() {
        return fTid;
    }

}
