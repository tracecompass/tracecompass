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
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;

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

    private final int fCpu;
    private final int fTid;
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
        fCpu = INVALID_CPU;
        fTid = INVALID_THREAD_ID;
        fTrace = trace;
    }

    /**
     * Constructs a new trace context with data taken from a builder.
     *
     * @param builder
     *            the builder
     * @since 2.2
     */
    public LinuxTraceContext(LinuxBuilder builder) {
        super(builder);
        fCpu = builder.cpu;
        fTid = builder.tid;
        fTrace = builder.trace;
    }

    @Override
    public void receive(@NonNull TmfTraceModelSignal signal) {
        if (signal.getHostId().equals(fTrace.getHostId())) {
            TmfTraceManager.getInstance().updateTraceContext(fTrace, builder -> {
                if (builder instanceof LinuxBuilder) {
                    if (signal instanceof TmfThreadSelectedSignal) {
                        ((LinuxBuilder) builder).setTid(((TmfThreadSelectedSignal) signal).getThreadId());
                    } else if (signal instanceof TmfCpuSelectedSignal) {
                        ((LinuxBuilder) builder).setCpu(((TmfCpuSelectedSignal) signal).getCore());
                    }
                }
                return builder;
            });
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

    @Override
    public @NonNull Builder builder() {
        return new LinuxBuilder(this);
    }

    /**
     * A builder for creating trace context instances.
     *
     * @since 2.2
     */
    public class LinuxBuilder extends Builder {
        private int cpu;
        private int tid;
        private ITmfTrace trace;

        /**
         * Constructor
         *
         * @param ctx
         *            the trace context used to initialize the builder
         */
        public LinuxBuilder(LinuxTraceContext ctx) {
            super(ctx);
            this.cpu = ctx.fCpu;
            this.tid = ctx.fTid;
            this.trace = ctx.fTrace;
        }

        /**
         * Build the trace context.
         *
         * @return a trace context
         */
        @Override
        public TmfTraceContext build() {
            return new LinuxTraceContext(this);
        }

        /**
         * Sets the current CPU.
         *
         * @param cpu
         *            the current CPU
         * @return this {@code Builder} object
         */
        public Builder setCpu(int cpu) {
            this.cpu = cpu;
            return this;
        }

        /**
         * Sets the current TID.
         *
         * @param tid
         *            the current TID
         * @return this {@code Builder} object
         */
        public Builder setTid(int tid) {
            this.tid = tid;
            return this;
        }
    }
}
