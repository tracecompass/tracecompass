/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree;

/**
 * Model's interface that can be used to represent a hierarchical relationship,
 * for instance a tree
 *
 * @author Yonni Chen
 */
public interface ITmfTreeDataModel {

    /**
     * Returns id of this model
     *
     * @return The id of the current instance
     */
    long getId();

    /**
     * Returns the parent id of this model, or <code>-1</code> if it has none.
     *
     * @return The parent id
     */
    long getParentId();

    /**
     * Returns the name of this model.
     *
     * @return the model name
     */
    String getName();
}
