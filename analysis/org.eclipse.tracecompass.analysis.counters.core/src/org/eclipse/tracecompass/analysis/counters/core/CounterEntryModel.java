/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.core;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.TmfTreeDataModel;

/**
 * Entry Model to encapsulate the data from a {@link CounterDataProvider}.
 *
 * @author Loic Prieur-Drevon
 * @since 1.1
 */
public class CounterEntryModel extends TmfTreeDataModel {

    private final @Nullable String fFullPath;

    /**
     * Build a {@link CounterEntryModel}
     *
     * @param id
     *            The id of the model
     * @param parentId
     *            The parent id of this model. If it has none, give <code>-1</code>.
     * @param name
     *            The name of this model
     * @param fullPath
     *            this entry's full path
     */
    public CounterEntryModel(long id, long parentId, String name, @Nullable String fullPath) {
        super(id, parentId, name);
        fFullPath = fullPath;
    }

    /**
     * Get the full path :
     * <code>getTrace().getName() + '/' + ss.getFullAttributePath(quark)</code>.
     *
     * @return this entry model's full path
     */
    public @Nullable String getFullPath() {
        return fFullPath;
    }

}
