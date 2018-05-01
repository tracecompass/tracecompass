/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.filters;

/**
 * This is a functional interface for query filter used by some data providers.
 * It is used for queries that return a subset based on multiple selected items.
 *
 * @author Yonni Chen
 * @since 4.0
 * @param <T>
 *            Generic type for the collection of selected objects
 */
public interface IMultipleSelectionQueryFilter<T> {

    /**
     * Gets the selected items
     *
     * @return The selected items
     */
    T getSelectedItems();
}
