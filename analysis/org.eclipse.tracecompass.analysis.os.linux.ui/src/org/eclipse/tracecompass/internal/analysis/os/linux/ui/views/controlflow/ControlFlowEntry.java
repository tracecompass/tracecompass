/*******************************************************************************
 * Copyright (c) 2012, 2016 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Geneviève Bastien - Move code to provide base classes for time graph view
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow;

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.Resolution;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.TimeFormat;

/**
 * An entry in the Control Flow view
 */
public class ControlFlowEntry extends TimeGraphEntry {

    private final @NonNull ITmfTrace fTrace;
    private final int fThreadId;
    private int fParentThreadId;
    private final int fThreadQuark;

    /**
     * This column is for keeping the order we found with the scheduling algorithm.
     * It will be used for sorting.
     */
    private long fSchedulingPosition;

    /**
     * Constructor
     *
     * @param quark
     *            The attribute quark matching the thread
     * @param trace
     *            The trace on which we are working
     * @param execName
     *            The exec_name of this entry
     * @param threadId
     *            The TID of the thread
     * @param parentThreadId
     *            the Parent_TID of this thread
     * @param startTime
     *            The start time of this process's lifetime
     * @param endTime
     *            The end time of this process
     */
    public ControlFlowEntry(int quark, @NonNull ITmfTrace trace, String execName, int threadId, int parentThreadId, long startTime, long endTime) {
        super(execName, startTime, endTime);
        fTrace = trace;
        fThreadId = threadId;
        fParentThreadId = parentThreadId;
        fThreadQuark = quark;
        fSchedulingPosition = Long.MAX_VALUE;
    }

    /**
     * Get this entry's thread ID
     *
     * @return The TID
     */
    public int getThreadId() {
        return fThreadId;
    }

    /**
     * Get the entry's trace
     *
     * @return the entry's trace
     */
    public @NonNull ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Get this thread's parent TID
     *
     * @return The "PTID"
     */
    public int getParentThreadId() {
        return fParentThreadId;
    }

    /**
     * Set this thread's parent TID
     *
     * @param ptid
     *            The "PTID"
     * @since 1.1
     */
    public void setParentThreadId(int ptid) {
        fParentThreadId = ptid;
    }

    /**
     * Get the quark of the attribute matching this thread's TID
     *
     * @return The quark
     */
    public int getThreadQuark() {
        return fThreadQuark;
    }

    @Override
    public boolean matches(@NonNull Pattern pattern) {
        if (pattern.matcher(getName()).find()) {
            return true;
        }
        if (pattern.matcher(Integer.toString(fThreadId)).find()) {
            return true;
        }
        if (pattern.matcher(Integer.toString(fParentThreadId)).find()) {
            return true;
        }
        if (pattern.matcher(Integer.toString(fThreadQuark)).find()) {
            return true;
        }
        return (pattern.matcher(FormatTimeUtils.formatTime(getStartTime(), TimeFormat.CALENDAR, Resolution.NANOSEC)).find());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + getName() + '[' + fThreadId + "])"; //$NON-NLS-1$
    }

    /**
     * Get the position this entry should be according to the scheduling
     * algorithm shown in ControlFlowView#OptimizationAction. The position helps
     * layout entries with more links closer together
     *
     * @return The position
     */
    public long getSchedulingPosition() {
        return fSchedulingPosition;
    }

    /**
     * Set the position this entry should be according to the scheduling
     * algorithm shown in ControlFlowView#OptimizationAction algorithm shown in
     * ControlFlowView#OptimizationAction
     *
     * @param schedulingPosition
     *            The position
     */
    public void setSchedulingPosition(long schedulingPosition) {
        fSchedulingPosition = schedulingPosition;
    }
}
