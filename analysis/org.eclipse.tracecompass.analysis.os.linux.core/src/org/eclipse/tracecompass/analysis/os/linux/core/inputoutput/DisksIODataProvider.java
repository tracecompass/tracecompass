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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.Messages;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.AbstractStateSystemAnalysisDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.TmfCommonXAxisResponseFactory;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.internal.tmf.core.model.YModel;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;

/**
 * This data provider will return a XY model (model is wrapped in a response)
 * based on a query filter. The model is used afterwards by any viewer to draw
 * charts. Model returned is for Disks I/O views
 *
 * @author Yonni Chen
 */
@SuppressWarnings("restriction")
public class DisksIODataProvider extends AbstractStateSystemAnalysisDataProvider implements ITmfTreeXYDataProvider<TmfTreeDataModel> {

    /**
     * Title used to create XY models for the {@link DisksIODataProvider}.
     */
    protected static final String PROVIDER_TITLE = Objects.requireNonNull(Messages.DisksIODataProvider_title);

    /**
     * Extension point ID.
     */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.DisksIODataProvider"; //$NON-NLS-1$

    /**
     * Inline class to encapsulate all the values required to build a series. Allows
     * for reuse of full query results to be faster than {@link Disk}.
     */
    private static final class DiskBuilder {

        private static final int BYTES_PER_SECTOR = 512;
        private static final double SECONDS_PER_NANOSECOND = 10E-9;
        private static final double RATIO = BYTES_PER_SECTOR / SECONDS_PER_NANOSECOND;

        /** This series' sector quark. public because final */
        public final int fSectorQuark;
        private final long fId;
        private final double[] fValues;
        private double fPrevCount;

        /**
         * Constructor
         *
         * @param id
         *            the unique ID for a disks read or write series.
         * @param sectorQuark
         *            sector quark
         * @param length
         *            desired length of the series
         */
        private DiskBuilder(long id, int sectorQuark, int length) {
            fId = id;
            fSectorQuark = sectorQuark;
            fValues = new double[length];
        }

        private void setPrevCount(double prevCount) {
            fPrevCount = prevCount;
        }

        /**
         * Update the value for the counter at the desired index. Use in increasing
         * order of position
         *
         * @param pos
         *            index to update
         * @param newCount
         *            new number of read / written sectors
         * @param deltaT
         *            time difference to the previous value for interpolation
         */
        private void updateValue(int pos, double newCount, long deltaT) {
            /**
             * Linear interpolation to compute the disk throughput between time and the
             * previous time, from the number of sectors at each time.
             */
            fValues[pos] = (newCount - fPrevCount) * RATIO / deltaT;
            fPrevCount = newCount;
        }

        private IYModel build() {
            return new YModel(String.valueOf(fId), fValues);
        }
    }

    private static final AtomicLong ENTRY_ID = new AtomicLong();

    private final InputOutputAnalysisModule fModule;
    private final BiMap<Long, Integer> fIdToSectorQuark = HashBiMap.create();
    private @Nullable TmfModelResponse<List<TmfTreeDataModel>> fCached = null;
    private final long fTraceId = ENTRY_ID.getAndIncrement();

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
        fModule.waitForInitialization();
        ITmfStateSystem ss = fModule.getStateSystem();
        if (ss == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.STATE_SYSTEM_FAILED);
        }
        boolean complete = ss.waitUntilBuilt(0);

        List<TmfTreeDataModel> nodes = new ArrayList<>();
        nodes.add(new TmfTreeDataModel(fTraceId, -1, getTrace().getName()));

        String readName = Objects.requireNonNull(Messages.DisksIODataProvider_read);
        String writeName = Objects.requireNonNull(Messages.DisksIODataProvider_write);

        for (Integer diskQuark : ss.getQuarks(Attributes.DISKS, "*")) { //$NON-NLS-1$
            String diskName = getDiskName(ss, diskQuark);
            long diskId = getId(diskQuark);
            nodes.add(new TmfTreeDataModel(diskId, fTraceId, diskName));

            int readQuark = ss.optQuarkRelative(diskQuark, Attributes.SECTORS_READ);
            if (readQuark != ITmfStateSystem.INVALID_ATTRIBUTE) {
                nodes.add(new TmfTreeDataModel(getId(readQuark), diskId, readName));
            }

            int writeQuark = ss.optQuarkRelative(diskQuark, Attributes.SECTORS_WRITTEN);
            if (writeQuark != ITmfStateSystem.INVALID_ATTRIBUTE) {
                nodes.add(new TmfTreeDataModel(getId(writeQuark), diskId, writeName));
            }
        }

        if (complete) {
            TmfModelResponse<List<TmfTreeDataModel>> response = new TmfModelResponse<>(nodes, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
            fCached = response;
            return response;
        }
        return new TmfModelResponse<>(nodes, ITmfResponse.Status.RUNNING, CommonStatusMessage.RUNNING);
    }

    private long getId(int quark) {
        Long id = fIdToSectorQuark.inverse().get(quark);
        if (id == null) {
            id = ENTRY_ID.getAndIncrement();
            fIdToSectorQuark.put(id, quark);
        }
        return id;
    }

    private static String getDiskName(ITmfStateSystem ss, Integer diskQuark) {
        ITmfStateInterval interval = StateSystemUtils.queryUntilNonNullValue(ss, diskQuark, ss.getStartTime(), ss.getCurrentEndTime());
        if (interval != null) {
            return String.valueOf(interval.getValue());
        }
        int devNum = Integer.parseInt(ss.getAttributeName(diskQuark));
        return Disk.extractDeviceIdString(devNum);
    }

    @Override
    public TmfModelResponse<ITmfCommonXAxisModel> fetchXY(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        TmfModelResponse<ITmfCommonXAxisModel> res = verifyParameters(fModule, filter, monitor);
        if (res != null) {
            return res;
        }

        @NonNull ITmfStateSystem ss = Objects.requireNonNull(fModule.getStateSystem(), "Statesystem should have been verified by verifyParameters"); //$NON-NLS-1$
        long[] xValues = filter.getTimesRequested();
        List<DiskBuilder> builders = initBuilders(ss, filter);
        if (builders.isEmpty()) {
            // this would return an empty map even if we did the queries.
            return TmfCommonXAxisResponseFactory.create(PROVIDER_TITLE, xValues, Collections.emptyMap(), true);
        }

        long currentEnd = ss.getCurrentEndTime();
        try {
            long prevTime = filter.getStart();
            if (prevTime >= ss.getStartTime() && prevTime <= currentEnd) {
                // reuse the results from the full query
                List<ITmfStateInterval> states = ss.queryFullState(prevTime);

                for (DiskBuilder entry : builders) {
                    entry.setPrevCount(Disk.extractCount(entry.fSectorQuark, ss, states, prevTime));
                }
            }

            for (int i = 1; i < xValues.length; i++) {
                if (monitor != null && monitor.isCanceled()) {
                    return TmfCommonXAxisResponseFactory.createCancelledResponse(CommonStatusMessage.TASK_CANCELLED);
                }
                long time = xValues[i];
                if (time > currentEnd) {
                    break;
                } else if (time >= ss.getStartTime()) {
                    // reuse the results from the full query
                    List<ITmfStateInterval> states = ss.queryFullState(time);

                    for (DiskBuilder entry : builders) {
                        double count = Disk.extractCount(entry.fSectorQuark, ss, states, time);
                        entry.updateValue(i, count, time - prevTime);
                    }
                }
                prevTime = time;
            }
            ImmutableMap.Builder<String, IYModel> ySeries = ImmutableMap.builder();
            for (DiskBuilder entry : builders) {
                IYModel model = entry.build();
                ySeries.put(model.getName(), model);
            }
            boolean complete = ss.waitUntilBuilt(0) || filter.getEnd() <= currentEnd;
            return TmfCommonXAxisResponseFactory.create(PROVIDER_TITLE, xValues, ySeries.build(), complete);
        } catch (StateSystemDisposedException e) {
            return TmfCommonXAxisResponseFactory.createFailedResponse(e.getMessage());
        }
    }

    private List<DiskBuilder> initBuilders(ITmfStateSystem ss, TimeQueryFilter filter) {
        if (!(filter instanceof SelectionTimeQueryFilter)) {
            return Collections.emptyList();
        }

        int length = filter.getTimesRequested().length;
        List<DiskBuilder> builders = new ArrayList<>();
        for (Long id : ((SelectionTimeQueryFilter) filter).getSelectedItems()) {
            Integer quark = fIdToSectorQuark.get(id);
            if (quark != null && (ss.getAttributeName(quark).equals(Attributes.SECTORS_READ) ||
                    ss.getAttributeName(quark).equals(Attributes.SECTORS_WRITTEN))) {
                builders.add(new DiskBuilder(id, quark, length));
            }
        }
        return builders;
    }
}
