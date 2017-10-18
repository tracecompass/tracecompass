/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.cpuusage;

import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.TmfTreeDataModel;

/**
 * Entry Model to represent entries for the {@link CpuUsageDataProvider}.
 *
 * @author Loic Prieur-Drevon
 * @since 2.4
 */
public class CpuUsageEntryModel extends TmfTreeDataModel {
    private final int fTid;
    private final long fTime;

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
        super(id, parentId, processName);
        fTid = tid;
        fTime = time;
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

}
