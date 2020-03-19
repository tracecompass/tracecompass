/**********************************************************************
 * Copyright (c) 2016 Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.analysis.os.linux.core.kernelmemoryusage;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelTidAspect;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.StateSystemBuilderUtils;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Creates a state system and computes the total memory usage for all threads
 * and for each selected thread from a kernel trace. It examines the page
 * allocation and deallocation events in the kernel to do so.
 *
 * The state provider also contains code that can query the state system.
 *
 * Attribute tree:
 *
 * <pre>
 * |- <TID number> -> current memory usage
 * |  |- THREAD_LOWEST_MEMORY_VALUE -> lowest memory value for thread
 * </pre>
 *
 * @author Samuel Gagnon
 * @since 2.0
 */
public class KernelMemoryStateProvider extends AbstractTmfStateProvider {

    /**
     * Special string to save memory allocation when tid is not known
     */
    public static final String OTHER_TID = "other"; //$NON-NLS-1$

    /* Version of this state provider */
    private static final int VERSION = 2;

    private static final int PAGE_SIZE = 4096;

    private static final long MAX_ORDER = 62; // Larger than that would overflow

    private IKernelAnalysisEventLayout fLayout;

    /**
     * Constructor
     *
     * @param trace
     *            trace
     * @param layout
     *            layout
     */
    public KernelMemoryStateProvider(@NonNull ITmfTrace trace, IKernelAnalysisEventLayout layout) {
        super(trace, "Kernel:Memory"); //$NON-NLS-1$
        fLayout = layout;
    }

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public ITmfStateProvider getNewInstance() {
        return new KernelMemoryStateProvider(getTrace(), fLayout);
    }

    @Override
    protected void eventHandle(@NonNull ITmfEvent event) {
        String name = event.getName();

        long inc;
        if (name.equals(fLayout.eventKmemPageAlloc())) {
            inc = PAGE_SIZE;
        } else if (name.equals(fLayout.eventKmemPageFree())) {
            inc = -PAGE_SIZE;
        } else {
            return;
        }

        try {
            String fieldOrder = fLayout.fieldOrder();
            if (fieldOrder != null) {
                Long value = event.getContent().getFieldValue(Long.class, fieldOrder);
                if (value != null) {
                    if (value > MAX_ORDER || value < 0) {
                        Activator.getDefault().logWarning("Order of alloc is outside of acceptable range : " + value); //$NON-NLS-1$
                        return;
                    }
                    inc <<= value;
                }
            }
            ITmfStateSystemBuilder ss = checkNotNull(getStateSystemBuilder());
            long ts = event.getTimestamp().toNanos();

            Integer tidField = KernelTidAspect.INSTANCE.resolve(event, true, new NullProgressMonitor());
            String tid;
            if (tidField == null) {
                // if the TID is not available
                tid = OTHER_TID;
            } else {
                tid = tidField.toString();
            }

            int tidQuark = ss.getQuarkAbsoluteAndAdd(tid);
            StateSystemBuilderUtils.incrementAttributeLong(ss, ts, tidQuark, inc);
            long currentMemoryValue = ss.queryOngoingState(tidQuark).unboxLong();

            /**
             * We add an attribute to keep the lowest memory value for each thread. This
             * quantity is used when we plot to avoid negative values.
             */
            int lowestMemoryQuark = ss.getQuarkRelativeAndAdd(tidQuark, KernelMemoryAnalysisModule.THREAD_LOWEST_MEMORY_VALUE);
            ITmfStateValue lowestMemoryValue = ss.queryOngoingState(lowestMemoryQuark);
            long previousLowest = lowestMemoryValue.isNull() ? 0 : lowestMemoryValue.unboxLong();

            if (previousLowest > currentMemoryValue) {
                ss.modifyAttribute(ts, currentMemoryValue, lowestMemoryQuark);
            }
        } catch (InterruptedException e) {
            Activator.getDefault().logError(String.valueOf(e.getMessage()), e);
        }
    }
}
