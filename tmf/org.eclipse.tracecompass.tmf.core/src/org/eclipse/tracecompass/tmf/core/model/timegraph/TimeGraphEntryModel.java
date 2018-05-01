/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.timegraph;

import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;

/**
 * Implementation of {@link ITimeGraphEntryModel}.
 *
 * @author Simon Delisle
 * @since 4.0
 */
public class TimeGraphEntryModel extends TmfTreeDataModel implements ITimeGraphEntryModel {
    private final long fStartTime;
    private final long fEndTime;
    private final boolean fHasRowModel;

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
        fHasRowModel = true;
    }

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
     * @param hasRowModel
     *            true if the entry has a row model
     */
    public TimeGraphEntryModel(long id, long parentId, String name, long startTime, long endTime, boolean hasRowModel) {
        super(id, parentId, name);
        fStartTime = startTime;
        fEndTime = endTime;
        fHasRowModel = hasRowModel;
    }

    @Override
    public long getStartTime() {
        return fStartTime;
    }

    @Override
    public long getEndTime() {
        return fEndTime;
    }

    @Override
    public boolean hasRowModel() {
        return fHasRowModel;
    }

@Override
    public String toString() {
        return "<name=" + getName() + " id=" + getId() + " parentId=" + getParentId() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + " start=" + fStartTime + " end=" + fEndTime + " hasRowModel=" + hasRowModel() + ">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}
