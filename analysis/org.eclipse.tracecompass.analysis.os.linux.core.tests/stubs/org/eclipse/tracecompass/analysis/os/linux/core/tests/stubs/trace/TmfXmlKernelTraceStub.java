/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.trace;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelTidAspect;
import org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.ThreadPriorityAspect;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStub;

import com.google.common.collect.ImmutableSet;

/**
 * A trace stub that implements a kernel trace. It can add an event layout to
 * the trace.
 *
 * @author Geneviève Bastien
 */
public class TmfXmlKernelTraceStub extends TmfXmlTraceStub implements IKernelTrace {

    private @Nullable IKernelAnalysisEventLayout fLayout;

    @Override
    public IKernelAnalysisEventLayout getKernelEventLayout() {
        IKernelAnalysisEventLayout layout = fLayout;
        if (layout == null) {
            return KernelEventLayoutStub.getInstance();
        }
        return layout;
    }

    /**
     * Set the kernel event layout to use with this trace
     *
     * @param layout
     *            The event layout to use with this trace
     */
    public void setKernelEventLayout(IKernelAnalysisEventLayout layout) {
        fLayout = layout;
    }

    @Override
    public Iterable<ITmfEventAspect> getEventAspects() {
        /*
         * This method needs to fill the aspects dynamically because aspects in
         * the parent class are not all present at the beginning of the trace
         */
        ImmutableSet.Builder<ITmfEventAspect> builder = ImmutableSet.builder();
        builder.addAll(super.getEventAspects());
        builder.add(KernelTidAspect.INSTANCE);
        builder.add(ThreadPriorityAspect.INSTANCE);
        return builder.build();
    }

}
