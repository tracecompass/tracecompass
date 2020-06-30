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
package org.eclipse.tracecompass.tmf.core.model;

import org.eclipse.tracecompass.tmf.core.dataprovider.DataType;

/**
 * Interface to implement to describe a table cell.
 *
 * @author Bernd Hufmann
 * @since 6.1
 *
 */
public interface ITableCellDescriptor {

    /**
     * Gets the data type of the cell.
     *
     * @return the data type of the cell
     */
    DataType getDataType();

    /**
     * Gets the unit of the cell.
     *
     * @return the unit of the cell
     */
    String getUnit();
}
