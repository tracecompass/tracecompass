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
import org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.ThreadEntryModel;
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
        this(new ThreadEntryModel(quark, -1, execName, startTime, endTime, threadId, parentThreadId), trace);
    }

    /**
     * Constructor, build a {@link ControlFlowEntry} from it's model
     *
     * @param model
     *            the {@link ThreadEntryModel} to compose this entry
     * @param trace
     *            The trace on which we are working
     */
    public ControlFlowEntry(ThreadEntryModel model, @NonNull ITmfTrace trace) {
        super(model);
        fTrace = trace;
        fSchedulingPosition = Long.MAX_VALUE;
    }

    /**
     * Get this entry's thread ID
     *
     * @return The TID
     */
    public int getThreadId() {
        return ((ThreadEntryModel) getModel()).getThreadId();
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
        return ((ThreadEntryModel) getModel()).getParentThreadId();
    }

    @Override
    public boolean matches(@NonNull Pattern pattern) {
        if (pattern.matcher(getName()).find()) {
            return true;
        }
        if (pattern.matcher(Integer.toString(getThreadId())).find()) {
            return true;
        }
        if (pattern.matcher(Integer.toString(getParentThreadId())).find()) {
            return true;
        }
        return (pattern.matcher(FormatTimeUtils.formatTime(getStartTime(), TimeFormat.CALENDAR, Resolution.NANOSEC)).find());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + getName() + '[' + getThreadId() + "])"; //$NON-NLS-1$
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
