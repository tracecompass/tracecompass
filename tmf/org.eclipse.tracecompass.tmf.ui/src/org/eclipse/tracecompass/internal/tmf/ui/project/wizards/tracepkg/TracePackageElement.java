/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *     Patrick Tasse - Add list methods
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * An ExportTraceElement represents an item in the ExportTraceWizard tree.
 *
 * @author Marc-Andre Laperle
 */
public abstract class TracePackageElement extends WorkbenchAdapter {
    private final List<TracePackageElement> fChildren;
    private TracePackageElement fParent;
    private boolean fEnabled;
    private boolean fChecked;
    private boolean fVisible;

    /**
     *
     * @param parent
     *            the parent of this element, can be set to null
     */
    public TracePackageElement(TracePackageElement parent) {
        fParent = parent;
        fEnabled = true;
        fVisible = true;
        fChildren = new ArrayList<>();
        if (parent != null) {
            parent.addChild(this);
        }
    }

    /**
     * Add a child at the end of the element's children list.
     *
     * @param child
     *            the element to add as a child
     */
    public void addChild(TracePackageElement child) {
        child.setParent(this);
        fChildren.add(child);
    }

    /**
     * Insert a child at the specified position in the element's children list.
     *
     * @param index
     *            the index at which the element is to be inserted
     * @param child
     *            the element to insert as a child
     */
    public void addChild(int index, TracePackageElement child) {
        fChildren.add(index, child);
        child.setParent(this);
    }

    /**
     * Remove a child from the element's children list.
     *
     * @param child
     *            the child to remove
     */
    public void removeChild(TracePackageElement child) {
        fChildren.remove(child);
        child.setParent(null);
    }

    /**
     * Returns the index of the specified child in the element's children list.
     *
     * @param child
     *            the child to search for
     * @return the index of the child in the list, or -1 if not found
     */
    public int indexOf(TracePackageElement child) {
        return fChildren.indexOf(child);
    }

    /**
     * @return the parent of this element or null if there is no parent
     */
    public TracePackageElement getParent() {
        return fParent;
    }

    private void setParent(TracePackageElement parent) {
        fParent = parent;
    }

    /**
     * Get the text representation of this element to be displayed in the tree.
     *
     * @return the text representation
     */
    public abstract String getText();

    /**
     * Get the children of this element
     *
     * @return the children of this element
     */
    public TracePackageElement[] getChildren() {
        return fChildren.toArray(new TracePackageElement[fChildren.size()]);
    }

    /**
     * Get the visible children of this element
     *
     * @return the visible children of this element
     */
    public TracePackageElement[] getVisibleChildren() {
        List<TracePackageElement> visibleChildren = new ArrayList<>();
        for (TracePackageElement child : fChildren) {
            if (child.isVisible()) {
                visibleChildren.add(child);
            }
        }
        return visibleChildren.toArray(new TracePackageElement[0]);
    }

    /**
     * Get the total size of the element including its children
     *
     * @param checkedOnly
     *            only count checked elements
     *
     * @return the total size of the element
     */
    public long getSize(boolean checkedOnly) {
        long size = 0;
        if (fChildren != null) {
            for (TracePackageElement child : fChildren) {
                size += child.getSize(checkedOnly);
            }
        }

        return size;
    }

    /**
     * Get the image representation of this element to be displayed in the tree.
     *
     * @return the image representation
     */
    public Image getImage() {
        return null;
    }

    /**
     * Returns whether or not the element is enabled (grayed and not
     * modifiable).
     *
     * @return whether or not the element is enabled
     */
    public boolean isEnabled() {
        return fEnabled;
    }

    /**
     * Returns whether or not the element is checked.
     *
     * @return whether or not the element is checked
     */
    public boolean isChecked() {
        return fChecked;
    }

    /**
     * Returns whether or not the element is visible.
     *
     * @return whether or not the element is visible
     */
    public boolean isVisible() {
        return fVisible;
    }

    /**
     * Sets whether or not the element should be enabled (grayed and not
     * modifiable).
     *
     * @param enabled
     *            if the element should be enabled
     */
    public void setEnabled(boolean enabled) {
        fEnabled = enabled;
    }

    /**
     * Sets whether or not the element should be checked.
     *
     * @param checked
     *            if the element should be checked
     */
    public void setChecked(boolean checked) {
        fChecked = checked;
    }

    /**
     * Sets whether or not the element is visible.
     *
     * @param visible
     *            if the element should be visible
     */
    public void setVisible(boolean visible) {
        fVisible = visible;
    }
}