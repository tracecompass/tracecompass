/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.statistics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.SystemCall;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.SystemCallLatencyAnalysis;
import org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.statistics.SegmentStoreStatistics;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.ImmutableList;

/**
 * Analysis module to calculate statistics of a latency analysis
 *
 * @author Bernd Hufmann
 */
public class SystemCallLatencyStatisticsAnalysisModule extends TmfAbstractAnalysisModule {

    /** The analysis module ID */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.core.latency.statistics.syscall"; //$NON-NLS-1$

    private @Nullable SystemCallLatencyAnalysis fLatencyModule;

    private @Nullable SegmentStoreStatistics fTotalStats;

    private @Nullable Map<String, SegmentStoreStatistics> fPerSyscallStats;

    @Override
    protected Iterable<IAnalysisModule> getDependentAnalyses() {
        ITmfTrace trace = getTrace();
        if (trace != null) {
            SystemCallLatencyAnalysis module = TmfTraceUtils.getAnalysisModuleOfClass(trace, SystemCallLatencyAnalysis.class, SystemCallLatencyAnalysis.ID);
            if (module != null) {
                fLatencyModule = module;
                return ImmutableList.of((IAnalysisModule) module);
            }
        }
        return super.getDependentAnalyses();
    }

    @Override
    protected boolean executeAnalysis(IProgressMonitor monitor) throws TmfAnalysisException {
        SystemCallLatencyAnalysis latency = fLatencyModule;
        ITmfTrace trace = getTrace();
        if ((latency == null) || (trace == null)) {
            return false;
        }
        latency.waitForCompletion();

        ISegmentStore<ISegment> segStore = latency.getSegmentStore();

        if (segStore != null) {

            boolean result = calculateTotalManual(segStore, monitor);

            if (!result) {
                return false;
            }

            result = calculateTotalPerSyscall(segStore, monitor);
            if (!result) {
                return false;
            }
        }
        return true;
    }

    private boolean calculateTotalManual(ISegmentStore<ISegment> store, IProgressMonitor monitor) {
        SegmentStoreStatistics total = new SegmentStoreStatistics();
        Iterator<ISegment> iter = store.iterator();
        while (iter.hasNext()) {
            if (monitor.isCanceled()) {
                return false;
            }
            ISegment segment = iter.next();
            total.update(segment);
        }
        fTotalStats = total;
        return true;
    }

    private boolean calculateTotalPerSyscall(ISegmentStore<ISegment> store, IProgressMonitor monitor) {
        Map<String, SegmentStoreStatistics> perSyscallStats = new HashMap<>();

        Iterator<ISegment> iter = store.iterator();
        while (iter.hasNext()) {
            if (monitor.isCanceled()) {
                return false;
            }
            ISegment segment = iter.next();
            if (segment instanceof SystemCall) {
                SystemCall syscall = (SystemCall) segment;
                SegmentStoreStatistics values = perSyscallStats.get(syscall.getName());
                if (values == null) {
                    values = new SegmentStoreStatistics();
                }
                values.update(segment);
                perSyscallStats.put(syscall.getName(), values);
            }
        }
        fPerSyscallStats = perSyscallStats;
        return true;
    }

    @Override
    protected void canceling() {
    }

    /**
     * The total statistics
     *
     * @return the total statistics
     */
    public @Nullable SegmentStoreStatistics getTotalStats() {
        return fTotalStats;
    }

    /**
     * The per syscall statistics
     *
     * @return the per syscall statistics
     */
    public @Nullable Map<String, SegmentStoreStatistics> getPerSyscallStats() {
        return fPerSyscallStats;
    }

 }
