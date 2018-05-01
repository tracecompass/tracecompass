/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.table;

import java.util.List;

/**
 * Virtual table model's interface
 *
 * @author Yonni Chen
 * @param <L> A {@link IVirtualTableLine} implementation
 * @since 4.0
 */
public interface ITmfVirtualTableModel<L extends IVirtualTableLine> {

    /**
     * Gets columns IDs
     *
     * @return The list of column IDs in order that they are sorted
     */
    List<Long> getColumnIds();

    /**
     * Gets the data associated with the model. The list represent the lines for
     * this table model. The data in a {@link IVirtualTableLine} are in the same
     * order as the column IDs order
     *
     * @return The list of lines
     */
    List<L> getData();

    /**
     * Gets the index of the first table entry in the model
     *
     * @return The top index
     */
    long getIndex();

    /**
     * Gets the number of table entries that matches a filter. If there was no
     * filter applied, it will return simply the the total number of table entries.
     *
     * @return The total number of table entries that matches a filter
     */
    long getNbTotalEntries();
}