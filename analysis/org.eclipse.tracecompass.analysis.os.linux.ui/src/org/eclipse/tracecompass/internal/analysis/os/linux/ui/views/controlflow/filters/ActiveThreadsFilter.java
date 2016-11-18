/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.filters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelThreadInformationProvider;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.ControlFlowEntry;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

/**
 * Provide active threads filtering for the Control Flow view.
 *
 * The Active Thread filter can be used in two modes: Show threads running on a
 * range of CPUs or show all threads considered active
 *
 * @author Jonathan Rajotte Julien
 */
public class ActiveThreadsFilter extends ViewerFilter {

    /** The filtering CPU ranges */
    private final @NonNull List<Range<Long>> fCpuRanges;
    /** The local cache for On CPU filtering */
    private @NonNull Set<Integer> fCachedOnCpusThreadForTimeRange = new HashSet<>();
    /** The local cache for Active Threads filtering */
    private @NonNull Set<Integer> fCachedActiveThreadForTimeRange = new HashSet<>();
    /** The cached time range */
    private TmfTimeRange fCachedTimeRange;
    /** Whether the filter is enabled */
    private boolean fEnabled = false;
    /** Whether the filter is an On CPU filter */
    private boolean fCpuRangesBasedFiltering = false;

    /**
     * Create an Active Threads filter with CPU ranges criteria.
     *
     * @param cpuRanges
     *            The CPU ranges for the filter if any.
     * @param cpuRangesBasedFiltering
     *            Whether or not to filter based on CPU ranges.
     */
    public ActiveThreadsFilter(List<Range<Long>> cpuRanges, boolean cpuRangesBasedFiltering) {
        super();
        if (cpuRanges != null) {
            fCpuRanges = ImmutableList.copyOf(cpuRanges);
        } else {
            fCpuRanges = new ArrayList<>();
        }
        fCpuRangesBasedFiltering = cpuRangesBasedFiltering;
    }

    /**
     * @return If the filter is enabled
     */
    public boolean isEnabled() {
        return fEnabled;
    }

    /**
     * @return If the filter is based on CPU ranges filtering
     */
    public boolean isCpuRangesBased() {
        return fCpuRangesBasedFiltering;
    }

    /**
     * Set the enabled state of the filter
     *
     * @param enabled
     *            The state of the filter
     */
    public void setEnabled(boolean enabled) {
        fEnabled = enabled;
    }

    /**
     * Get the CPU ranges of the filter
     *
     * @return The CPU ranges of the filter
     */
    public @NonNull List<Range<Long>> getCpuRanges() {
        return fCpuRanges;
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {

        if (!fEnabled || !(element instanceof ControlFlowEntry)) {
            return true;
        }
        ControlFlowEntry cfe = (ControlFlowEntry) element;

        /* Check if on CPU */
        if (fCpuRangesBasedFiltering && fCachedOnCpusThreadForTimeRange.contains(cfe.getThreadId())) {
            return true;
        } else if (fCachedActiveThreadForTimeRange.contains(cfe.getThreadId())) {
            return true;
        }

        /* Not active per see. Check children if any is active */
        for (TimeGraphEntry child : cfe.getChildren()) {
            if (select(viewer, cfe, child)) {
                return true;
            }
        }

        /* No children are active */
        return false;
    }

    private static @NonNull Set<Integer> getOnCpuThreads(List<Range<Long>> cpuRanges) {
        if ((cpuRanges == null) || cpuRanges.isEmpty()) {
            return new HashSet<>();
        }

        TmfTraceManager traceManager = TmfTraceManager.getInstance();
        TmfTraceContext traceContext = traceManager.getCurrentTraceContext();
        TmfTimeRange winRange = traceContext.getWindowRange();

        ITmfTrace trace = traceManager.getActiveTrace();

        if (trace == null) {
            return new HashSet<>();
        }

        KernelAnalysisModule kernelAnalysisModule = TmfTraceUtils.getAnalysisModuleOfClass(trace, KernelAnalysisModule.class, KernelAnalysisModule.ID);

        long beginTS = winRange.getStartTime().getValue();
        long endTS = winRange.getEndTime().getValue();

        if (kernelAnalysisModule == null) {
            return new HashSet<>();
        }

        /* Convert range to sets */
        @NonNull
        Set<@NonNull Long> cpus = new HashSet<>();
        for (Range<Long> range : cpuRanges) {
            Long minimum = range.lowerEndpoint();
            Long maximum = range.upperEndpoint();
            for (Long i = minimum; i <= maximum; i++) {
                cpus.add(i);
            }
        }

        Set<Integer> set = KernelThreadInformationProvider.getThreadsOfCpus(kernelAnalysisModule, cpus, beginTS, endTS);
        if (set == null) {
            set = new HashSet<>();
        }
        return set;
    }

    private static @NonNull Set<Integer> getActiveThreads() {
        TmfTraceManager traceManager = TmfTraceManager.getInstance();
        TmfTraceContext traceContext = traceManager.getCurrentTraceContext();
        TmfTimeRange winRange = traceContext.getWindowRange();

        ITmfTrace trace = traceManager.getActiveTrace();

        if (trace == null) {
            return new HashSet<>();
        }

        KernelAnalysisModule kernelModule = TmfTraceUtils.getAnalysisModuleOfClass(trace, KernelAnalysisModule.class, KernelAnalysisModule.ID);

        long beginTS = winRange.getStartTime().getValue();
        long endTS = winRange.getEndTime().getValue();

        if (kernelModule == null) {
            return new HashSet<>();
        }

        Set<Integer> set = KernelThreadInformationProvider.getActiveThreadsForRange(kernelModule, beginTS, endTS);
        if (set == null) {
            set = new HashSet<>();
        }
        return set;
    }

    /**
     * Update the filter internal data
     */
    public void updateData() {
        TmfTraceManager traceManager = TmfTraceManager.getInstance();
        TmfTraceContext traceContext = traceManager.getCurrentTraceContext();
        TmfTimeRange winRange = traceContext.getWindowRange();
        long beginTS = winRange.getStartTime().getValue();
        long endTS = winRange.getEndTime().getValue();

        TmfTimeRange timeRange = new TmfTimeRange(TmfTimestamp.fromNanos(beginTS), TmfTimestamp.fromNanos(endTS));

        /* Caching result for subsequent select() call for other entry */
        if (fEnabled && (fCachedTimeRange == null || !fCachedTimeRange.equals(timeRange))) {
            fCachedTimeRange = timeRange;
            if (fCpuRangesBasedFiltering) {
                fCachedOnCpusThreadForTimeRange = getOnCpuThreads(fCpuRanges);
            } else {
                fCachedActiveThreadForTimeRange = getActiveThreads();
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(fCpuRanges, fCpuRangesBasedFiltering, fEnabled);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ActiveThreadsFilter other = (ActiveThreadsFilter) obj;
        return Objects.equals(fCpuRanges, other.fCpuRanges) &&
                fCpuRangesBasedFiltering == other.fCpuRangesBasedFiltering &&
                fEnabled == other.fEnabled;
    }
}
