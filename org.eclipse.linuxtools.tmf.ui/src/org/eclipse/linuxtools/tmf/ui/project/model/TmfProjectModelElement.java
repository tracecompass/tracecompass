/*******************************************************************************
 * Copyright (c) 2010 Ericsson
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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * <b><u>TmfProjectModelElement</u></b>
 * <p>
 */
public abstract class TmfProjectModelElement implements ITmfProjectModelElement, IResourceChangeListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final String fName;
    protected final IResource fResource;
    protected final URI fLocation;
    protected final IPath fPath;
    private final ITmfProjectModelElement fParent;
    protected final List<ITmfProjectModelElement> fChildren;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    protected TmfProjectModelElement(String name, IResource resource, ITmfProjectModelElement parent) {
        fName = name;
        fResource = resource;
        fPath = resource.getFullPath();
        fLocation = resource.getLocationURI();
        fParent = parent;
        fChildren = new ArrayList<ITmfProjectModelElement>();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
    }

    // ------------------------------------------------------------------------
    // ITmfProjectModelElement
    // ------------------------------------------------------------------------

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public IResource getResource() {
        return fResource;
    }

    @Override
    public IPath getPath() {
        return fPath;
    }

    @Override
    public URI getLocation() {
        return fLocation;
    }

    @Override
    public ITmfProjectModelElement getParent() {
        return fParent;
    }

    @Override
    public boolean hasChildren() {
        return fChildren.size() > 0;
    }

    @Override
    public List<ITmfProjectModelElement> getChildren() {
        return fChildren;
    }

    @Override
    public void addChild(ITmfProjectModelElement child) {
        fChildren.add(child);
    }

    @Override
    public void removeChild(ITmfProjectModelElement child) {
        fChildren.remove(child);
        refresh();
    }

    @Override
    public void refresh() {
        // Do nothing by default: sub-classes override this on an "as-needed"
        // basis.
    }

    // ------------------------------------------------------------------------
    // IResourceChangeListener
    // ------------------------------------------------------------------------

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        // Do nothing by default: sub-classes override this on an "as-needed"
        // basis.
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fLocation == null) ? 0 : fLocation.hashCode());
        result = prime * result + ((fName == null) ? 0 : fName.hashCode());
        result = prime * result + ((fPath == null) ? 0 : fPath.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null)
            return false;
        if (!(other instanceof TmfProjectModelElement))
            return false;
        TmfProjectModelElement element = (TmfProjectModelElement) other;
        return element.fName.equals(fName) && element.fLocation.equals(fLocation);
    }

}
