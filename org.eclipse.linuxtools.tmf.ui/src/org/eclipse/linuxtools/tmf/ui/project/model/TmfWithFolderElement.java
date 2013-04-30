/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Added supplementary files handling (in class TmfTraceElement)
 *   Geneviève Bastien - Copied supplementary files handling from TmfTracElement
 *                       Moved to this class code to copy a model element
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;


/**
 * Base class for project elements who will have folder elements
 * under them to store supplementary files.
 *
 * @author gbastien
 * @since 2.0
 */
public abstract class TmfWithFolderElement extends TmfProjectModelElement {

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------


    /**
     * Constructor.
     * Creates model element.
     * @param name The name of the element
     * @param resource The resource.
     * @param parent The parent element
     */
    public TmfWithFolderElement(String name, IResource resource, TmfProjectModelElement parent) {
        super(name, resource, parent);
    }

    /**
     * Return the resource name for this element
     *
     * @return The name of the resource for this element
     */
    protected String getResourceName() {
        return fResource.getName() + getSuffix();
    }

    /**
     * @return The suffix for resource names
     */
    protected String getSuffix() {
        return ""; //$NON-NLS-1$
    }

    /**
     * Deletes this element specific supplementary folder.
     */
    public void deleteSupplementaryFolder() {
        IFolder supplFolder = getTraceSupplementaryFolder(getResourceName());
        if (supplFolder.exists()) {
            try {
                supplFolder.delete(true, new NullProgressMonitor());
            } catch (CoreException e) {
                Activator.getDefault().logError("Error deleting supplementary folder " + supplFolder, e); //$NON-NLS-1$
            }
        }
    }

    /**
     * Renames the element specific supplementary folder according to the new element name.
     *
     * @param newName The new element name
     */
    public void renameSupplementaryFolder(String newName) {
        IFolder oldSupplFolder = getTraceSupplementaryFolder(getResourceName());
        IFolder newSupplFolder =  getTraceSupplementaryFolder(newName + getSuffix());

        // Rename supplementary folder
        if (oldSupplFolder.exists()) {
            try {
                oldSupplFolder.move(newSupplFolder.getFullPath(), true, new NullProgressMonitor());
            } catch (CoreException e) {
                Activator.getDefault().logError("Error renaming supplementary folder " + oldSupplFolder, e); //$NON-NLS-1$
            }
        }
    }

    /**
     * Copies the element specific supplementary folder to the new element name.
     *
     * @param newName The new element name
     */
    public void copySupplementaryFolder(String newName) {
        IFolder oldSupplFolder = getTraceSupplementaryFolder(getResourceName());
        IFolder newSupplFolder = getTraceSupplementaryFolder(newName + getSuffix());

        // copy supplementary folder
        if (oldSupplFolder.exists()) {
            try {
                oldSupplFolder.copy(newSupplFolder.getFullPath(), true, new NullProgressMonitor());
            } catch (CoreException e) {
                Activator.getDefault().logError("Error renaming supplementary folder " + oldSupplFolder, e); //$NON-NLS-1$
            }
        }
    }

    /**
     * Copies the element specific supplementary folder a new folder.
     *
     * @param destination The destination folder to copy to.
     */
    public void copySupplementaryFolder(IFolder destination) {
        IFolder oldSupplFolder = getTraceSupplementaryFolder(getResourceName());

        // copy supplementary folder
        if (oldSupplFolder.exists()) {
            try {
                oldSupplFolder.copy(destination.getFullPath(), true, new NullProgressMonitor());
            } catch (CoreException e) {
                Activator.getDefault().logError("Error copying supplementary folder " + oldSupplFolder, e); //$NON-NLS-1$
            }
        }
    }


    /**
     * Refreshes the element specific supplementary folder information. It creates the folder if not exists.
     * It sets the persistence property of the trace resource
     */
    public void refreshSupplementaryFolder() {
        createSupplementaryDirectory();
    }

    /**
     * Checks if supplementary resource exist or not.
     *
     * @return <code>true</code> if one or more files are under the element supplementary folder
     */
    public boolean hasSupplementaryResources() {
        IResource[] resources = getSupplementaryResources();
        return (resources.length > 0);
    }

    /**
     * Returns the supplementary resources under the trace supplementary folder.
     *
     * @return array of resources under the trace supplementary folder.
     */
    public IResource[] getSupplementaryResources() {
        IFolder supplFolder = getTraceSupplementaryFolder(getResourceName());
        if (supplFolder.exists()) {
            try {
                return supplFolder.members();
            } catch (CoreException e) {
                Activator.getDefault().logError("Error deleting supplementary folder " + supplFolder, e); //$NON-NLS-1$
            }
        }
        return new IResource[0];
    }

    /**
     * Deletes the given resources.
     *
     * @param resources array of resources to delete.
     */
    public void deleteSupplementaryResources(IResource[] resources) {

        for (int i = 0; i < resources.length; i++) {
            try {
                resources[i].delete(true, new NullProgressMonitor());
            } catch (CoreException e) {
                Activator.getDefault().logError("Error deleting supplementary resource " + resources[i], e); //$NON-NLS-1$
            }
        }
    }

    /**
     * Deletes all supplementary resources in the supplementary directory
     */
    public void deleteSupplementaryResources() {
        deleteSupplementaryResources(getSupplementaryResources());
    }

    private void createSupplementaryDirectory() {
        IFolder supplFolder = getTraceSupplementaryFolder(getResourceName());
        if (!supplFolder.exists()) {
            try {
                supplFolder.create(true, true, new NullProgressMonitor());
            } catch (CoreException e) {
                Activator.getDefault().logError("Error creating resource supplementary file " + supplFolder, e); //$NON-NLS-1$
            }
        }

        try {
            fResource.setPersistentProperty(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER, supplFolder.getLocationURI().getPath());
        } catch (CoreException e) {
            Activator.getDefault().logError("Error setting persistant property " + TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER, e); //$NON-NLS-1$
        }

    }

    /**
     * Copy this model element
     *
     * @param newName The name of the new element
     * @param copySuppFiles Whether to copy supplementary files or not
     * @return the new Resource object
     */
    public IResource copy(final String newName, final boolean copySuppFiles) {

        final IPath newPath = getParent().getResource().getFullPath().addTrailingSeparator().append(newName);

        /* Copy supplementary files first, only if needed */
        if (copySuppFiles) {
            copySupplementaryFolder(newName);
        }
        /* Copy the trace */
        try {
            getResource().copy(newPath, IResource.FORCE | IResource.SHALLOW, null);

            /* Delete any bookmarks file found in copied trace folder */
            IFolder folder = ((IFolder)getParent().getResource()).getFolder(newName);
            if (folder.exists()) {
                for (IResource member : folder.members()) {
                    if (TmfTrace.class.getCanonicalName().equals(member.getPersistentProperty(TmfCommonConstants.TRACETYPE))) {
                        member.delete(true, null);
                    }
                    if (TmfExperiment.class.getCanonicalName().equals(member.getPersistentProperty(TmfCommonConstants.TRACETYPE))) {
                        member.delete(true, null);
                    }
                }
            }
            return folder;
        } catch (CoreException e) {

        }

        return null;

    }

}
