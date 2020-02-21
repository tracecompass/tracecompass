/**********************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.table;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Represent a cell in a virtual table
 *
 * @author Simon Delisle
 */
public class VirtualTableCell {
    private String fContent;
    private int fTags;

    /**
     * Constructor
     *
     * @param content
     *            Content of the cell
     */
    public VirtualTableCell(String content) {
        this.fContent = content;
        this.fTags = 0;
    }

    /**
     * Constructor
     *
     * @param content
     *            Content of the cell
     * @param tags
     *            Used if the cell pass a filter
     */
    public VirtualTableCell(String content, int tags) {
        this.fContent = content;
        this.fTags = tags;
    }

    /**
     * Retrieve the content of the cell
     *
     * @return Content
     */
    public String getContent() {
        return fContent;
    }

    /**
     * Get tags
     *
     * @return Tags encode as int
     */
    public int getTags() {
        return fTags;
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
        VirtualTableCell other = (VirtualTableCell) obj;
        return fContent.equals(other.getContent()) &&
                fTags == other.getTags();
    }

    @Override
    public int hashCode() {
        return Objects.hash(fContent, fTags);
    }
}
