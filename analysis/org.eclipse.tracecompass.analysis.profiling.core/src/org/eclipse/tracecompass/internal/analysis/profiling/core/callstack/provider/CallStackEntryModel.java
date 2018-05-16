/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.profiling.core.callstack.provider;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;

/**
 * {@link TimeGraphEntryModel} for the Call Stack
 *
 * @author Loic Prieur-Drevon
 */
public class CallStackEntryModel extends TimeGraphEntryModel {

    /**
     * Stack level associated to a Trace
     */
    public static final int TRACE = -2;
    /**
     * Stack level associated to a Process
     */
    public static final int PROCESS = -1;
    /**
     * Stack level associated to a Thread
     */
    public static final int THREAD = 0;

    private final int fStackLevel;
    private final int fPid;

    /**
     * Constructor
     *
     * @param id
     *            unique ID for this {@link CallStackEntryModel}
     * @param parentId
     *            parent's ID to build the tree
     * @param name
     *            entry's name
     * @param startTime
     *            entry's start time
     * @param endTime
     *            entry's end time
     * @param stackLevel
     *            function's stack level or {@link #TRACE} if the entry is a trace,
     *            {@link #PROCESS} if the entry is a Process, or {@link #THREAD} if
     *            the entry is a Thread
     * @param pid
     *            entry's PID or TID if is a thread
     */
    public CallStackEntryModel(long id, long parentId, String name, long startTime, long endTime, int stackLevel, int pid) {
        super(id, parentId, name, startTime, endTime);
        fStackLevel = stackLevel;
        fPid = pid;
    }

    /**
     * Getter for the stack level
     *
     * @return the stack level if the the entry is a function, else {@value #TRACE}
     *         if the entry is a trace, {@value #PROCESS} if the entry is a Process,
     *         or {@value #THREAD} if the entry is a Thread
     */
    public int getStackLevel() {
        return fStackLevel;
    }

    /**
     * Get the PID or TID if this is a thread.
     *
     * @return the PID or TID
     */
    public int getPid() {
        return fPid;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!super.equals(obj)) {
            // nullness, class, name, ids
            return false;
        }
        if (!(obj instanceof CallStackEntryModel)) {
            return false;
        }
        CallStackEntryModel other = (CallStackEntryModel) obj;
        return fStackLevel == other.fStackLevel && fPid == other.fPid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fStackLevel, fPid);
    }

    @Override
    public String toString() {
        return super.toString() + ' ' + getType();
    }

    private String getType() {
        switch (fStackLevel) {
        case TRACE:
            return "TRACE"; //$NON-NLS-1$
        case PROCESS:
            return "PROCESS"; //$NON-NLS-1$
        case THREAD:
            return "THREAD"; //$NON-NLS-1$
        default:
            return "FUNCTION"; //$NON-NLS-1$
        }
    }
}
