/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.table;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Basic implementation of {@link IVirtualTableLine}
 *
 * @author Simon Delisle
 * @since 4.0
 */
public class VirtualTableLine implements IVirtualTableLine {

    private List<VirtualTableCell> fCells;
    private long findex;

    /**
     * Constructor.
     *
     * @param index
     *            Index for this line
     *
     * @param cellData
     *            Data for this line
     */
    public VirtualTableLine(long index, List<VirtualTableCell> cellData) {
        findex = index;
        fCells = cellData;
    }

    @Override
    public long getIndex() {
        return findex;
    }

    @Override
    public List<VirtualTableCell> getCells() {
        return fCells;
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
        VirtualTableLine other = (VirtualTableLine) obj;
        return fCells.equals(other.getCells()) &&
                findex == other.getIndex();
    }

    @Override
    public int hashCode() {
        return Objects.hash(fCells, findex);
    }
}
