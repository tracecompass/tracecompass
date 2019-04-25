/**********************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.tree;

import java.util.List;

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
}
