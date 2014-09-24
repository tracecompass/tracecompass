/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg;

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
    private TracePackageElement[] fChildren;
    private final TracePackageElement fParent;
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
        fChildren = new TracePackageElement[0];
    }

    /**
     * @return the parent of this element or null if there is no parent
     */
    public TracePackageElement getParent() {
        return fParent;
    }

    /**
     * Get the text representation of this element to be displayed in the tree.
     *
     * @return the text representation
     */
    abstract public String getText();

    /**
     * Get the children of this element
     *
     * @return the children of this element
     */
    public TracePackageElement[] getChildren() {
        return fChildren;
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
     * Set the children of this element
     *
     * @param children
     *            the children of this element
     */
    public void setChildren(TracePackageElement[] children) {
        this.fChildren = children;
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