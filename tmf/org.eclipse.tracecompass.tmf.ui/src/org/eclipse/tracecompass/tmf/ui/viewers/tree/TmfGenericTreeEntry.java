/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.tree;

import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.TmfTreeDataModel;

/**
 * Generic implementation for a {@link TmfTreeViewerEntry} that also
 * encapsulates a model from an {@link ITmfTreeDataProvider}.
 *
 * @param <M>
 *            type of model payload
 * @author Loic Prieur-Drevon
 */
public class TmfGenericTreeEntry<M extends TmfTreeDataModel> extends TmfTreeViewerEntry {

    private final M fModel;

    /**
     * Constructor
     *
     * @param model
     *            the model to encapsulate
     */
    public TmfGenericTreeEntry(M model) {
        super(model.getName());
        fModel = model;
    }

    /**
     * Get the encapsulated model
     *
     * @return the encapsulated model.
     */
    public M getModel() {
        return fModel;
    }

}
