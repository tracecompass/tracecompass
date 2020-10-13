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

package org.eclipse.tracecompass.analysis.os.linux.core.cpuusage;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.model.OsStrings;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.cpuusage.CpuUsageDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.IElementResolver;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

/**
 * Entry Model to represent entries for the {@link CpuUsageDataProvider}.
 *
 * @author Loic Prieur-Drevon
 * @since 2.4
 */
public class CpuUsageEntryModel extends TmfTreeDataModel implements IElementResolver {
    private final int fTid;
    private final long fTime;
    private final Multimap<String, Object> fMetadata;

    /**
     * Constructor
     *
     * @param id
     *            the new entry's unique ID
     * @param parentId
     *            the entry's parent ID
     * @param tid
     *            The TID of the process
     * @param processName
     *            The process's name
     * @param time
     *            The total amount of time spent on CPU
     */
    public CpuUsageEntryModel(long id, long parentId, String processName, int tid, long time) {
        this(id, parentId, ImmutableList.of(processName, String.valueOf(tid), String.valueOf(0L), String.valueOf(time)), tid, time);
    }

    /**
     * Constructor
     *
     * @param id
     *            the new entry's unique ID
     * @param parentId
     *            the entry's parent ID
     * @param tid
     *            The TID of the process
     * @param labels
     *            The process's labels, in order, name, tid, percent usage and
     *            formatted time
     * @param time
     *            The total amount of time spent on CPU
     * @since 4.0
     */
    public CpuUsageEntryModel(long id, long parentId, List<String> labels, int tid, long time) {
        super(id, parentId, labels);
        fTid = tid;
        fTime = time;
        Multimap<String, Object> map = HashMultimap.create();
        map.put(OsStrings.tid(), tid);
        fMetadata = map;
    }

    /**
     * Get the process name thread represented by this entry
     *
     * @return The thread's TID
     */
    public int getTid() {
        return fTid;
    }

    /**
     * Get the total time spent on CPU in the time interval represented by this
     * entry.
     *
     * @return The total time spent on CPU
     */
    public long getTime() {
        return fTime;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!super.equals(obj)) {
            // reference equality, nullness, getName, ID and parent ID
            return false;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        CpuUsageEntryModel other = (CpuUsageEntryModel) obj;
        return fTid == other.fTid
                && fTime == other.fTime;
    }
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fTid, fTime);
    }

    @Override
    public Multimap<String, Object> getMetadata() {
        return fMetadata;
    }
}
