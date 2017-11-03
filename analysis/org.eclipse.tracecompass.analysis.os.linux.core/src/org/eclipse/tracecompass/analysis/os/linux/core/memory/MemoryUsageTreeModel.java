/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.memory;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.TmfTreeDataModel;

/**
 * This class represents an entry in the tree viewer of the kernel memory usage
 * View. It extends {@link TmfTreeDataModel}
 *
 * @author Yonni Chen
 * @since 2.4
 */
@NonNullByDefault
@SuppressWarnings("restriction")
public class MemoryUsageTreeModel extends TmfTreeDataModel {

    /**
     * Suffix added to the total entry's name to map to its' series' name.
     */
    public static final String TOTAL_SUFFIX = ":total"; //$NON-NLS-1$
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

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!super.equals(obj)) {
            // reference equality, nullness, getName, ID and parent ID
            return false;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MemoryUsageTreeModel other = (MemoryUsageTreeModel) obj;
        return fTid == other.fTid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fTid);
    }
}
