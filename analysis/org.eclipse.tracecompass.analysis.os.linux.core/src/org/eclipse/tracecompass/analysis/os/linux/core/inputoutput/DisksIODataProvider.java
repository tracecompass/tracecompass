/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.inputoutput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.Messages;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.AbstractStateSystemAnalysisDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.TmfCommonXAxisResponseFactory;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfXYDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.internal.tmf.core.model.YModel;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

/**
 * This data provider will return a XY model (model is wrapped in a response)
 * based on a query filter. The model is used afterwards by any viewer to draw
 * charts. Model returned is for Disks I/O views
 *
 * @author Yonni Chen
 */
@SuppressWarnings("restriction")
public class DisksIODataProvider extends AbstractStateSystemAnalysisDataProvider implements ITmfXYDataProvider, ITmfTreeDataProvider<TmfTreeDataModel> {

    /**
     * Extension point ID.
     */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.DisksIODataProvider"; //$NON-NLS-1$

    private static final int BYTES_PER_SECTOR = 512;
    private static final double SECONDS_PER_NANOSECOND = Math.pow(10, -9);
    private static final AtomicLong ENTRY_ID = new AtomicLong();

    private final InputOutputAnalysisModule fModule;
    private final Map<Long, Integer> fIdToSectorQuark = new HashMap<>();
    private @Nullable TmfModelResponse<List<TmfTreeDataModel>> fCached = null;

    /**
     * Create an instance of {@link DisksIODataProvider}. Returns a null instance if
     * the analysis module is not found.
     *
     * @param trace
     *            A trace on which we are interested to fetch a model
     * @return A {@link DisksIODataProvider} instance. If analysis module is not
     *         found, it returns null
     */
    public static @Nullable DisksIODataProvider create(ITmfTrace trace) {
        InputOutputAnalysisModule module = TmfTraceUtils.getAnalysisModuleOfClass(trace, InputOutputAnalysisModule.class, InputOutputAnalysisModule.ID);
        if (module != null) {
            module.schedule();
            return new DisksIODataProvider(trace, module);
        }
        return null;
    }

    /**
     * Constructor
     */
    private DisksIODataProvider(ITmfTrace trace, InputOutputAnalysisModule module) {
        super(trace);
        fModule = module;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public TmfModelResponse<List<TmfTreeDataModel>> fetchTree(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        if (fCached != null) {
            return fCached;
        }
        fModule.waitForCompletion();
        ITmfStateSystem ss = fModule.getStateSystem();
        if (ss == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.STATE_SYSTEM_FAILED);
        }

        List<TmfTreeDataModel> nodes = new ArrayList<>();

        long traceId = ENTRY_ID.getAndIncrement();
        nodes.add(new TmfTreeDataModel(traceId, -1, getTrace().getName()));

        String readName = Objects.requireNonNull(Messages.DisksIODataProvider_read);
        String writeName = Objects.requireNonNull(Messages.DisksIODataProvider_write);

        for (Integer diskQuark : ss.getQuarks(Attributes.DISKS, "*")) { //$NON-NLS-1$
            String diskName = ss.getAttributeName(diskQuark);
            long diskId = ENTRY_ID.getAndIncrement();
            nodes.add(new TmfTreeDataModel(diskId, traceId, diskName));

            int readQuark = ss.optQuarkRelative(diskQuark, Attributes.SECTORS_READ);
            if (readQuark != ITmfStateSystem.INVALID_ATTRIBUTE) {
                long readId = ENTRY_ID.getAndIncrement();
                fIdToSectorQuark.put(readId, readQuark);
                nodes.add(new TmfTreeDataModel(readId, diskId, readName));
            }

            int writeQuark = ss.optQuarkRelative(diskQuark, Attributes.SECTORS_WRITTEN);
            if (writeQuark != ITmfStateSystem.INVALID_ATTRIBUTE) {
                long writeId = ENTRY_ID.getAndIncrement();
                fIdToSectorQuark.put(writeId, writeQuark);
                nodes.add(new TmfTreeDataModel(writeId, diskId, writeName));
            }
        }

        TmfModelResponse<List<TmfTreeDataModel>> response = new TmfModelResponse<>(nodes, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        fCached = response;
        return response;
    }

    @Override
    public TmfModelResponse<ITmfCommonXAxisModel> fetchXY(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {

        TmfModelResponse<ITmfCommonXAxisModel> res = verifyParameters(fModule, filter, monitor);
        if (res != null) {
            return res;
        }

        @NonNull ITmfStateSystem ss = Objects.requireNonNull(fModule.getStateSystem(), "Statesystem should have been verified by verifyParameters"); //$NON-NLS-1$

        long queryStart = filter.getStart();
        long[] xValues = filter.getTimesRequested();

        ImmutableMap.Builder<String, IYModel> ySeries = ImmutableMap.builder();
        Collection<Disk> disks = InputOutputInformationProvider.getDisks(fModule);
        long currentEnd = ss.getCurrentEndTime();

        for (Disk disk : Iterables.filter(disks, Disk::hasActivity)) {
            String diskName = disk.getDiskName();

            /* Initialize quarks and series names */
            double[] yValuesWritten = new double[xValues.length];
            double[] yValuesRead = new double[xValues.length];

            String seriesNameWritten = diskName + Messages.DisksIODataProvider_write;
            String seriesNameRead = diskName + Messages.DisksIODataProvider_read;

            long prevTime = queryStart;
            long prevCountRead = disk.getSectorsAt(prevTime, IoOperationType.READ);
            long prevCountWrite = disk.getSectorsAt(prevTime, IoOperationType.WRITE);
            for (int i = 1; i < xValues.length; i++) {
                if (monitor != null && monitor.isCanceled()) {
                    return TmfCommonXAxisResponseFactory.createCancelledResponse(CommonStatusMessage.TASK_CANCELLED);
                }

                long time = xValues[i];
                if (time >= ss.getStartTime() && time <= currentEnd) {
                    long count = disk.getSectorsAt(time, IoOperationType.WRITE);
                    yValuesWritten[i] = interpolate(prevTime, time, prevCountWrite, count);
                    prevCountWrite = count;
                    count = disk.getSectorsAt(time, IoOperationType.READ);
                    yValuesRead[i] = interpolate(prevTime, time, prevCountRead, count);
                    prevCountRead = count;
                }
                prevTime = time;
            }

            ySeries.put(seriesNameRead, new YModel(seriesNameRead, yValuesRead));
            ySeries.put(seriesNameWritten, new YModel(seriesNameWritten, yValuesWritten));
            if (monitor != null && monitor.isCanceled()) {
                return TmfCommonXAxisResponseFactory.createCancelledResponse(CommonStatusMessage.TASK_CANCELLED);
            }
        }
        boolean complete = ss.waitUntilBuilt(0) || filter.getEnd() <= currentEnd;
        return TmfCommonXAxisResponseFactory.create(Objects.requireNonNull(Messages.DisksIODataProvider_title), xValues, ySeries.build(), complete);
    }

    /**
     * Linear interpolation to compute the disk throughput between time and the
     * previous time, from the number of sectors at each time.
     */
    private static double interpolate(long prevTime, long time, long prevCount, long count) {
        return (count - prevCount) * BYTES_PER_SECTOR / ((time - prevTime) * SECONDS_PER_NANOSECOND);
    }
}
