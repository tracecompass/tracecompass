/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Added supplementary files/folder handling
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;

/**
 * The implementation of the base TMF project model element. It provides default implementation
 * of the <code>ITmfProjectModelElement</code> interface.
 * <p>
 * @version 1.0
 * @author Francois Chouinard
 */
public abstract class TmfProjectModelElement implements ITmfProjectModelElement, IResourceChangeListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final String fName;
    /**
     * The project model element resource.
     */
    protected final IResource fResource;
    /**
     * The project model resource location (URI)
     */
    protected final URI fLocation;
    /**
     * The project model path of a resource.
     */
    protected final IPath fPath;
    private final ITmfProjectModelElement fParent;
    /**
     * The list of children elements.
     */
    protected final List<ITmfProjectModelElement> fChildren;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * Creates a base project model element.
     * @param name The name of the element.
     * @param resource The element resource.
     * @param parent The parent model element.
     */
    protected TmfProjectModelElement(String name, IResource resource, ITmfProjectModelElement parent) {
        fName = name;
        fResource = resource;
        fPath = resource.getFullPath();
        fLocation = resource.getLocationURI();
        fParent = parent;
        fChildren = new ArrayList<ITmfProjectModelElement>();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
    }

    private void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
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
        if (child instanceof TmfProjectModelElement) {
            ((TmfProjectModelElement) child).dispose();
        }
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
        result = prime * result + ((fPath == null) ? 0 : fPath.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof TmfProjectModelElement)) {
            return false;
        }
        TmfProjectModelElement element = (TmfProjectModelElement) other;
        return element.fPath.equals(fPath);
    }

    /**
     * Returns the trace specific supplementary directory under the project's supplementary folder.
     * The folder will be created if it doesn't exist.
     *
     * @param supplFoldername - folder name.
     * @return returns the trace specific supplementary directory
     */
    public IFolder getTraceSupplementaryFolder(String supplFoldername) {
        IFolder supplFolderParent = getSupplementaryFolderParent();
        return supplFolderParent.getFolder(supplFoldername);
    }

    /**
     * Returns the supplementary folder for this project
     *
     * @return the supplementary folder for this project
     */
    public IFolder getSupplementaryFolderParent() {
        TmfProjectElement project = getProject();
        IProject projectResource = project.getResource();
        IFolder supplFolderParent = projectResource.getFolder(TmfCommonConstants.TRACE_SUPPLEMENATARY_FOLDER_NAME);

        if (!supplFolderParent.exists()) {
            try {
                supplFolderParent.create(true, true, new NullProgressMonitor());
            } catch (CoreException e) {
                Activator.getDefault().logError("Error creating project specific supplementary folder " + supplFolderParent, e); //$NON-NLS-1$
            }
        }
        return supplFolderParent;
    }
}
