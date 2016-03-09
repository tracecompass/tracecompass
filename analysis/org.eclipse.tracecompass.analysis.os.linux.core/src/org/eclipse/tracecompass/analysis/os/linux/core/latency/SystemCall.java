/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.latency;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.segmentstore.core.ISegment;

import com.google.common.collect.ImmutableMap;

/**
 * A linux kernel system call, represented as an {@link ISegment}.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class SystemCall implements ISegment {

    private static final long serialVersionUID = 1554494342105208730L;

    /**
     * The subset of information that is available from the syscall entry event.
     */
    public static class InitialInfo implements Serializable {

        private static final long serialVersionUID = -5009710718804983721L;

        private final long fStartTime;
        private final String fName;
        private final Map<String, String> fArgs;

        /**
         * @param startTime
         *            Start time of the system call
         * @param name
         *            Name of the system call
         * @param arguments
         *            Arguments of the system call
         */
        public InitialInfo(
                long startTime,
                String name,
                Map<String, String> arguments) {
            fStartTime = startTime;
            fName = name;
            fArgs = ImmutableMap.copyOf(arguments);
        }
    }

    private final InitialInfo fInfo;
    private final long fEndTime;
    private final int fRet;

    /**
     * @param info
     *            Initial information of the system call
     * @param endTime
     *            End time of the system call
     * @param ret
     *            Return value of the system call
     */
    public SystemCall(
            InitialInfo info,
            long endTime,
            int ret) {
        fInfo = info;
        fEndTime = endTime;
        fRet = ret;
    }

    @Override
    public long getStart() {
        return fInfo.fStartTime;
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
    public String getName() {
        return fInfo.fName;
    }

    /**
     * Get the arguments of the system call
     *
     * @return Map of the arguments
     */
    public Map<String, String> getArguments() {
        return fInfo.fArgs;
    }

    /**
     * Get the return value of the system call
     *
     * @return Return value
     */
    public int getReturnValue() {
        return fRet;
    }

    @Override
    public int compareTo(@NonNull ISegment o) {
        int ret = ISegment.super.compareTo(o);
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
                "; Name = " + getName() + //$NON-NLS-1$
                "; Args = " + getArguments().toString() + //$NON-NLS-1$
                "; Return = " + getReturnValue(); //$NON-NLS-1$
    }
}
