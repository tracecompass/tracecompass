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
    private final String fProcessName;
    private final Long fTime;

    /**
     * Constructor
     *
     * @param tid
     *            The TID of the process
     * @param name
     *            The thread's name
     * @param time
     *            The total amount of time spent on CPU
     */
    public CpuUsageEntryModel(long id, long parentId, String tid, String name, long time) {
        super(id, parentId, tid);
        fProcessName = name;
        fTime = time;
    }

    /**
     * Get the process name thread represented by this entry
     *
     * @return The thread's TID
     */
    public String getProcessName() {
        return fProcessName;
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
