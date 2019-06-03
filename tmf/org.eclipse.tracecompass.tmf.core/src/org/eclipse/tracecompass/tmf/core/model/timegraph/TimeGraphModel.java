/**********************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.timegraph;

import java.util.List;

/**
 * Represent an entire Time Graph model
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
}
