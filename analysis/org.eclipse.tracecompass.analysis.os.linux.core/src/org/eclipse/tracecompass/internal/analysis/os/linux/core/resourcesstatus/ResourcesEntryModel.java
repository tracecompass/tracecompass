/*******************************************************************************
 * Copyright (c) 2018 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.resourcesstatus;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.TimeGraphEntryModel;

/**
 * {@link TimeGraphEntryModel} for the Resources Status data provider.
 *
 * @author Loic Prieur-Drevon
 */
public class ResourcesEntryModel extends TimeGraphEntryModel {

    /** Type of entry */
    public enum Type {
        /** Group entries (trace, separators) */
        GROUP,
        /** Entries for CPUs */
        CPU,
        /** Entries for Current Thread */
        CURRENT_THREAD,
        /** Entries for IRQs */
        IRQ,
        /** Entries for Soft IRQ */
        SOFT_IRQ
    }

    private final int fResourceId;
    private final Type fType;

    /**
     * Constructor
     *
     * @param id
     *            unique Entry ID
     * @param parentId
     *            parent ID
     * @param name
     *            entry name
     * @param startTime
     *            start time for this entry
     * @param endTime
     *            end time for this entry
     * @param resourceId
     *            resource ID (IRQ or CPU number)
     * @param type
     *            type of entry (GROUP / CPU / CURRENT_THREAD / IRQ / SOFT_IRQ)
     */
    public ResourcesEntryModel(long id, long parentId, @NonNull String name, long startTime, long endTime, int resourceId, Type type) {
        super(id, parentId, name, startTime, endTime, !name.isEmpty());
        fResourceId = resourceId;
        fType = type;
    }

    /**
     * Get this entry's resource ID (IRQ or CPU number)
     *
     * @return the resource ID.
     */
    public int getResourceId() {
        return fResourceId;
    }

    /**
     * Get this entry's type (TRACE / CPU / IRQ / SOFT_IRQ)
     *
     * @return the type of entry
     */
    public Type getType() {
        return fType;
    }
}
