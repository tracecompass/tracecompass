/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.table;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Basic implementation of {@link IVirtualTableLine}
 *
 * @author Simon Delisle
 */
public class VirtualTableLine implements IVirtualTableLine {

    private List<String> fLineData;
    private long findex;

    /**
     * Constructor.
     *
     * @param index
     *            Index for this line
     *
     * @param lineData
     *            Data for this line
     */
    public VirtualTableLine(long index, List<String> lineData) {
        findex = index;
        fLineData = lineData;
    }

    @Override
    public long getIndex() {
        return findex;
    }

    @Override
    public List<String> getLine() {
        return fLineData;
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
        return fLineData.equals(other.getLine()) &&
                findex == other.getIndex();
    }

    @Override
    public int hashCode() {
        return Objects.hash(fLineData, findex);
    }
}
