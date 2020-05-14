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

/**
 * Interface to implement to describe a table column.
 *
 * @author Bernd Hufmann
 * @since 6.1
 *
 */
public interface ITableColumnDescriptor {

    /**
     * Gets the header text of the column.
     *
     * @return the text of the header
     */
    String getText();

    /**
     * Gets the header tooltip text of the column.
     *
     * @return the tooltip text of the column
     */
    String getTooltip();

}
