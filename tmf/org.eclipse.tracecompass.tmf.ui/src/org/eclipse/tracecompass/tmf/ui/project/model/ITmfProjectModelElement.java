/*******************************************************************************
 * Copyright (c) 2010, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.graphics.Image;

/**
 * The TMF project model interface.
 *
 * The TMF tracing project is integrated in the Common Navigator framework.
 * Each tracing tree element has to implement this interface to be visible in the
 * Project Explorer.
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public interface ITmfProjectModelElement {

    /**
     * Returns the name of the project model element.
     *
     * @return the name of the project element.
     */
    String getName();

    /**
     * Returns the resource associated with the project model element.
     *
     * @return the model resource.
     */
    IResource getResource();

    /**
     * Returns the path of the project model resource.
     *
     * @return the resource path.
     */
    IPath getPath();

    /**
     * Returns the URI (location) of the resource.
     *
     * @return the resource URI.
     */
    URI getLocation();

    /**
     * Returns the project model element.
     *
     * @return the project model element.
     */
    TmfProjectElement getProject();

    /**
     * Returns the parent of this model element.
     *
     * @return the parent of this model element.
     */
    ITmfProjectModelElement getParent();

    /**
     * Returns a list of children model elements.
     *
     * @return a list of children model elements.
     */
    List<ITmfProjectModelElement> getChildren();

    /**
     * Returns the child model element with the given name.
     *
     * @param name
     *            the child name
     *
     * @return the child model element with the given name, or null.
     * @since 3.1
     */
    default ITmfProjectModelElement getChild(String name) {
        for (ITmfProjectModelElement child : getChildren()) {
            if (Objects.equals(child.getName(), name)) {
                return child;
            }
        }
        return null;
    }

    /**
     * Method to request to refresh the project.
     */
    void refresh();

    /**
     * Returns the icon of this element.
     *
     * @return The icon
     * @since 2.0
     */
    Image getIcon();

    /**
     * Returns the text of the label of this element.
     *
     * @return The label text
     * @since 2.0
     */
    default String getLabelText() {
        return getName();
    }

    /**
     * Returns whether this model element has children or not.
     *
     * @return <code>true</code> if this model element has children else
     *         <code>false</code>
     */
    default boolean hasChildren() {
        return !getChildren().isEmpty();
    }

    /**
     * Recursively dispose of the element and its children.
     *
     * @since 2.3
     */
    default void dispose() {
        getChildren().forEach(element -> element.dispose());
        /* override to perform any necessary cleanup */
    }
}
