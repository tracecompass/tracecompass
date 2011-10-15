/*******************************************************************************
 * Copyright (c) 2010, 2011 Ericsson
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
 * <b><u>ITmfProjectModelElement</u></b>
 * <p>
 * TODO: Make ITmfProjectModelElement extend IAdaptable
 */
public interface ITmfProjectModelElement {

    public String getName();

    public IResource getResource();
    
    public IPath getPath();

    public URI getLocation();

    public TmfProjectElement getProject();

    public ITmfProjectModelElement getParent();

    public boolean hasChildren();

    public List<ITmfProjectModelElement> getChildren();

    public void addChild(ITmfProjectModelElement child);

    public void removeChild(ITmfProjectModelElement child);

    public void refresh();
}
