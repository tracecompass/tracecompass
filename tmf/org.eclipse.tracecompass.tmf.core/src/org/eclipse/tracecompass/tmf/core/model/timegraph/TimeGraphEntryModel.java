/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.timegraph;

import java.util.Collections;
import java.util.List;

import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

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
    private Multimap<String, Object> fMetaData = HashMultimap.create();

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
        this(id, parentId, Collections.singletonList(name), startTime, endTime, true);
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
        this(id, parentId, Collections.singletonList(name), startTime, endTime, hasRowModel);
    }

    /**
     * Constructor
     *
     * @param id
     *            Entry ID
     * @param parentId
     *            Parent ID
     * @param labels
     *            Entry labels to be displayed
     * @param startTime
     *            Start time
     * @param endTime
     *            End time
     * @since 5.0
     */
    public TimeGraphEntryModel(long id, long parentId, List<String> labels, long startTime, long endTime) {
        this(id, parentId, labels, startTime, endTime, true);
    }

    /**
     * Constructor
     *
     * @param id
     *            Entry ID
     * @param parentId
     *            Parent ID
     * @param labels
     *            Entry labels to be displayed
     * @param startTime
     *            Start time
     * @param endTime
     *            End time
     * @param hasRowModel
     *            true if the entry has a row model
     * @since 5.0
     */
    public TimeGraphEntryModel(long id, long parentId, List<String> labels, long startTime, long endTime, boolean hasRowModel) {
        super(id, parentId, labels);
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
    public Multimap<String, Object> getMetadata() {
        return fMetaData;
    }

    @Override
    public String toString() {
        return "<name=" + getLabels() + " id=" + getId() + " parentId=" + getParentId() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + " start=" + fStartTime + " end=" + fEndTime + " hasRowModel=" + hasRowModel() + ">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}
