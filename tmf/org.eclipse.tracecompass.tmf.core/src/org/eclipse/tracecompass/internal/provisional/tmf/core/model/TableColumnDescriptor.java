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
import org.eclipse.tracecompass.tmf.core.model.ITableColumnDescriptor;

/**
 * Data table column descriptor implementation.
 * .
 * @since 6.1
 */
public class TableColumnDescriptor implements ITableColumnDescriptor {

    private String fText = ""; //$NON-NLS-1$
    private String fTooltipText = ""; //$NON-NLS-1$
    private ITableCellDescriptor fCellDescriptor;

    /**
     * Constructor
     *
     * @param header
     *            Column header
     */
    private TableColumnDescriptor(Builder builder) {
        fText = builder.fText;
        fTooltipText = builder.fTooltipText;
        fCellDescriptor = builder.fCellDescriptor;
    }

    @Override
    public String getText() {
        return fText;
    }

    @Override
    public String getTooltip() {
        return fTooltipText;
    }

    @Override
    public ITableCellDescriptor getCellDescriptor() {
        return fCellDescriptor;
    }

    /**
     *
     * A builder class to build instances implementing interface {@link TableColumnDescriptor}
     *
     * @author Bernd Hufmann
     */
    public static class Builder {
        private String fText = ""; //$NON-NLS-1$
        private String fTooltipText = ""; //$NON-NLS-1$
        private ITableCellDescriptor fCellDescriptor = new TableCellDescriptor.Builder().setDataType(DataType.STRING).build();

        /**
         * Constructor
         */
        public Builder() {
            // Empty constructor
        }

        /**
         * Sets the text of the header
         *
         * @param text
         *            the header text to set
         * @return this {@link Builder} object
         */
        public Builder setText(String text) {
            fText = text;
            return this;
        }

        /**
         * Sets the tooltip text of the header
         *
         * @param tooltip
         *      the tooltip text to set
         * @return this {@link Builder} object
         */
        public Builder setTooltip(String tooltip) {
            fTooltipText = tooltip;
            return this;
        }

        /**
         * Sets the default cell descriptor of the column
         *
         * @param cellDescriptor
         *      the default cell descriptor to set
         * @return this {@link Builder} object
         */
        public Builder setCellDescriptor(ITableCellDescriptor cellDescriptor) {
            fCellDescriptor = cellDescriptor;
            return this;
        }

        /**
         * The method to construct an instance of
         * {@link ITableColumnDescriptor}
         *
         * @return a {@link ITableColumnDescriptor} instance
         */
        public TableColumnDescriptor build() {
            return new TableColumnDescriptor(this);
        }
    }
}
