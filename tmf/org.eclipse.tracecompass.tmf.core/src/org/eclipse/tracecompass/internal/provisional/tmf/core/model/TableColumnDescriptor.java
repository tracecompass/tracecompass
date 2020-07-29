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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.model.ITableColumnDescriptor;

import com.google.common.base.Objects;

/**
 * Data table column descriptor implementation.
 * .
 * @since 6.1
 */
public class TableColumnDescriptor implements ITableColumnDescriptor {

    private String fText = ""; //$NON-NLS-1$
    private String fTooltipText = ""; //$NON-NLS-1$

    /**
     * Constructor
     *
     * @param header
     *            Column header
     */
    private TableColumnDescriptor(Builder builder) {
        fText = builder.fText;
        fTooltipText = builder.fTooltipText;
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
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ITableColumnDescriptor)) {
            return false;
        }
        ITableColumnDescriptor other = (ITableColumnDescriptor) obj;
        return Objects.equal(fText, other.getText()) &&
                Objects.equal(fTooltipText, other.getTooltip());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fText, fTooltipText);
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[text=")
               .append(fText)
               .append(" tooltip=")
               .append(fTooltipText)
               .append("]");
        return builder.toString();
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
