/**********************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.TableColumnDescriptor;
import org.eclipse.tracecompass.tmf.core.model.ITableColumnDescriptor;

/**
 * Represent an entire tree model
 *
 * TODO: deprecate public constructors and fHeaders
 *
 * @author Simon Delisle
 * @param <T>
 *            Tree data model extending {@link ITmfTreeDataModel}
 * @since 5.0
 */
public class TmfTreeModel<T extends ITmfTreeDataModel> {
    private List<String> fHeaders;
    private List<ITableColumnDescriptor> fColumnDescriptors;
    private List<T> fEntries;
    private @Nullable String fScope;

    /**
     * Constructor
     *
     * @param headers
     *            List of string that represent the header of a column
     * @param entries
     *            List of entries in the tree
     */
    public TmfTreeModel(List<String> headers, List<T> entries) {
        this(headers, entries, null);
    }

    /**
     * Constructor
     *
     * @param headers
     *            List of string that represent the header of a column
     * @param entries
     *            List of entries in the tree
     * @param scope
     *            The scope of all entry ids in the model
     * @since 5.2
     */
    public TmfTreeModel(List<String> headers, List<T> entries, @Nullable String scope) {
        fHeaders = headers;
        fColumnDescriptors = new ArrayList<>();
        for (String header : headers) {
            TableColumnDescriptor.Builder builder = new TableColumnDescriptor.Builder();
            builder.setText(header);
            fColumnDescriptors.add(builder.build());
        }
        fEntries = entries;
        fScope = scope;
    }

    /**
     * Headers for the model
     *
     * @return List of name of the header
     * @since 6.1
     */
    public List<ITableColumnDescriptor> getColumnDescriptors() {
        return fColumnDescriptors;
    }

    private TmfTreeModel(Builder<T> builder) {
        fHeaders = builder.fColumnDescriptors.stream()
                .map(ITableColumnDescriptor::getText)
                .collect(Collectors.toList());
        fColumnDescriptors = builder.fColumnDescriptors;
        fEntries = builder.fEntries;
        fScope = builder.fScope;
    }

    /**
     * Headers for the model
     *
     * @return List of name of the header
     */
    public List<String> getHeaders() {
        return fHeaders;
    }

    /**
     * Entries for the model
     *
     * @return List of entries
     */
    public List<T> getEntries() {
        return fEntries;
    }

    /**
     * Scope of all entry ids in the model
     *
     * @return Scope
     * @since 5.2
     */
    public @Nullable String getScope() {
        return fScope;
    }

    /**
     *
     * A builder class to build instances implementing interface
     * {@link TmfTreeModel}
     *
     * @param <T>
     *            Tree data model extending {@link ITmfTreeDataModel}
     * @since 6.1
     */
    public static class Builder<T extends ITmfTreeDataModel> {
        private List<ITableColumnDescriptor> fColumnDescriptors = new ArrayList<>();
        private List<T> fEntries;
        private @Nullable String fScope;

        /**
         * Constructor
         */
        public Builder() {
            fEntries = Collections.emptyList();
        }

        /**
         * Sets the column descriptors
         *
         * @param columnDescriptors
         *            the column descriptors to set
         * @return this {@link Builder} object
         */
        public Builder<T> setColumnDescriptors(List<ITableColumnDescriptor> columnDescriptors) {
            fColumnDescriptors = columnDescriptors;
            return this;
        }

        /**
         * Sets the entries of the model
         *
         * @param entries
         *            the entries to set
         * @return this {@link Builder} object
         */
        public Builder<T> setEntries(List<T> entries) {
            fEntries = entries;
            return this;
        }

        /**
         * Sets the scope
         *
         * @param scope
         *            the scope of all entry IDs in the model
         * @return this {@link Builder} object
         */
        public Builder<T> setScope(String scope) {
            fScope = scope;
            return this;
        }

        /**
         * The method to construct an instance of {@link TmfTreeModel}
         *
         * @return a {@link TmfTreeModel} instance
         */
        public TmfTreeModel<T> build() {
            return new TmfTreeModel<>(this);
        }
    }
}
