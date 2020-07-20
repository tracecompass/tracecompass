/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.segment.interfaces.INamedSegment;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;

/**
 * @since 2.0
 */
public class RequestIntervalSegment implements INamedSegment {

    /**
     *
     */
    private static final long serialVersionUID = 3064409294604514508L;
    private final ITmfStateInterval fInterval;
    private final IoOperationType fType;
    private final String fDiskName;

    /**
     * Create a request segment from an interval
     *
     * @param interval
     *            The base interval for this request, whose value contains the
     *            type of disk request
     * @param disk
     *            The disks this request is from
     * @return The new request or null if the interval does not contain a value
     */
    public static @Nullable RequestIntervalSegment create(ITmfStateInterval interval, @Nullable Disk disk) {
        Object value = interval.getValue();
        if (value instanceof Integer) {
            return new RequestIntervalSegment(interval, IoOperationType.fromNumber((Integer) value), disk);
        }
        return null;
    }

    private RequestIntervalSegment(ITmfStateInterval interval, IoOperationType ioOperationType, @Nullable Disk disk) {
        fInterval = interval;
        fType = ioOperationType;
        fDiskName = disk != null ? disk.getDiskName() : ""; //$NON-NLS-1$
    }

    @Override
    public int compareTo(@NonNull ISegment o) {

        return 0;
    }

    @Override
    public long getStart() {
        return fInterval.getStartTime();
    }

    @Override
    public long getEnd() {
        /* Return the +1 to include also the end time stamp
         * TODO: It should not return a timestamp after the end time of the state system */
        return fInterval.getEndTime() + 1;
    }

    @Override
    public String getName() {
        return fDiskName.isEmpty() ? String.valueOf(fType) : fDiskName + ' ' + ':' + ' ' + String.valueOf(fType);
    }

    /**
     * Get the type of this request operation
     *
     * @return The operation type
     */
    public IoOperationType getOperationType() {
        return fType;
    }

    /**
     * Get the name of the disk
     *
     * @return The name of the disk
     */
    public @Nullable String getDiskName() {
        return fDiskName;
    }

}
