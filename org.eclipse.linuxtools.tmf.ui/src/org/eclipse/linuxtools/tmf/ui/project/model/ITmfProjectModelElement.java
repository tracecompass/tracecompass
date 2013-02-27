/*******************************************************************************
 * Copyright (c) 2010, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import java.net.URI;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

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
     * @return the name of the project element.
     */
    public String getName();
    /**
     * Returns the resource associated with the project model element.
     * @return the model resource.
     */
    public IResource getResource();
    /**
     * Returns the path of the project model resource.
     * @return the resource path.
     */
    public IPath getPath();
    /**
     * Returns the URI (location) of the resource.
     * @return the resource URI.
     */
    public URI getLocation();
    /**
     * Returns the project model element.
     * @return the project model element.
     */
    public TmfProjectElement getProject();
    /**
     * Returns the parent of this model element.
     * @return the parent of this model element.
     */
    public ITmfProjectModelElement getParent();
    /**
     * Returns whether this model element has children or not.
     * @return <code>true</code> if this model has children else <code>false</code>
     */
    public boolean hasChildren();
    /**
     * Returns a list of children model elements.
     * @return a list of children model elements.
     */
    public List<ITmfProjectModelElement> getChildren();
    /**
     * Method to add a child to the model element.
     * @param child A child element to add.
     */
    public void addChild(ITmfProjectModelElement child);
    /**
     * Method to remove a child from the model element.
     * @param child A child element to remove
     */
    public void removeChild(ITmfProjectModelElement child);
    /**
     * Method to request to refresh the project.
     */
    public void refresh();
}
