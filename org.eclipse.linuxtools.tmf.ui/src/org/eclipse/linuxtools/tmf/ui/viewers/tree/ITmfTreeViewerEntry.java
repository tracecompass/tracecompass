/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.tree;

import java.util.List;

/**
 * Interface for an entry (row) in a TMF tree viewer
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public interface ITmfTreeViewerEntry {

    /**
     * Returns the parent of this entry, or <code>null</code> if it has none.
     *
     * @return the parent element, or <code>null</code> if it has none
     */
    ITmfTreeViewerEntry getParent();

    /**
     * Returns whether this entry has children.
     *
     * @return <code>true</code> if the given element has children,
     *  and <code>false</code> if it has no children
     */
    boolean hasChildren();

    /**
     * Returns the child elements of this entry.
     *
     * @return an array of child elements
     */
    List<? extends ITmfTreeViewerEntry> getChildren();

    /**
     * Returns the name of this entry.
     *
     * @return the entry name
     */
    String getName();

}
