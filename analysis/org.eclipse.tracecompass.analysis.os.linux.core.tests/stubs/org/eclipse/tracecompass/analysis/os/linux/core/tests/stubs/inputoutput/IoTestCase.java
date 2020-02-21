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
package org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.inputoutput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.IoOperationType;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.LinuxTestCase;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Test case with additional information for IO test cases
 *
 * @author Geneviève Bastien
 */
public class IoTestCase extends LinuxTestCase {

    /**
     * Constructor
     *
     * @param filename
     *            The Name of the file containing the trace for this test case
     */
    public IoTestCase(String filename) {
        super(filename);
    }

    /**
     * Class to store sector count information
     */
    public static class SectorCountInfo {
        private final long fTs;
        private final IoOperationType fType;
        private final long fSectors;

        /**
         * Constructor
         *
         * @param ts
         *            The timestamp at which to test
         * @param type
         *            The type of IO operation to test
         * @param nbSectors
         *            The expected number of sectors at this timestamp
         */
        public SectorCountInfo(long ts, IoOperationType type, long nbSectors) {
            fTs = ts;
            fType = type;
            fSectors = nbSectors;
        }

        /**
         * Get the timestamp to test
         *
         * @return The timestamp at which to test
         */
        public long getTimestamp() {
            return fTs;
        }

        /**
         * Get the type of IO operation
         *
         * @return The type of IO operation
         */
        public IoOperationType getType() {
            return fType;
        }

        /**
         * Get the expected number of sectors at this timestamp
         *
         * @return The expected number of sectors
         */
        public double getNbSectors() {
            return fSectors;
        }
    }

    /**
     * Class to contain information on a disk
     */
    public static class DiskInfo {
        private final String fDeviceString;
        private final String fDiskName;
        private final boolean fActive;

        /**
         * Constructor
         *
         * @param deviceString
         *            The device ID string, as obtained with ls -al /dev
         * @param diskname
         *            The real human-readable name of the disk. If a name is not
         *            available, this value should be equal to the deviceString
         * @param active
         *            Whether there is activity on this disk
         */
        public DiskInfo(String deviceString, String diskname, boolean active) {
            fDeviceString = deviceString;
            fDiskName = diskname;
            fActive = active;
        }

        /**
         * Get the device ID string for this disk
         *
         * @return The device ID string
         */
        public String getDeviceString() {
            return fDeviceString;
        }

        /**
         * Get the device name of the disk
         *
         * @return The device name
         */
        public String getDeviceName() {
            return fDiskName;
        }

        /**
         * Return whether the disk had activity during the trace
         *
         * @return Whether the disk had activity
         */
        public boolean hasActivity() {
            return fActive;
        }
    }

    /**
     * A class to contain disk activity information
     */
    public static class DiskActivity {
        private static final String READ_NAME = "read";
        private static final String WRITE_NAME = "write";

        private final long fStartTime;
        private final long fEndTime;
        private final int fResolution;
        private final double[] fActivity;
        private final IoOperationType fType;
        private final String fDiskName;

        /**
         * Constructor
         *
         * @param startTime
         *            Start time of the request
         * @param endTime
         *            End time of the request
         * @param res
         *            Resolution of the request
         * @param activity
         *            Activity at different query time
         * @param type
         *            Activity type
         * @param diskName
         *            The name of the disk to test
         */
        public DiskActivity(long startTime, long endTime, int res, double[] activity, IoOperationType type, String diskName) {
            fStartTime = startTime;
            fEndTime = endTime;
            fResolution = res;
            fActivity = activity;
            fType = type;
            fDiskName = diskName;
        }

        /**
         * Get the time query given the test parameters
         *
         * @return The parameter map for the time query
         */
        public Map<String, Object> getTimeQuery() {
            TimeQueryFilter filter = new TimeQueryFilter(fStartTime, fEndTime, fResolution);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put(DataProviderParameterUtils.REQUESTED_TIME_KEY, getTimeRequested(filter));
            return parameters;
        }

        /**
         * Get the time query to retrieve the data. It will query only one item,
         * the one corresponding to what is being tested.
         *
         * @param model
         *            The list of tree models available
         * @return The query
         */
        public Map<String, Object> getTimeQueryForModel(TmfTreeModel<TmfTreeDataModel> model) {
            long diskId = getDiskId(model);
            long selectionId = -1;
            for (TmfTreeDataModel oneModel : model.getEntries()) {
                if (oneModel.getParentId() == diskId) {
                    switch (fType) {
                    case READ:
                        if (oneModel.getName().equals(READ_NAME)) {
                            selectionId = oneModel.getId();
                        }
                        break;
                    case WRITE:
                        if (oneModel.getName().equals(WRITE_NAME)) {
                            selectionId = oneModel.getId();
                        }
                        break;
                    default:
                        throw new IllegalStateException("Unknown type");
                    }
                }
                oneModel.getName();
            }
            if (selectionId == -1) {
                throw new NoSuchElementException("Requested entry not found for " + fDiskName + ' ' + fType);
            }
            Map<String, Object> parameters = getTimeQuery();
            parameters.put(DataProviderParameterUtils.REQUESTED_ITEMS_KEY, Collections.singletonList(selectionId));
            return parameters;
        }

        private long getDiskId(TmfTreeModel<TmfTreeDataModel> model) {
            for (TmfTreeDataModel oneModel : model.getEntries()) {
                if (fDiskName.equals(oneModel.getName())) {
                    return oneModel.getId();
                }
            }
            throw new NoSuchElementException("Disk not found " + fDiskName);
        }

        /**
         * Get the activity of this test case
         *
         * @return The collection of values for the given time query
         */
        public double[] getActivity() {
            return fActivity;
        }

        @Override
        public String toString() {
            return fDiskName + ' ' + fType + " time range: " + fStartTime + ',' + fEndTime;
        }
    }

    /**
     * Get a collection of sector count information to test
     *
     * @return A collection of sector count information
     */
    public Multimap<Integer, SectorCountInfo> getSectorCount() {
        return HashMultimap.create();
    }

    /**
     * Get a mapping of device ID to disk information for a disk
     *
     * @return A mapping of device ID to disk information
     */
    public Map<Integer, DiskInfo> getDiskInfo() {
        return Collections.EMPTY_MAP;
    }

    /**
     * Get a list of disk activity to request
     *
     * @return A list of disk activity to test
     */
    public Collection<DiskActivity> getDiskActivity() {
        return Collections.emptyList();
    }

    private static List<Long> getTimeRequested(TimeQueryFilter filter) {
        List<Long> times = new ArrayList<>();
        for (long time : filter.getTimesRequested()) {
            times.add(time);
        }
        return times;
    }

}
