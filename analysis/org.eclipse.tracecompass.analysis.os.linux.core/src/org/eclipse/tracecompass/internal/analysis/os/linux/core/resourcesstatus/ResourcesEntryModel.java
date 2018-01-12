/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.resourcesstatus;

import org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph.TimeGraphEntryModel;

/**
 * {@link TimeGraphEntryModel} for the Resources Status data provider.
 *
 * @author Loic Prieur-Drevon
 */
public class ResourcesEntryModel extends TimeGraphEntryModel {

    /** Type of resource */
    public enum Type {
        /** Null resources (filler rows, etc.) */
        TRACE,
        /** Entries for CPUs */
        CPU,
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
     *            type of resource (TRACE / CPU / IRQ / SOFT_IRQ)
     */
    public ResourcesEntryModel(long id, long parentId, String name, long startTime, long endTime, int resourceId, Type type) {
        super(id, parentId, computeEntryName(name, type, resourceId), startTime, endTime);
        fResourceId = resourceId;
        fType = type;
    }

    private static String computeEntryName(String name, Type type, int id) {
        if (type == Type.TRACE) {
            // use the trace name.
            return name;
        }
        if (type == Type.SOFT_IRQ) {
            return type.toString() + ' ' + id + ' ' + SoftIrqLabelProvider.getSoftIrq(id);
        }
        return type.toString() + ' ' + id;
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
