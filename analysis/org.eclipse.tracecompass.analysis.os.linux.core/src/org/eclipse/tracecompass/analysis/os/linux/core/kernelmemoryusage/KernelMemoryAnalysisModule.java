/**********************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.analysis.os.linux.core.kernelmemoryusage;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.tid.TidAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.DefaultEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.ImmutableSet;

/**
 * This analysis module creates a stateprovider that keeps track of the memory
 * allocated and deallocated by the kernel
 *
 * @author Samuel Gagnon
 * @since 2.0
 */
public class KernelMemoryAnalysisModule extends TmfStateSystemAnalysisModule {

    /**
     * Analysis ID, it should match that in the plugin.xml file
     */
    public static final @NonNull String ID = "org.eclipse.tracecompass.analysis.os.linux.core.kernelmemory"; //$NON-NLS-1$

    /**
     * Each thread attribute in the tree has an attribute for keeping the lowest memory
     * value for that thread during the trace. (Those values can be negative because we
     * don't have access to a memory dump before the trace)
     */
    public static final @NonNull String THREAD_LOWEST_MEMORY_VALUE = "lowestMemory"; //$NON-NLS-1$

    @Override
    protected Iterable<IAnalysisModule> getDependentAnalyses() {
        ITmfTrace trace = getTrace();
        if (trace == null) {
            throw new IllegalStateException("Trace should not be null"); //$NON-NLS-1$
        }
        IAnalysisModule module = TmfTraceUtils.getAnalysisModuleOfClass(trace, TidAnalysisModule.class, TidAnalysisModule.ID);
        if (module == null) {
            throw new IllegalStateException("Module should not be null"); //$NON-NLS-1$
        }
        return ImmutableSet.of(module);
    }

    @Override
    protected @NonNull ITmfStateProvider createStateProvider() {
        ITmfTrace trace = checkNotNull(getTrace());
        IKernelAnalysisEventLayout layout;

        if (trace instanceof IKernelTrace) {
            layout = ((IKernelTrace) trace).getKernelEventLayout();
        } else {
            /* Fall-back to the base LttngEventLayout */
            layout = DefaultEventLayout.getInstance();
        }
        return new KernelMemoryStateProvider(trace, layout);
    }
}