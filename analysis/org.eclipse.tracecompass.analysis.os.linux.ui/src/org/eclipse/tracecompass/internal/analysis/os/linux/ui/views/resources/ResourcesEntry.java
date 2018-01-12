/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Geneviève Bastien - Move code to provide base classes for time graph view
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.resources;

import java.util.Comparator;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.resourcesstatus.ResourcesEntryModel;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.resourcesstatus.ResourcesEntryModel.Type;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

/**
 * An entry, or row, in the resource view
 *
 * @author Patrick Tasse
 */
public class ResourcesEntry extends TimeGraphEntry implements Comparable<ITimeGraphEntry> {

    /**
     * Resources entry names should all be of type "ABC 123"
     *
     * We want to filter on the Type first (the "ABC" part), then on the ID ("123")
     * in numerical order (so we get 1,2,10 and not 1,10,2).
     */
    private static final Comparator<ResourcesEntry> COMPARATOR = Comparator.comparing(ResourcesEntry::getType)
            .thenComparingInt(ResourcesEntry::getId);

    private final int fId;
    private final @NonNull ITmfTrace fTrace;
    private final Type fType;

    /**
     * Contructor
     *
     * @param model
     *            Model from which to build the entry.
     * @param trace
     *            trace that this entry comes from.
     */
    public ResourcesEntry(ResourcesEntryModel model, @NonNull ITmfTrace trace) {
        super(model);
        fId = model.getResourceId();
        fTrace = trace;
        fType = model.getType();
    }

    /**
     * Get the entry's id
     *
     * @return the entry's id
     */
    public int getId() {
        return fId;
    }

    /**
     * Get the entry's trace
     *
     * @return the entry's trace
     */
    public @NonNull ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Get the entry Type of this entry. Uses the inner Type enum.
     *
     * @return The entry type
     */
    public Type getType() {
        return fType;
    }

    @Override
    public boolean hasTimeEvents() {
        return fType != Type.TRACE;
    }

    @Override
    public int compareTo(ITimeGraphEntry other) {
        if (!(other instanceof ResourcesEntry)) {
            /*
             * Should not happen, but if it does, put those entries at the end
             */
            return -1;
        }
        return COMPARATOR.compare(this, (ResourcesEntry) other);
    }

}
