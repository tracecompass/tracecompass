/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.kernelmemoryusage;

import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeViewerEntry;

/**
 * This class represents an entry in the tree viewer of the kernel memory usage
 * View.
 *
 * @author mahdi zolnouri
 */
public class KernelMemoryUsageEntry extends TmfTreeViewerEntry {

    private final String fTid;
    private final String fProcessName;

    /**
     * Constructor
     *
     * @param tid
     *            The TID of the process
     * @param name
     *            The thread's name
     */
    public KernelMemoryUsageEntry(String tid, String name) {
        super(tid);
        fTid = tid;
        fProcessName = name;
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
}
