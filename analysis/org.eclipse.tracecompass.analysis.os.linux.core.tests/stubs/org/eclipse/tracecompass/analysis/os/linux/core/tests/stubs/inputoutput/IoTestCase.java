/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.inputoutput;

import java.util.Collections;
import java.util.Map;

import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.IoOperationType;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.LinuxTestCase;

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

}
