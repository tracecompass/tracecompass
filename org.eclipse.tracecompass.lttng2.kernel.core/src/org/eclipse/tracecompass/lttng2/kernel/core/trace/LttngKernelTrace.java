/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Matthew Khouzam - Improved validation
 ******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.trace;

import java.nio.BufferOverflowException;
import java.util.Collection;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.ThreadPriorityAspect;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelTidAspect;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.Activator;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout.Lttng26EventLayout;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout.LttngEventLayout;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout.PerfEventLayout;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.trace.TraceValidationStatus;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;

import com.google.common.collect.ImmutableSet;

/**
 * This is the specification of CtfTmfTrace for use with LTTng 2.x kernel
 * traces.
 *
 * @author Alexandre Montplaisir
 */
public class LttngKernelTrace extends CtfTmfTrace implements IKernelTrace {

    /**
     * Supported Linux kernel tracers
     */
    private enum OriginTracer {
        LTTNG(LttngEventLayout.getInstance()),
        LTTNG26(Lttng26EventLayout.getInstance()),
        PERF(PerfEventLayout.getInstance());

        private final @NonNull IKernelAnalysisEventLayout fLayout;

        private OriginTracer(@NonNull IKernelAnalysisEventLayout layout) {
            fLayout = layout;
        }
    }

    /**
     * Event aspects available for all Lttng Kernel traces
     */
    private static final @NonNull Collection<ITmfEventAspect> LTTNG_KERNEL_ASPECTS;

    static {
        ImmutableSet.Builder<ITmfEventAspect> builder = ImmutableSet.builder();
        builder.addAll(CtfTmfTrace.CTF_ASPECTS);
        builder.add(KernelTidAspect.INSTANCE);
        builder.add(ThreadPriorityAspect.INSTANCE);
        LTTNG_KERNEL_ASPECTS = NonNullUtils.checkNotNull(builder.build());
    }

    /**
     * CTF metadata identifies trace type and tracer version pretty well, we are
     * quite confident in the inferred trace type.
     */
    private static final int CONFIDENCE = 100;

    /** The tracer which originated this trace */
    private OriginTracer fOriginTracer = null;

    /**
     * Default constructor
     */
    public LttngKernelTrace() {
        super();
    }

    @Override
    public @NonNull IKernelAnalysisEventLayout getKernelEventLayout() {
        OriginTracer tracer = fOriginTracer;
        if (tracer == null) {
            throw new IllegalStateException("Cannot get the layout of a non-initialized trace!"); //$NON-NLS-1$
        }
        return tracer.fLayout;
    }

    @Override
    public void initTrace(IResource resource, String path,
            Class<? extends ITmfEvent> eventType) throws TmfTraceException {
        super.initTrace(resource, path, eventType);

        /*
         * Set the 'fOriginTracer' in accordance to what is found in the
         * metadata
         */
        Map<String, String> traceEnv = this.getEnvironment();
        String tracerName = traceEnv.get("tracer_name"); //$NON-NLS-1$
        String tracerMajor = traceEnv.get("tracer_major"); //$NON-NLS-1$
        String tracerMinor = traceEnv.get("tracer_minor"); //$NON-NLS-1$

        if ("\"perf\"".equals(tracerName)) { //$NON-NLS-1$
            fOriginTracer = OriginTracer.PERF;

        } else if ("\"lttng-modules\"".equals(tracerName) && //$NON-NLS-1$
                tracerMajor != null && (Integer.valueOf(tracerMajor) >= 2) &&
                tracerMinor != null && (Integer.valueOf(tracerMinor) >= 6)) {
            fOriginTracer = OriginTracer.LTTNG26;

        } else {
            fOriginTracer = OriginTracer.LTTNG;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation sets the confidence to 100 if the trace is a valid
     * CTF trace in the "kernel" domain.
     */
    @Override
    public IStatus validate(final IProject project, final String path) {
        /*
         * Make sure the trace is openable as a CTF trace. We do this here
         * instead of calling super.validate() to keep the reference to "temp".
         */
        try (CtfTmfTrace temp = new CtfTmfTrace();) {
            temp.initTrace((IResource) null, path, CtfTmfEvent.class);

            /* Make sure the domain is "kernel" in the trace's env vars */
            String dom = temp.getEnvironment().get("domain"); //$NON-NLS-1$
            if (dom != null && dom.equals("\"kernel\"")) { //$NON-NLS-1$
                return new TraceValidationStatus(CONFIDENCE, Activator.PLUGIN_ID);
            }
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LttngKernelTrace_DomainError);

        } catch (TmfTraceException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.toString(), e);
        } catch (NullPointerException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.toString(), e);
        } catch (final BufferOverflowException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LttngKernelTrace_TraceReadError + ": " + Messages.LttngKernelTrace_MalformedTrace); //$NON-NLS-1$
        }
    }

    @Override
    public Iterable<ITmfEventAspect> getEventAspects() {
         return LTTNG_KERNEL_ASPECTS;
    }

}
