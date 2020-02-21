/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.tree;

import java.util.Collections;
import java.util.List;
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
    private final List<String> fLabels;

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
        this(id, parentId, Collections.singletonList(name));
    }

    /**
     * Constructor
     *
     * @param id
     *            The id of the model
     * @param parentId
     *            The parent id of this model. If it has none, give <code>-1</code>.
     * @param labels
     *            The labels of this model
     * @since 5.0
     */
    public TmfTreeDataModel(long id, long parentId, List<String> labels) {
        fId = id;
        fParentId = parentId;
        fLabels = labels;
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
        return fLabels.isEmpty() ? "" : fLabels.get(0); //$NON-NLS-1$
    }

    @Override
    public List<String> getLabels() {
        return fLabels;
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
                && fLabels.equals(other.fLabels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fId, fParentId, fLabels);
    }

    @Override
    public String toString() {
        return "<name=" + fLabels + " id=" + fId + " parentId=" + fParentId + ">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}
