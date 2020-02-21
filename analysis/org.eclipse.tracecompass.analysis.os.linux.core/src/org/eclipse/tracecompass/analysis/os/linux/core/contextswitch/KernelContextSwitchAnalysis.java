/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexis Cabana-Loriaux - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.contextswitch;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.DefaultEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.Activator;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.Messages;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * This analysis module computes the number of context switches of a system from
 * a kernel trace.
 *
 * @author Alexis Cabana-Loriaux
 * @since 2.0
 */
@NonNullByDefault
public class KernelContextSwitchAnalysis extends TmfStateSystemAnalysisModule {

    /** The ID of this analysis */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.contextswitch"; //$NON-NLS-1$

    /** Integer used to identify 'total' entries in the returned maps */
    public static final Integer TOTAL = -1;

    @Override
    public @NonNull String getHelpText() {
        String msg = Messages.KernelContextSwitchAnalysis_Description;
        return msg != null ? msg : super.getHelpText();
    }

    @Override
    public @NonNull String getHelpText(@NonNull ITmfTrace trace) {
        return getHelpText();
    }

    @Override
    protected ITmfStateProvider createStateProvider() {
        ITmfTrace trace = checkNotNull(getTrace());
        IKernelAnalysisEventLayout layout;

        if (trace instanceof IKernelTrace) {
            layout = ((IKernelTrace) trace).getKernelEventLayout();
        } else {
            /* Fall-back to the base LttngEventLayout */
            layout = DefaultEventLayout.getInstance();
        }

        return new KernelContextSwitchStateProvider(trace, layout);
    }

    @Override
    protected StateSystemBackendType getBackendType() {
        return StateSystemBackendType.FULL;
    }

    @Override
    protected Iterable<IAnalysisModule> getDependentAnalyses() {
        Set<IAnalysisModule> modules = new HashSet<>();

        ITmfTrace trace = getTrace();
        if (trace == null) {
            throw new IllegalStateException("Analysis requires a trace"); //$NON-NLS-1$
        }
        /*
         * This analysis depends on the LTTng kernel analysis, so it's added to
         * dependent modules.
         */
        Iterable<KernelAnalysisModule> kernelModules = TmfTraceUtils.getAnalysisModulesOfClass(trace, KernelAnalysisModule.class);
        for (KernelAnalysisModule kernelModule : kernelModules) {
            /* Only add the first one we find, if there is one */
            modules.add(kernelModule);
            break;
        }
        return modules;
    }

    /**
     * Get a map of the number of context switch per CPU during a time range.
     *
     * @param startParam
     *            Start time of requested range
     * @param endParam
     *            End time of requested range
     * @return A map of CPU# -> nb of context switch in the [start, end]
     *         interval. CPU# == -1 represents the total number of context
     *         switch
     * @throws TimeRangeException
     *             if one or more of the parameters is outside the range the
     *             state history
     */
    public @NonNullByDefault({}) @NonNull Map<Integer, Long> getContextSwitchesRange(final long startParam, final long endParam) {
        final @Nullable ITmfStateSystem stateSystem = getStateSystem();
        ITmfTrace trace = getTrace();
        if (trace == null || stateSystem == null) {
            return Collections.<Integer, Long> emptyMap();
        }
        long start = Math.max(startParam, stateSystem.getStartTime());
        long end = Math.min(endParam, stateSystem.getCurrentEndTime());
        ITmfStateSystem contextSwitchStateSystem = TmfStateSystemAnalysisModule.getStateSystem(trace, KernelContextSwitchAnalysis.ID);
        if (contextSwitchStateSystem == null) {
            return Collections.<Integer, Long> emptyMap();
        }

        /*
         * Make sure the start/end times are within the state history, so we
         * don't get TimeRange exceptions.
         */
        long startTime = contextSwitchStateSystem.getStartTime();
        long endTime = contextSwitchStateSystem.getCurrentEndTime();
        if (endTime < startTime) {
            return Collections.<Integer, Long> emptyMap();
        }

        Map<Integer, Long> map = new HashMap<>();
        try {
            /* Get the list of quarks for each CPU */
            int cpusNode = contextSwitchStateSystem.getQuarkAbsolute(Attributes.CPUS);
            List<Integer> cpuQuarks = contextSwitchStateSystem.getSubAttributes(cpusNode, false);
            /* Query full states at start and end times */
            List<ITmfStateInterval> kernelEndState = contextSwitchStateSystem.queryFullState(end);
            List<ITmfStateInterval> kernelStartState = contextSwitchStateSystem.queryFullState(start);
            Long totalNbCxtSwt = 0l;
            for (Integer cpuQuark : cpuQuarks) {
                int cpuNb = Integer.parseInt(contextSwitchStateSystem.getAttributeName(cpuQuark.intValue()));
                Long nbCxtSwtForCore = kernelEndState.get(cpuQuark).getStateValue().unboxLong() - kernelStartState.get(cpuQuark).getStateValue().unboxLong();
                map.put(cpuNb, nbCxtSwtForCore);
                totalNbCxtSwt += nbCxtSwtForCore;
            }

            /* Put the total number of context switches in the interval */
            map.put(TOTAL, totalNbCxtSwt);
        } catch (TimeRangeException | AttributeNotFoundException e) {
            /*
             * Assume there is no events or the attribute does not exist yet,
             * nothing will be put in the map.
             */
        } catch (StateValueTypeException | StateSystemDisposedException e) {
            /*
             * These other exception types would show a logic problem, so they
             * should not happen.
             */
            Activator.getDefault().logError("Error getting CPU context switches in a time range", e); //$NON-NLS-1$
        }

        return map;
    }

}
