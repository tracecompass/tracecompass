/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tid;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.DefaultEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.Activator;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.Messages;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue.Type;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Active tid analysis module, this only does one thing: figure out the active
 * tid on any given cpu. This analysis should be approx 10x faster than the full
 * {@link KernelAnalysisModule}.
 *
 * Note: this module exists as a way to accelerate the TID aspect, but also to
 * start splitting up the kernel analysis into smaller sections.
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class TidAnalysisModule extends TmfStateSystemAnalysisModule {

    /** The ID of this analysis module */
    public static final @NonNull String ID = "org.eclipse.tracecompass.analysis.os.linux.kernel.tid"; //$NON-NLS-1$

    /** The requirements as an immutable set */
    private static final @NonNull Set<@NonNull TmfAbstractAnalysisRequirement> REQUIREMENTS = Collections.EMPTY_SET;

    @Override
    public @NonNull Iterable<@NonNull TmfAbstractAnalysisRequirement> getAnalysisRequirements() {
        return REQUIREMENTS;
    }

    @Override
    public String getHelpText() {
        String msg = Messages.TidAnalysisModule_Description;
        return msg != null ? msg : super.getHelpText();
    }

    @Override
    public @NonNull String getHelpText(@NonNull ITmfTrace trace) {
        return getHelpText();
    }

    @Override
    protected @NonNull ITmfStateProvider createStateProvider() {
        ITmfTrace trace = checkNotNull(getTrace());
        IKernelAnalysisEventLayout layout = (trace instanceof IKernelTrace) ? ((IKernelTrace) trace).getKernelEventLayout() : DefaultEventLayout.getInstance();
        return new ActiveTidStateProvider(trace, layout);
    }

    /**
     * Gets the current thread ID on a given CPU for a given time
     *
     * @param cpu
     *            the CPU
     * @param time
     *            the time in nanoseconds
     * @return the current TID at the time on the CPU or {@code null} if not
     *         known
     */
    public @Nullable Integer getThreadOnCpuAtTime(int cpu, long time) {
        ITmfStateSystem stateSystem = getStateSystem();
        if (stateSystem == null || time < stateSystem.getStartTime()) {
            return null;
        }

        Integer tid = null;
        try {
            int cpuQuark = stateSystem.optQuarkAbsolute(Integer.toString(cpu));
            if (cpuQuark == ITmfStateSystem.INVALID_ATTRIBUTE) {
                return null;
            }
            ITmfStateValue value = stateSystem.querySingleState(time, cpuQuark).getStateValue();
            if (value.getType().equals(Type.INTEGER)) {
                tid = value.unboxInt();
            }
        } catch (StateSystemDisposedException | TimeRangeException e) {
            Activator.getDefault().logError(NonNullUtils.nullToEmptyString(e.getMessage()), e);
        }
        return tid;
    }

    /**
     * Gets the CPU a thread is running on for a given time <br>
     * Note: this is not designed to be fast, only convenient
     *
     * @param tid
     *            the tid
     * @param time
     *            the time in nanoseconds
     * @return the current CPU at the time for a TID or {@code null} if not
     *         available
     */
    public @Nullable Integer getCpuForTidAtTime(int tid, long time) {
        ITmfStateSystem stateSystem = getStateSystem();
        if (stateSystem == null) {
            return null;
        }

        try {
            for (ITmfStateInterval interval : stateSystem.queryFullState(time)) {
                if (tid == interval.getStateValue().unboxInt()) {
                    return Integer.parseInt(stateSystem.getAttributeName(interval.getAttribute()));
                }
            }
        } catch (StateSystemDisposedException e) {
            Activator.getDefault().logError(NonNullUtils.nullToEmptyString(e.getMessage()), e);
        }
        return null;
    }
}
