/**********************************************************************
 * Copyright (c) 2019, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.timegraph;

import java.util.List;

/**
 * Represent a Time Graph model that includes all the rows.
 *
 * @author Simon Delisle
 * @since 5.0
 */
public class TimeGraphModel {
    private List<ITimeGraphRowModel> fRows;

    /**
     * Constructor
     *
     * @param rows
     *            List of {@link ITimeGraphRowModel}
     */
    public TimeGraphModel(List<ITimeGraphRowModel> rows) {
        fRows = rows;
    }

    /**
     * Get rows associated to the model
     *
     * @return List of {@link ITimeGraphRowModel}
     */
    public List<ITimeGraphRowModel> getRows() {
        return fRows;
    }

    @Override
    public String toString() {
        return String.format("Model: rows size: %d", fRows.size()); //$NON-NLS-1$
    }
}
