/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.kernel;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.LinuxTidAspect;
import org.eclipse.tracecompass.analysis.os.linux.core.tid.TidAnalysisModule;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * This aspect finds the ID of the thread running from this event using the
 * {@link TidAnalysisModule}.
 *
 * @author Geneviève Bastien
 * @since 2.0
 */
public final class KernelTidAspect extends LinuxTidAspect {

    /** The singleton instance */
    public static final KernelTidAspect INSTANCE = new KernelTidAspect();

    private static final IProgressMonitor NULL_MONITOR = new NullProgressMonitor();

    private KernelTidAspect() {
    }

    @Override
    public @Nullable Integer resolve(ITmfEvent event) {
        try {
            return resolve(event, false, NULL_MONITOR);
        } catch (InterruptedException e) {
            /* Should not happen since there is nothing to interrupt */
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public @Nullable Integer resolve(@NonNull ITmfEvent event, boolean block, final IProgressMonitor monitor) throws InterruptedException {
        /* Find the CPU this event is run on */
        Integer cpu = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(),
                TmfCpuAspect.class, event);
        if (cpu == null) {
            return null;
        }

        /* Find the analysis module for the trace */
        TidAnalysisModule analysis = TmfTraceUtils.getAnalysisModuleOfClass(event.getTrace(),
                TidAnalysisModule.class, TidAnalysisModule.ID);
        if (analysis == null) {
            return null;
        }
        long ts = event.getTimestamp().toNanos();
        while (block && !analysis.isQueryable(ts) && !monitor.isCanceled()) {
            Thread.sleep(100);
        }
        return analysis.getThreadOnCpuAtTime(cpu, ts);
    }

}
