/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.os.linux.core.inputoutput;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * This class represents a storage device in the system that behaves like a disk
 * from the operating system point of view. Concretely, it can be an HDD, an
 * SSD, a USB key, etc.
 *
 * @author Geneviève Bastien
 * @since 2.0
 */
public class Disk {

    private static final HashFunction HF = NonNullUtils.checkNotNull(Hashing.goodFastHash(32));

    private static final Integer MINORBITS = 20;
    private static final Integer MINORMASK = ((1 << MINORBITS) - 1);

    private final Integer fDev;
    private final int fDiskQuark;
    private final ITmfStateSystem fSs;
    private @Nullable String fDiskName = null;

    /**
     * Constructor
     *
     * @param dev
     *            The device number of the disk
     * @param ss
     *            The state system this disk will be saved to
     * @param diskQuark
     *            The quark of this disk in the state system
     */
    public Disk(Integer dev, ITmfStateSystem ss, int diskQuark) {
        fDev = dev;
        fSs = ss;
        fDiskQuark = diskQuark;
        ITmfStateInterval diskNameInterval = StateSystemUtils.queryUntilNonNullValue(ss, diskQuark, ss.getStartTime(), ss.getCurrentEndTime());
        if (diskNameInterval != null) {
            fDiskName = diskNameInterval.getStateValue().unboxStr();
        }
    }

    /**
     * Get the device ID of this device
     *
     * @return The devide ID of this disk
     */
    public Integer getDevideId() {
        return fDev;
    }

    /**
     * Get the disk name if available. If the disk name is not set, this method
     * will return the string corresponding to the major, minor value of the
     * disk's ID, ie the return value of {@link #getDeviceIdString()}.
     *
     * @return The disk name or the value returned by
     *         {@link #getDeviceIdString()}
     */
    public String getDiskName() {
        String diskName = fDiskName;
        if (diskName == null) {
            return getDeviceIdString();
        }
        return diskName;
    }

    /**
     * Get the quark
     *
     * @return The quark of this disk in the state system
     */
    public int getQuark() {
        return fDiskQuark;
    }

    /**
     * Set the human readable disk name of this device
     *
     * @param diskname
     *            The human readable name of the disk
     */
    public void setDiskName(String diskname) {
        fDiskName = diskname;
    }

    /**
     * Return the disk's device ID as a major,minor string. Those major,minor
     * numbers correspond to the number of the disk found when listing disk with
     * ls -al /dev, or using lsblk in Linux.
     *
     * @return The device ID string as major,minor
     */
    public String getDeviceIdString() {
        Integer major = fDev >> MINORBITS;
        Integer minor = fDev & MINORMASK;
        return major.toString() + ',' + minor.toString();
    }

    /**
     * Get the total number of sectors either read or written at the end of a
     * time range. This method will interpolate the requests that are in
     * progress.
     *
     * @param ts
     *            The start of the time range to query
     * @param type
     *            The type of IO operation to query
     * @return The number of sectors affected by operation at the end of the
     *         range
     */
    public long getSectorsAt(long ts, IoOperationType type) {

        ITmfStateSystem ss = fSs;
        long currentCount = 0;

        /* Get the quark for the number of sector for the requested operation */
        int rwSectorQuark = ITmfStateSystem.INVALID_ATTRIBUTE;
        if (type == IoOperationType.READ) {
            rwSectorQuark = ss.optQuarkRelative(fDiskQuark, Attributes.SECTORS_READ);
        } else if (type == IoOperationType.WRITE) {
            rwSectorQuark = ss.optQuarkRelative(fDiskQuark, Attributes.SECTORS_WRITTEN);
        }
        if (rwSectorQuark == ITmfStateSystem.INVALID_ATTRIBUTE) {
            return currentCount;
        }

        int rw = type == IoOperationType.READ ? StateValues.READING_REQUEST : StateValues.WRITING_REQUEST;

        long time = Math.max(ts, ss.getStartTime());
        time = Math.min(time, ss.getCurrentEndTime());

        try {
            List<ITmfStateInterval> states = ss.queryFullState(time);
            long count = states.get(rwSectorQuark).getStateValue().unboxLong();
            if (count == -1) {
                count = 0;
            }
            Integer driverQ = ss.getQuarkRelative(fDiskQuark, Attributes.DRIVER_QUEUE);

            /*
             * Interpolate the part of the requests in progress at requested
             * time
             */
            for (Integer driverSlotQuark : ss.getSubAttributes(driverQ, false)) {
                int sizeQuark = ss.getQuarkRelative(driverSlotQuark, Attributes.REQUEST_SIZE);
                ITmfStateInterval interval = states.get(sizeQuark);
                if (!interval.getStateValue().isNull()) {
                    if (states.get(driverSlotQuark).getStateValue().unboxInt() == rw) {
                        /*
                         * The request is fully completed (and included in the
                         * r/w sectors) at interval end time + 1, so at interval
                         * end time, we do not expect the size to be total size
                         */
                        long runningTime = interval.getEndTime() - interval.getStartTime() + 1;
                        long runningEnd = interval.getEndTime() + 1;
                        long startsize = interval.getStateValue().unboxLong();
                        count = interpolateCount(count, time, runningEnd, runningTime, startsize);
                    }
                }
            }
            currentCount = count;
        } catch (StateSystemDisposedException | AttributeNotFoundException e) {
            Activator.getDefault().logError("Error getting disk IO Activity", e); //$NON-NLS-1$
        }
        return currentCount;
    }

    private static long interpolateCount(long count, long ts, long runningEnd, long runningTime, long size) {

        long newCount = count;
        if (runningTime > 0) {
            long runningStart = runningEnd - runningTime;
            if (ts < runningStart) {
                return newCount;
            }
            double interpolation = (double) (ts - runningStart) * (double) size / (runningTime);
            /* Will truncate the decimal part */
            newCount += (long) interpolation;
        }
        return newCount;
    }

    /**
     * Return whether requests were made on this disk during the trace or not
     *
     * @return {@code true} if there was requests on this disk, {@code false}
     *         otherwise
     */
    public boolean hasActivity() {
        try {
            int wqQuark = fSs.getQuarkRelative(fDiskQuark, Attributes.WAITING_QUEUE);
            if (fSs.getSubAttributes(wqQuark, false).size() > 0) {
                return true;
            }
            int dqQuark = fSs.getQuarkRelative(fDiskQuark, Attributes.DRIVER_QUEUE);
            if (fSs.getSubAttributes(dqQuark, false).size() > 0) {
                return true;
            }
        } catch (AttributeNotFoundException e) {
        }
        return false;
    }

    // ----------------------------------------------------
    // Object methods
    // ----------------------------------------------------

    @Override
    public String toString() {
        return "Disk: [" + getDeviceIdString() + ',' + fDiskName + ']'; //$NON-NLS-1$
    }

    @Override
    public int hashCode() {
        return HF.newHasher().putInt(fDev).hash().asInt();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o instanceof Disk) {
            Disk disk = (Disk) o;
            if (fDev.equals(disk.fDev)) {
                return true;
            }
        }
        return false;
    }

}
