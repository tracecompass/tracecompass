/**********************************************************************
 * Copyright (c) 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.tmf.tests.stubs.model.tree;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;

/**
 * Simple Data model stub
 */
public class TmfTreeDataModelStub implements ITmfTreeDataModel {

    private final long fId;
    private final long fParentId;
    private final List<String> fLabels;

    /**
     * Constructor
     *
     * @param id
     *            the id
     * @param parentId
     *            the parent's id
     * @param labels
     *            the labels
     */
    public TmfTreeDataModelStub(long id, long parentId, List<String> labels) {
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
        return getLabels().get(0);
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

        if (!(this.getClass().equals(obj.getClass()))) {
            return false;
        }
        final TmfTreeDataModelStub other = (TmfTreeDataModelStub) obj;

        return (fId == other.fId) && (fParentId == other.fParentId) && Objects.equals(fLabels, other.fLabels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fId, fParentId, fLabels);
    }
}
