/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.LinuxPidAspect;
import org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.LinuxTidAspect;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelThreadInformationProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * This aspect finds the ID of the thread running from this event using the
 * {@link KernelAnalysisModule}.
 *
 * @author Geneviève Bastien
 * @since 2.0
 */
public final class KernelPidAspect extends LinuxPidAspect {

    /** The singleton instance */
    public static final KernelPidAspect INSTANCE = new KernelPidAspect();

    private static final IProgressMonitor NULL_MONITOR = new NullProgressMonitor();

    private KernelPidAspect() {
    }

    @Override
    public @Nullable Integer resolve(ITmfEvent event) {
        try {
            return resolve(event, false, NULL_MONITOR);
        } catch (InterruptedException e) {
            /* Should not happen since there is nothing to interrupt */
            return null;
        }
    }

    @Override
    public @Nullable Integer resolve(@NonNull ITmfEvent event, boolean block, final IProgressMonitor monitor) throws InterruptedException {
        /* Find the running tid */
        Integer tid = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(),
                LinuxTidAspect.class, event);
        if (tid == null) {
            return null;
        }

        /* Find the analysis module for the trace */
        KernelAnalysisModule analysis = TmfTraceUtils.getAnalysisModuleOfClass(event.getTrace(),
                KernelAnalysisModule.class, KernelAnalysisModule.ID);
        if (analysis == null) {
            return null;
        }
        long ts = event.getTimestamp().toNanos();
        while (block && !analysis.isQueryable(ts) && !monitor.isCanceled()) {
            Thread.sleep(100);
        }
        return KernelThreadInformationProvider.getProcessId(analysis, tid, ts);
    }

}
