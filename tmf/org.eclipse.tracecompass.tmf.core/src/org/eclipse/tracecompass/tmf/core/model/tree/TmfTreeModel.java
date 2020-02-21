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

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Represent an entire tree model
 *
 * @author Simon Delisle
 * @param <T>
 *            Tree data model extending {@link ITmfTreeDataModel}
 * @since 5.0
 */
public class TmfTreeModel<T extends ITmfTreeDataModel> {
    private List<String> fHeaders;
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
        fHeaders = headers;
        fEntries = entries;
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
        fEntries = entries;
        fScope = scope;
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
}
