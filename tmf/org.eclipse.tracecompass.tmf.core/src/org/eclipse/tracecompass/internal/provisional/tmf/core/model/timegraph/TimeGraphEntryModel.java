/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph;

import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.TmfTreeDataModel;

/**
 * Implementation of {@link ITimeGraphEntryModel}.
 *
 * @since 3.2
 * @author Simon Delisle
 */
public class TimeGraphEntryModel extends TmfTreeDataModel implements ITimeGraphEntryModel {
    private long fStartTime;
    private long fEndTime;

    /**
     * Constructor
     *
     * @param id
     *            Entry ID
     * @param parentId
     *            Parent ID
     * @param name
     *            Entry name to be displayed
     * @param startTime
     *            Start time
     * @param endTime
     *            End time
     */
    public TimeGraphEntryModel(long id, long parentId, String name, long startTime, long endTime) {
        super(id, parentId, name);
        fStartTime = startTime;
        fEndTime = endTime;
    }

    @Override
    public long getStartTime() {
        return fStartTime;
    }

    @Override
    public long getEndTime() {
        return fEndTime;
    }

    /**
     * Update entry start time
     *
     * @param start
     *            new start time
     */
    public void updateStartTime(long start) {
        if (fStartTime == -1) {
            fStartTime = start;
        } else {
            fStartTime = Long.min(start, fStartTime);
        }
    }

    /**
     * Update entry end time
     *
     * @param endTime
     *            New end time
     */
    public void updateEndTime(long endTime) {
        if (fEndTime == -1) {
            fEndTime = endTime;
        } else {
            fEndTime = Long.max(endTime, fEndTime);
        }
    }
}
