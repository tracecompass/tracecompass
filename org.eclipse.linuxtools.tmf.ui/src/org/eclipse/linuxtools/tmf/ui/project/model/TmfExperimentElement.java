/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Geneviève Bastien - Copied code to add/remove traces in this class
 *   Patrick Tasse - Close editors to release resources
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource2;

/**
 * Implementation of TMF Experiment Model Element.
 * <p>
 * @version 1.0
 * @author Francois Chouinard
 *
 */
public class TmfExperimentElement extends TmfWithFolderElement implements IPropertySource2 {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    // Property View stuff
    private static final String sfInfoCategory = "Info"; //$NON-NLS-1$
    private static final String sfName = "name"; //$NON-NLS-1$
    private static final String sfPath = "path"; //$NON-NLS-1$
    private static final String sfLocation = "location"; //$NON-NLS-1$
    private static final String sfFolderSuffix = "_exp"; //$NON-NLS-1$

    private static final ReadOnlyTextPropertyDescriptor sfNameDescriptor = new ReadOnlyTextPropertyDescriptor(sfName, sfName);
    private static final ReadOnlyTextPropertyDescriptor sfPathDescriptor = new ReadOnlyTextPropertyDescriptor(sfPath, sfPath);
    private static final ReadOnlyTextPropertyDescriptor sfLocationDescriptor = new ReadOnlyTextPropertyDescriptor(sfLocation,
            sfLocation);

    private static final IPropertyDescriptor[] sfDescriptors = { sfNameDescriptor, sfPathDescriptor,
            sfLocationDescriptor };

    static {
        sfNameDescriptor.setCategory(sfInfoCategory);
        sfPathDescriptor.setCategory(sfInfoCategory);
        sfLocationDescriptor.setCategory(sfInfoCategory);
    }

    private static final String BOOKMARKS_HIDDEN_FILE = ".bookmarks"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param name The name of the experiment
     * @param folder The folder reference
     * @param parent The experiment folder reference.
     */
    public TmfExperimentElement(String name, IFolder folder, TmfExperimentFolder parent) {
        super(name, folder, parent);
        parent.addChild(this);
    }

    // ------------------------------------------------------------------------
    // TmfProjectModelElement
    // ------------------------------------------------------------------------

    @Override
    public IFolder getResource() {
        return (IFolder) fResource;
    }

    @Override
    public TmfProjectElement getProject() {
        return (TmfProjectElement) getParent().getParent();
    }

    @Override
    void refreshChildren() {
        IFolder folder = getResource();

        // Get the children from the model
        Map<String, ITmfProjectModelElement> childrenMap = new HashMap<>();
        for (ITmfProjectModelElement element : getChildren()) {
            childrenMap.put(element.getResource().getName(), element);
        }

        try {
            IResource[] members = folder.members();
            for (IResource resource : members) {
                String name = resource.getName();
                ITmfProjectModelElement element = childrenMap.get(name);
                if (element instanceof TmfTraceElement) {
                    childrenMap.remove(name);
                } else if (!resource.isHidden()) {
                    // exclude hidden resources (e.g. bookmarks file)
                    element = new TmfTraceElement(name, resource, this);
                }
            }
        } catch (CoreException e) {
        }

        // Cleanup dangling children from the model
        for (ITmfProjectModelElement danglingChild : childrenMap.values()) {
            removeChild(danglingChild);
        }
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Returns a list of TmfTraceElements contained in this experiment.
     * @return a list of TmfTraceElements
     */
    public List<TmfTraceElement> getTraces() {
        List<ITmfProjectModelElement> children = getChildren();
        List<TmfTraceElement> traces = new ArrayList<>();
        for (ITmfProjectModelElement child : children) {
            if (child instanceof TmfTraceElement) {
                traces.add((TmfTraceElement) child);
            }
        }
        return traces;
    }


    /**
     * Adds a trace to the experiment
     *
     * @param trace The trace element to add
     * @since 2.0
     */
    public void addTrace(TmfTraceElement trace) {
        /**
         * Create a link to the actual trace and set the trace type
         */
        IFolder experiment = getResource();
        IResource resource = trace.getResource();
        IPath location = resource.getLocation();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {
            Map<QualifiedName, String> properties = trace.getResource().getPersistentProperties();
            String bundleName = properties.get(TmfCommonConstants.TRACEBUNDLE);
            String traceType = properties.get(TmfCommonConstants.TRACETYPE);
            String iconUrl = properties.get(TmfCommonConstants.TRACEICON);

            if (resource instanceof IFolder) {
                IFolder folder = experiment.getFolder(trace.getName());
                if (workspace.validateLinkLocation(folder, location).isOK()) {
                    folder.createLink(location, IResource.REPLACE, null);
                    setProperties(folder, bundleName, traceType, iconUrl);

                } else {
                    Activator.getDefault().logError("Error creating link. Invalid trace location " + location); //$NON-NLS-1$
                }
            } else {
                IFile file = experiment.getFile(trace.getName());
                if (workspace.validateLinkLocation(file, location).isOK()) {
                    file.createLink(location, IResource.REPLACE, null);
                    setProperties(file, bundleName, traceType, iconUrl);
                } else {
                    Activator.getDefault().logError("Error creating link. Invalid trace location " + location); //$NON-NLS-1$
                }
            }
        } catch (CoreException e) {
            Activator.getDefault().logError("Error creating link to location " + location, e); //$NON-NLS-1$
        }

    }

    /**
     * Removes a trace from an experiment
     *
     * @param trace The trace to remove
     * @throws CoreException exception
     * @since 2.0
     */
    public void removeTrace(TmfTraceElement trace) throws CoreException {

        // Close the experiment if open
        closeEditors();

        /* Finally, remove the trace from experiment*/
        removeChild(trace);
        trace.getResource().delete(true, null);
        deleteSupplementaryResources();
    }

    private static void setProperties(IResource resource, String bundleName,
            String traceType, String iconUrl) throws CoreException {
        resource.setPersistentProperty(TmfCommonConstants.TRACEBUNDLE, bundleName);
        resource.setPersistentProperty(TmfCommonConstants.TRACETYPE, traceType);
        resource.setPersistentProperty(TmfCommonConstants.TRACEICON, iconUrl);
    }

    /**
     * Returns the file resource used to store bookmarks after creating it if necessary.
     * The file will be created if it does not exist.
     * @return the bookmarks file
     * @throws CoreException if the bookmarks file cannot be created
     * @since 2.0
     */
    public IFile createBookmarksFile() throws CoreException {
        IFile file = getBookmarksFile();
        if (!file.exists()) {
            final IFile bookmarksFile = getProject().getExperimentsFolder().getResource().getFile(BOOKMARKS_HIDDEN_FILE);
            if (!bookmarksFile.exists()) {
                final InputStream source = new ByteArrayInputStream(new byte[0]);
                bookmarksFile.create(source, true, null);
            }
            bookmarksFile.setHidden(true);
            file.createLink(bookmarksFile.getLocation(), IResource.REPLACE, null);
            file.setHidden(true);
            file.setPersistentProperty(TmfCommonConstants.TRACETYPE, TmfExperiment.class.getCanonicalName());
        }
        return file;
    }

    /**
     * Returns the file resource used to store bookmarks.
     * The file may not exist.
     * @return the bookmarks file
     * @since 2.0
     */
    public IFile getBookmarksFile() {
        final IFolder folder = (IFolder) fResource;
        IFile file = folder.getFile(getName() + '_');
        return file;
    }

    // ------------------------------------------------------------------------
    // IPropertySource2
    // ------------------------------------------------------------------------

    @Override
    public Object getEditableValue() {
        return null;
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return Arrays.copyOf(sfDescriptors, sfDescriptors.length);
    }

    @Override
    public Object getPropertyValue(Object id) {

        if (sfName.equals(id)) {
            return getName();
        }

        if (sfPath.equals(id)) {
            return getPath().toString();
        }

        if (sfLocation.equals(id)) {
            return getLocation().toString();
        }

        return null;
    }

    @Override
    public void resetPropertyValue(Object id) {
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
    }

    @Override
    public boolean isPropertyResettable(Object id) {
        return false;
    }

    @Override
    public boolean isPropertySet(Object id) {
        return false;
    }

    /**
     * Return the suffix for resource names
     * @return The folder suffix
     */
    @Override
    public String getSuffix() {
        return sfFolderSuffix;
    }

    /**
     * Close open editors associated with this experiment.
     * @since 2.0
     */
    public void closeEditors() {
        IFile file = getBookmarksFile();
        FileEditorInput input = new FileEditorInput(file);
        IWorkbench wb = PlatformUI.getWorkbench();
        for (IWorkbenchWindow wbWindow : wb.getWorkbenchWindows()) {
            for (IWorkbenchPage wbPage : wbWindow.getPages()) {
                for (IEditorReference editorReference : wbPage.getEditorReferences()) {
                    try {
                        if (editorReference.getEditorInput().equals(input)) {
                            wbPage.closeEditor(editorReference.getEditor(false), false);
                        }
                    } catch (PartInitException e) {
                        Activator.getDefault().logError("Error closing editor for experiment " + getName(), e); //$NON-NLS-1$
                    }
                }
            }
        }
    }

    /**
     * Get the list of analysis elements
     *
     * @return Array of analysis elements
     * @since 3.0
     */
    public List<TmfAnalysisElement> getAvailableAnalysis() {
        List<TmfAnalysisElement> list = new ArrayList<>();

        /**
         * TODO : implement this cleanly and test it when experiment types are
         * available
         */

        return list;
    }
}
