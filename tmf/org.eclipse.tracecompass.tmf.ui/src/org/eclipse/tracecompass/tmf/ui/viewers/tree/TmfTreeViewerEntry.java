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

package org.eclipse.tracecompass.tmf.ui.viewers.tree;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Basic implementation of an entry for the TMF tree viewer. A name is all that is needed for this entry.
 *
 * @author Geneviève Bastien
 */
public class TmfTreeViewerEntry implements ITmfTreeViewerEntry {

    /** Entry's parent */
    private ITmfTreeViewerEntry fParent = null;

    /** List of child entries */
    private final @NonNull List<@NonNull ITmfTreeViewerEntry> fChildren = new CopyOnWriteArrayList<>();

    /** Name of this entry (default text to show in first column) */
    private String fName;

    /**
     * Constructor
     *
     * @param name
     *            The name of this entry
     */
    public TmfTreeViewerEntry(String name) {
        fName = name;
    }

    // ---------------------------------------------
    // Getters and setters
    // ---------------------------------------------

    @Override
    public ITmfTreeViewerEntry getParent() {
        return fParent;
    }

    /**
     * Sets the entry's parent
     *
     * @param entry The new parent entry
     */
    protected void setParent(ITmfTreeViewerEntry entry) {
        fParent = entry;
    }

    @Override
    public boolean hasChildren() {
        return fChildren.size() > 0;
    }

    @Override
    public List<@NonNull ITmfTreeViewerEntry> getChildren() {
        return fChildren;
    }

    @Override
    public String getName() {
        return fName;
    }

    /**
     * Update the entry name
     *
     * @param name
     *            the updated entry name
     */
    public void setName(String name) {
        fName = name;
    }

    /**
     * Add a child entry to this one
     *
     * @param child
     *            The child entry
     */
    public void addChild(TmfTreeViewerEntry child) {
        child.fParent = this;
        fChildren.add(child);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + fName + ']';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TmfTreeViewerEntry other = (TmfTreeViewerEntry) obj;
        return Objects.equals(getName(), other.getName())
                && getChildren().equals(other.getChildren());
    }

    @Override
    public int hashCode() {
        return Objects.hash(fName, fChildren);
    }

}
