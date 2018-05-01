/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.tree;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Basic implementation of {@link ITmfTreeDataModel}.
 *
 * @author Yonni Chen
 * @since 4.0
 */
public class TmfTreeDataModel implements ITmfTreeDataModel {

    private final long fId;
    private final long fParentId;
    private final String fName;

    /**
     * Constructor
     *
     * @param id
     *            The id of the model
     * @param parentId
     *            The parent id of this model. If it has none, give <code>-1</code>.
     * @param name
     *            The name of this model
     */
    public TmfTreeDataModel(long id, long parentId, String name) {
        fId = id;
        fParentId = parentId;
        fName = name;
    }

    @Override
    public long getId() {
        return fId;
    }

    @Override
    public long getParentId() {
        return fParentId;
    }

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TmfTreeDataModel other = (TmfTreeDataModel) obj;
        return fId == other.fId
                && fParentId == other.fParentId
                && fName.equals(other.fName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fId, fParentId, fName);
    }

    @Override
    public String toString() {
        return "<name=" + fName + " id=" + fId + " parentId=" + fParentId + ">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}
