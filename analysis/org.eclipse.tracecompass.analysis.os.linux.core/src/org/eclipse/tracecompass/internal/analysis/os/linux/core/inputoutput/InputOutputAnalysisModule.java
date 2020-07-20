/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.tid.TidAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.DefaultEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.IAnalysisProgressListener;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.IGroupingSegmentAspect;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.Activator;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.segment.ISegmentAspect;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.ImmutableList;

/**
 * State System Module for Input Output traces
 *
 * @author Houssem Daoud
 */
public class InputOutputAnalysisModule extends TmfStateSystemAnalysisModule {

    /** The ID of this analysis module */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.inputoutput"; //$NON-NLS-1$
    private final Collection<ISegmentAspect> fAspects = ImmutableList.of(new IoDiskAspect(), IoRequestTypeAspect.INSTANCE);

    private final ISegmentStoreProvider fWaitingQueueSs = new InputOutputSegmentStore(Attributes.WAITING_QUEUE);
    private final ISegmentStoreProvider fExecQueueSs = new InputOutputSegmentStore(Attributes.DRIVER_QUEUE);

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

        return new InputOutputStateProvider(trace, layout);
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
            throw new IllegalStateException();
        }
        /*
         * This analysis depends on the LTTng kernel analysis, so it's added to
         * dependent modules.
         */
        Iterable<TidAnalysisModule> tidModules = TmfTraceUtils.getAnalysisModulesOfClass(trace, TidAnalysisModule.class);
        for (TidAnalysisModule tidModule : tidModules) {
            /* Only add the first one we find, if there is one */
            modules.add(tidModule);
            break;
        }
        return modules;
    }

    /**
     * @param start
     *            start interval
     * @param end
     *            end interval
     * @return table of latencies
     */
    public ArrayList<Long> getLatencyTables(long start, long end) {
        @NonNull
        ArrayList<@NonNull Long> map = new ArrayList<>();
        ITmfTrace trace = getTrace();
        ITmfStateSystem ss = getStateSystem();
        if (trace == null || ss == null) {
            return map;
        }

        try {
            int driverQueueQuark;

            List<Integer> diskQuarks = ss.getQuarks(Attributes.DISKS, "*"); //$NON-NLS-1$

            for (Integer diskQuark : diskQuarks) {

                driverQueueQuark = ss.getQuarkRelative(diskQuark, Attributes.DRIVER_QUEUE); // $NON-NLS-1$
                List<Integer> slotsQuark = ss.getSubAttributes(driverQueueQuark, false);
                for (Integer slotQuark : slotsQuark) {
                    List<ITmfStateInterval> intervals = StateSystemUtils.queryHistoryRange(ss, slotQuark, start, end);
                    for (ITmfStateInterval currentInterval : intervals) {
                        if (!currentInterval.getStateValue().isNull()) {
                            Long latency = currentInterval.getEndTime() - currentInterval.getStartTime();
                            // Long sector =
                            // currentInterval.getStateValue().unboxLong();
                            map.add(latency / 1000000);
                        }
                    }
                }
            }
        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            Activator.getDefault().logError("Error getting the latency table", e); //$NON-NLS-1$
            return map;
        }
        return map;
    }

    private final class IoDiskAspect implements IGroupingSegmentAspect {

        private IoDiskAspect() {
            // Do nothing
        }

        @Override
        public String getName() {
            return Objects.requireNonNull(Messages.IoAspect_DiskName);
        }

        @Override
        public String getHelpText() {
            return Objects.requireNonNull(Messages.IoAspect_DiskHelpText);
        }

        @Override
        public @Nullable Comparator<?> getComparator() {
            return null;
        }

        @Override
        public @Nullable Object resolve(ISegment segment) {
            if (segment instanceof RequestIntervalSegment) {
                return ((RequestIntervalSegment) segment).getDiskName();
            }
            return EMPTY_STRING;
        }
    }

    private static final class IoRequestTypeAspect implements ISegmentAspect {
        public static final ISegmentAspect INSTANCE = new IoRequestTypeAspect();

        private IoRequestTypeAspect() {
            // Do nothing
        }

        @Override
        public String getHelpText() {
            return Objects.requireNonNull(Messages.IoAspect_TypeHelpText);
        }

        @Override
        public String getName() {
            return Objects.requireNonNull(Messages.IoAspect_TypeName);
        }

        @Override
        public @Nullable Comparator<?> getComparator() {
            return null;
        }

        @Override
        public @Nullable String resolve(ISegment segment) {
            if (segment instanceof RequestIntervalSegment) {
                return ((RequestIntervalSegment) segment).getOperationType().name();
            }
            return EMPTY_STRING;
        }
    }

    private class InputOutputSegmentStore implements ISegmentStoreProvider {

        private final String fQueueAttribute;

        public InputOutputSegmentStore(String queueAttribute) {
            fQueueAttribute = queueAttribute;
        }

        @Override
        public void addListener(@NonNull IAnalysisProgressListener listener) {

        }

        @Override
        public void removeListener(@NonNull IAnalysisProgressListener listener) {

        }

        @Override
        public Iterable<ISegmentAspect> getSegmentAspects() {
            return fAspects;
        }

        @Override
        public @Nullable ISegmentStore<ISegment> getSegmentStore() {
            ITmfStateSystem ss = getStateSystem();
            if (ss == null) {
                schedule();
                return null;
            }
            if (!InputOutputAnalysisModule.this.waitForCompletion()) {
                return null;
            }
            Collection<Disk> disks = InputOutputInformationProvider.getDisks(InputOutputAnalysisModule.this);
            Map<Integer, Disk> quarkToDisk = new HashMap<>();
            for (Disk disk : disks) {
                int wqQuark = ss.optQuarkRelative(disk.getQuark(), fQueueAttribute);
                if (wqQuark == ITmfStateSystem.INVALID_ATTRIBUTE) {
                    continue;
                }
                List<Integer> subAttributes = ss.getSubAttributes(wqQuark, false);
                for (Integer subAttribute : subAttributes) {
                    quarkToDisk.put(subAttribute, disk);
                }
            }

            return new IOStateSystemSegmentStore(ss, quarkToDisk);
        }

    }

    /**
     * Get the waiting queue segment store
     *
     * @return The waiting queue segment store
     */
    public ISegmentStoreProvider getWaitingQueueSegmentStore() {
        return fWaitingQueueSs;
    }

    /**
     * Get the exec queue segment store
     *
     * @return The exec queue segment store
     */
    public ISegmentStoreProvider getExecutionQueueSegmentStore() {
        return fExecQueueSs;
    }

}
