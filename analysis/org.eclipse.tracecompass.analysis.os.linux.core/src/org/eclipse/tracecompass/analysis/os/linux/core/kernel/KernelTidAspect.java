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

package org.eclipse.tracecompass.analysis.os.linux.core.kernel;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.LinuxTidAspect;
import org.eclipse.tracecompass.analysis.os.linux.core.tid.TidAnalysisModule;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * This aspect finds the ID of the thread running from this event using the
 * {@link KernelAnalysisModule}.
 *
 * @author Geneviève Bastien
 * @since 2.0
 */
public final class KernelTidAspect extends LinuxTidAspect {

    /** The singleton instance */
    public static final KernelTidAspect INSTANCE = new KernelTidAspect();

    private KernelTidAspect() {
    }

    @Override
    public @Nullable Integer resolve(ITmfEvent event) {
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
        return analysis.getThreadOnCpuAtTime(cpu, event.getTimestamp().toNanos());
    }

}
