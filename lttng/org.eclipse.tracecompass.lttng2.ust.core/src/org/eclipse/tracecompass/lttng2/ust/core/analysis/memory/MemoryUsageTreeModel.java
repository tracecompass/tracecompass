/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.analysis.memory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.TmfTreeDataModel;

/**
 * This class represents an entry in the tree viewer of the UST memory usage
 * View. It extends {@link TmfTreeDataModel}
 *
 * @since 3.2
 * @author Yonni Chen
 */
@NonNullByDefault
@SuppressWarnings("restriction")
public class MemoryUsageTreeModel extends TmfTreeDataModel {

    private final int fTid;

    /**
     * Constructor
     *
     * @param id
     *            The id of this model
     * @param parentId
     *            The parent id of this model
     * @param tid
     *            The TID of the process
     * @param name
     *            The thread's name
     */
    public MemoryUsageTreeModel(long id, long parentId, int tid, String name) {
        super(id, parentId, name);
        fTid = tid;
    }

    /**
     * Get the TID of the thread represented by this entry
     *
     * @return The thread's TID
     */
    public int getTid() {
        return fTid;
    }
}
