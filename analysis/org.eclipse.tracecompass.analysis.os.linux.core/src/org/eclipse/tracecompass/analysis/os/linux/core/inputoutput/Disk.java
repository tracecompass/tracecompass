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
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;

/**
 * This class represents a storage device in the system that behaves like a disk
 * from the operating system point of view. Concretely, it can be an HDD, an
 * SSD, a USB key, etc.
 *
 * @author Geneviève Bastien
 */
public class Disk {

    private static final int MINORBITS = 20;
    private static final int MINORMASK = (1 << MINORBITS) - 1;

    private final int fDev;
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
     * @return The device ID of this disk
     */
    public int getDeviceId() {
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
        return extractDeviceIdString(fDev);
    }

    protected static String extractDeviceIdString(int dev) {
        int major = dev >> MINORBITS;
        int minor = dev & MINORMASK;
        return major + "," + minor; //$NON-NLS-1$
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
    public double getSectorsAt(long ts, IoOperationType type) {

        ITmfStateSystem ss = fSs;

        /* Get the quark for the number of sector for the requested operation */
        int rwSectorQuark;
        if (type == IoOperationType.READ) {
            rwSectorQuark = ss.optQuarkRelative(fDiskQuark, Attributes.SECTORS_READ);
        } else if (type == IoOperationType.WRITE) {
            rwSectorQuark = ss.optQuarkRelative(fDiskQuark, Attributes.SECTORS_WRITTEN);
        } else {
            return 0;
        }

        long time = Math.max(ts, ss.getStartTime());
        time = Math.min(time, ss.getCurrentEndTime());

        try {
            List<ITmfStateInterval> states = ss.queryFullState(time);
            return extractCount(rwSectorQuark, ss, states, time);
        } catch (StateSystemDisposedException e) {
            Activator.getDefault().logError("Error getting disk IO Activity", e); //$NON-NLS-1$
        }
        return 0;
    }

    /**
     * Get the total number of sectors either read or written at the end of a time
     * range. This method will interpolate the requests that are in progress.
     *
     * @param sectorQuark
     *            the read or write sector quark for this Disk.
     * @param ss
     *            this disk's state system
     * @param states
     *            list of full states queried at the desired time.
     * @param ts
     *            the desired time
     * @return The number of sectors affected by operation at the end of the range
     */
    protected static double extractCount(int sectorQuark, ITmfStateSystem ss, List<ITmfStateInterval> states, long ts) {
        // Determine if we are handling read or write requests.
        String sectorName = ss.getAttributeName(sectorQuark);
        int rw = sectorName.equals(Attributes.SECTORS_READ) ? StateValues.READING_REQUEST : StateValues.WRITING_REQUEST;

        // Get the initial value.
        Object stateValue = states.get(sectorQuark).getValue();
        double count = (stateValue instanceof Number) ? ((Number) stateValue).doubleValue() : 0.0;

        // Find the driver queue root attribute
        int diskQuark = ss.getParentAttributeQuark(sectorQuark);
        int driverQ = ss.optQuarkRelative(diskQuark, Attributes.DRIVER_QUEUE);
        if (driverQ == ITmfStateSystem.INVALID_ATTRIBUTE) {
            return count;
        }

        /*
         * Interpolate the part of the requests in progress at requested time
         */
        for (Integer driverSlotQuark : ss.getSubAttributes(driverQ, false)) {
            int sizeQuark = ss.optQuarkRelative(driverSlotQuark, Attributes.REQUEST_SIZE);
            if (sizeQuark != ITmfStateSystem.INVALID_ATTRIBUTE) {
                ITmfStateInterval interval = states.get(sizeQuark);
                Object size = interval.getValue();
                if (size instanceof Number && Objects.equals(rw, states.get(driverSlotQuark).getValue())) {
                    /*
                     * The request is fully completed (and included in the r/w sectors) at interval
                     * end time + 1, so at interval end time, we do not expect the size to be total
                     * size
                     */
                    count += interpolate(ts, interval, (Number) size);
                }
            }
        }
        return count;
    }

    private static double interpolate(long ts, ITmfStateInterval interval, Number size) {
        long runningTime = interval.getEndTime() - interval.getStartTime() + 1;
        return (ts - interval.getStartTime()) * size.doubleValue() / runningTime;
    }

    /**
     * Return whether requests were made on this disk during the trace or not
     *
     * @return {@code true} if there was requests on this disk, {@code false}
     *         otherwise
     */
    public boolean hasActivity() {
        return queueIsActive(Attributes.WAITING_QUEUE) && queueIsActive(Attributes.DRIVER_QUEUE);
    }

    private boolean queueIsActive(String queue) {
        int quark = fSs.optQuarkRelative(fDiskQuark, queue);
        return quark != ITmfStateSystem.INVALID_ATTRIBUTE && !fSs.getSubAttributes(quark, false).isEmpty();
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
        return Objects.hash(fSs, fDev);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o instanceof Disk) {
            Disk disk = (Disk) o;
            return fSs.equals(disk.fSs) && fDev == disk.fDev;
        }
        return false;
    }

}
