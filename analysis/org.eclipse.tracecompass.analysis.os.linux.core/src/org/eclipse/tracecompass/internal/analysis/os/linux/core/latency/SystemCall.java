/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.latency;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.datastore.core.interval.IHTIntervalReader;
import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferWriter;
import org.eclipse.tracecompass.datastore.core.serialization.SafeByteBufferFactory;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.segment.interfaces.INamedSegment;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfCallsite;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfSourceLookup;

/**
 * A linux kernel system call, represented as an {@link ISegment}.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public final class SystemCall implements INamedSegment, ITmfSourceLookup {

    private static final long serialVersionUID = 1554494342105208730L;

    /**
     * The reader for this segment class
     */
    public static final IHTIntervalReader<ISegment> READER = buffer -> new SystemCall(buffer.getLong(), buffer.getLong(), buffer.getString(), buffer.getInt(), buffer.getInt());

    /**
     * The subset of information that is available from the syscall entry event.
     */
    public static class InitialInfo {

        private long fStartTime;
        private String fName;
        private int fTid;

        /**
         * @param startTime
         *            Start time of the system call
         * @param name
         *            Name of the system call
         * @param tid
         *            The TID of the thread running this sytem call
         */
        public InitialInfo(
                long startTime,
                String name,
                int tid) {
            fStartTime = startTime;
            fName = name.intern();
            fTid = tid;
        }
    }

    private final long fStartTime;
    private final long fEndTime;
    private final String fName;
    private final int fTid;
    private final int fRet;

    /**
     * @param info
     *            Initial information of the system call
     * @param endTime
     *            End time of the system call
     * @param ret
     *            The return value of the system call
     */
    public SystemCall(
            InitialInfo info,
            long endTime, int ret) {
        fStartTime = info.fStartTime;
        fName = info.fName;
        fEndTime = endTime;
        fTid = info.fTid;
        fRet = ret;
    }

    private SystemCall(long startTime, long endTime, String name, int tid, int ret) {
        fStartTime = startTime;
        fEndTime = endTime;
        fName = name;
        fTid = tid;
        fRet = ret;
    }

    @Override
    public long getStart() {
        return fStartTime;
    }

    @Override
    public long getEnd() {
        return fEndTime;
    }

    /**
     * Get the name of the system call
     *
     * @return Name
     */
    @Override
    public String getName() {
        return fName;
    }

    /**
     * Get the thread ID for this syscall
     *
     * @return The ID of the thread
     */
    public int getTid() {
        return fTid;
    }

    /**
     * Get the return value of the system call
     *
     * @return The return value of this syscall
     */
    public int getReturnValue() {
        return fRet;
    }

    @Override
    public int getSizeOnDisk() {
        return 2 * Long.BYTES + SafeByteBufferFactory.getStringSizeInBuffer(fName) + 2 * Integer.BYTES;
    }

    @Override
    public void writeSegment(@NonNull ISafeByteBufferWriter buffer) {
        buffer.putLong(fStartTime);
        buffer.putLong(fEndTime);
        buffer.putString(fName);
        buffer.putInt(fTid);
        buffer.putInt(fRet);
    }

    @Override
    public int compareTo(@NonNull ISegment o) {
        int ret = INamedSegment.super.compareTo(o);
        if (ret != 0) {
            return ret;
        }
        return toString().compareTo(o.toString());
    }

    @Override
    public String toString() {
        return "Start Time = " + getStart() + //$NON-NLS-1$
                "; End Time = " + getEnd() + //$NON-NLS-1$
                "; Duration = " + getLength() + //$NON-NLS-1$
                "; Name = " + getName(); //$NON-NLS-1$
    }

    @Override
    public @Nullable ITmfCallsite getCallsite() {
        return (ITmfCallsite) SystemCallLatencyAnalysis.SyscallCallsiteAspect.INSTANCE.resolve(this);
    }

}
