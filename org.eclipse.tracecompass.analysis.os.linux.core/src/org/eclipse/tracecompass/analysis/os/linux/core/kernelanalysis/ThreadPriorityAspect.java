/*******************************************************************************
 * Copyright (c) 2015 Keba AG
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Christian Mansky - Initial implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * This aspect finds the priority of the thread running from this event using
 * the {@link KernelAnalysis}.
 *
 * @author Christian Mansky
 * @since 1.0
 */
public final class ThreadPriorityAspect implements ITmfEventAspect {

    /** The singleton instance */
    public static final ThreadPriorityAspect INSTANCE = new ThreadPriorityAspect();

    private ThreadPriorityAspect() {
    }

    @Override
    public final String getName() {
        return NonNullUtils.nullToEmptyString(Messages.AspectName_Prio);
    }

    @Override
    public final String getHelpText() {
        return NonNullUtils.nullToEmptyString(Messages.AspectHelpText_Prio);
    }

    @Override
    public @Nullable Integer resolve(ITmfEvent event) {
        KernelAnalysis kernelAnalysis = TmfTraceUtils.getAnalysisModuleOfClass(event.getTrace(), KernelAnalysis.class, KernelAnalysis.ID);
        if (kernelAnalysis == null) {
            return null;
        }

        Integer tid = KernelTidAspect.INSTANCE.resolve(event);
        if (tid == null) {
            return null;
        }

        Integer prio = KernelThreadInformationProvider.getThreadPrio(kernelAnalysis, tid, event.getTimestamp().getValue());
        return prio;
    }

}
