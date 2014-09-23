/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.cpuusage;

import org.eclipse.linuxtools.tmf.ui.viewers.tree.TmfTreeViewerEntry;

/**
 * Represents an entry in the tree viewer of the CPU usage view. An entry is a
 * thread that occupied part of the CPU in the selected time range.
 *
 * @author Geneviève Bastien
 */
public class CpuUsageEntry extends TmfTreeViewerEntry {
    private final String fTid;
    private final String fProcessName;
    private final Double fPercent;
    private final Long fTime;

    /**
     * Constructor
     *
     * @param tid
     *            The TID of the process
     * @param name
     *            The thread's name
     * @param percent
     *            The percentage CPU usage
     * @param time
     *            The total amount of time spent on CPU
     */
    public CpuUsageEntry(String tid, String name, double percent, long time) {
        super(tid);
        fTid = tid;
        fProcessName = name;
        fPercent = percent;
        fTime = time;
    }

    /**
     * Get the TID of the thread represented by this entry
     *
     * @return The thread's TID
     */
    public String getTid() {
        return fTid;
    }

    /**
     * Get the process name
     *
     * @return The process name
     */
    public String getProcessName() {
        return fProcessName;
    }

    /**
     * Get the percentage of time spent on CPU in the time interval represented
     * by this entry.
     *
     * @return The percentage of time spent on CPU
     */
    public Double getPercent() {
        return fPercent;
    }

    /**
     * Get the total time spent on CPU in the time interval represented by this
     * entry.
     *
     * @return The total time spent on CPU
     */
    public Long getTime() {
        return fTime;
    }
}
