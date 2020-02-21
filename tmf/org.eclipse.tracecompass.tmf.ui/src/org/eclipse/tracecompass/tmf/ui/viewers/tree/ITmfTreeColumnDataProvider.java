/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mathieu Denis - Initial API and Implementation
 *   Geneviève Bastien - Moved class and adapted it to abstract tree viewer
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.tree;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Basic methods that must be implemented in a column data provider. Tree
 * viewers will use class implementing this to populate the columns.
 *
 * @author Mathieu Denis
 */
public interface ITmfTreeColumnDataProvider {

    /**
     * Return a list of the column created for the view
     *
     * @return columns list
     */
    @NonNull List<TmfTreeColumnData> getColumnData();
}