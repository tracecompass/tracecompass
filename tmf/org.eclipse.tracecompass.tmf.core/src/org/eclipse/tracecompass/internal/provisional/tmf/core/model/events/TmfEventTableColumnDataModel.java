/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.events;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;

/**
 * Model that contain all the information on a column for a virtual table
 *
 * @author Simon Delisle
 * @since 4.0
 */
public class TmfEventTableColumnDataModel extends TmfTreeDataModel {

    private String fHeaderTooltip;

    /**
     * Specified if this column should be hidden by default. TODO: Currently aspects
     * can specified if they are hidden by default, this is why we need this
     * information to be pass from backend to frontend. It should be decoupled from
     * the backend.
     */
    private boolean fHiddenByDefault;

    /**
     * Constructor.
     *
     * @param id
     *            Column ID
     * @param parentId
     *            Parent ID, -1 if no parent
     * @param name
     *            Column name
     * @param headerTooltip
     *            Header tooltip
     * @param isHiddenByDefault
     *            If the column should be hidden by default
     */
    public TmfEventTableColumnDataModel(long id, long parentId, String name, String headerTooltip, boolean isHiddenByDefault) {
        super(id, parentId, name);
        fHeaderTooltip = headerTooltip;
        fHiddenByDefault = isHiddenByDefault;
    }

    /**
     * Get the tooltip attached to this column
     *
     * @return The header tooltip
     */
    public String getHeaderTooltip() {
        return fHeaderTooltip;
    }

    /**
     * If the column is hidden by default
     *
     * @return True if the column should be hidden by default
     */
    public boolean isHiddenByDefault() {
        return fHiddenByDefault;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!super.equals(obj)) {
            return false;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        TmfEventTableColumnDataModel other = (TmfEventTableColumnDataModel) obj;
        return fHeaderTooltip.equals(other.getHeaderTooltip()) &&
                Boolean.compare(fHiddenByDefault, other.isHiddenByDefault()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fHeaderTooltip, fHiddenByDefault, getId(), getName());
    }
}