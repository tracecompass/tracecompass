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

package org.eclipse.tracecompass.internal.provisional.tmf.core.model;

import org.eclipse.tracecompass.tmf.core.dataprovider.DataType;
import org.eclipse.tracecompass.tmf.core.model.ITableCellDescriptor;

/**
 * Data table cell descriptor implementation. .
 *
 * @author Bernd Hufmann
 * @since 6.1
 */
public class TableCellDescriptor implements ITableCellDescriptor {

    private DataType fDataType = DataType.STRING;
    private String fUnit = ""; //$NON-NLS-1$

    /**
     * Constructor
     *
     * @param builder
     *            Builder class
     */
    private TableCellDescriptor(Builder builder) {
        fDataType = builder.fDataType;
        fUnit = builder.fUnit;
    }

    @Override
    public DataType getDataType() {
        return fDataType;
    }

    @Override
    public String getUnit() {
        return fUnit;
    }

    /**
     * A builder class to build instances implementing interface
     * {@link ITableCellDescriptor}
     */
    public static class Builder {
        private DataType fDataType = DataType.STRING;
        private String fUnit = ""; //$NON-NLS-1$

        /**
         * Constructor
         */
        public Builder() {
            // Empty constructor
        }

        /**
         * Sets the data type of the cell.
         *
         * @param type
         *            the data type
         * @return this {@link Builder} object
         */
        public Builder setDataType(DataType type) {
            fDataType = type;
            return this;
        }

        /**
         * Sets the unit of the cell.
         *
         * @param unit
         *            the unit to set
         * @return this {@link Builder} object
         */
        public Builder setUnit(String unit) {
            fUnit = unit;
            return this;
        }

        /**
         * The method to construct an instance of {@link ITableCellDescriptor}
         *
         * @return a {@link ITableCellDescriptor} instance
         */
        public TableCellDescriptor build() {
            return new TableCellDescriptor(this);
        }
    }
}
