/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.tree;

import java.util.Collections;
import java.util.List;

/**
 * Model's interface that can be used to represent a hierarchical relationship,
 * for instance a tree
 *
 * @author Yonni Chen
 * @since 4.0
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
     * Returns the name of this model. The name is a single label for this model
     * and it is similar to getLabels(). The goal of this method is to provide
     * an simpler way of dealing with label if this model is used in a context
     * where there is no columns support.
     *
     * @return the model name
     */
    String getName();

    /**
     * Returns a list of labels. Each label in this list can be use as a value
     * for columns. If there is no plan for column you can use getName() to
     * simplify things.
     *
     * @return the model name
     * @since 5.0
     */
    default List<String> getLabels() {
        return Collections.singletonList(getName());
    }
}
