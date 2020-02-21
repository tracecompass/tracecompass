/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.tree;

import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;

/**
 * Generic implementation for a {@link TmfTreeViewerEntry} that also
 * encapsulates a model from an {@link ITmfTreeDataProvider}.
 *
 * @param <M>
 *            type of model payload
 * @author Loic Prieur-Drevon
 * @since 3.2
 */
public class TmfGenericTreeEntry<M extends ITmfTreeDataModel> extends TmfTreeViewerEntry {

    private final M fModel;

    /**
     * Constructor
     *
     * @param model
     *            the model to encapsulate
     * @since 4.0
     */
    public TmfGenericTreeEntry(M model) {
        super(model.getName());
        fModel = model;
    }

    /**
     * Get the encapsulated model
     *
     * @return the encapsulated model.
     * @since 4.0
     */
    public M getModel() {
        return fModel;
    }
}
