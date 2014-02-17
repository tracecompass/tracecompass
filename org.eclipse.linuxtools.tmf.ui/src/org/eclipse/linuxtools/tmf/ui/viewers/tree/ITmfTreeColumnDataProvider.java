/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis - Initial API and Implementation
 *   Geneviève Bastien - Moved class and adapted it to abstract tree viewer
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.tree;

import java.util.List;

/**
 * Basic methods that must be implemented in a column data provider. Tree
 * viewers will use class implementing this to populate the columns.
 *
 * @author Mathieu Denis
 * @since 3.0
 */
public interface ITmfTreeColumnDataProvider {

    /**
     * Return a list of the column created for the view
     *
     * @return columns list
     */
    List<TmfTreeColumnData> getColumnData();
}