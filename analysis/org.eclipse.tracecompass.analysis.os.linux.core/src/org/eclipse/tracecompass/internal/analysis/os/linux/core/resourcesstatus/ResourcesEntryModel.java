/*******************************************************************************
 * Copyright (c) 2018, 2019 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.resourcesstatus;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.TmfStrings;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

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
        /** Entries for CPU frequencies */
        FREQUENCY,
        /** Entries for IRQs */
        IRQ,
        /** Entries for Soft IRQ */
        SOFT_IRQ
    }

    private final int fResourceId;
    private final Type fType;

    private final @NonNull Multimap<@NonNull String, @NonNull Object> fAspects;

    /**
     * Constructor
     *
     * @param id
     *            unique Entry ID
     * @param parentId
     *            parent ID
     * @param labels
     *            entry labels
     * @param startTime
     *            start time for this entry
     * @param endTime
     *            end time for this entry
     * @param resourceId
     *            resource ID (IRQ or CPU number)
     * @param type
     *            type of entry (GROUP / CPU / CURRENT_THREAD / IRQ / SOFT_IRQ)
     */
    public ResourcesEntryModel(long id, long parentId, @NonNull List<@NonNull String> labels, long startTime, long endTime, int resourceId, Type type) {
        super(id, parentId, labels, startTime, endTime, type != Type.GROUP);
        fResourceId = resourceId;
        fType = type;
        switch (type) {
        case CPU: // resourceID is CPU, fall-through
        case CURRENT_THREAD: // resourceID is CPU, fall-through
        case FREQUENCY: // resourceID is CPU, fall-through
            fAspects = ImmutableMultimap.of(TmfStrings.cpu(), resourceId);
            break;
        case IRQ:
        case SOFT_IRQ: // Fall-through
        case GROUP: // Fall-through
        default:
            fAspects = ImmutableMultimap.of();
            break;

        }
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

    @Override
    public @NonNull Multimap<@NonNull String, @NonNull Object> getMetadata() {
        return fAspects;
    }

}
