/*******************************************************************************
 * Copyright (c) 2010, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Added supplementary files/folder handling
 *   Patrick Tasse - Refactor resource change listener
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.project.model.TmfProjectModelHelper;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;

import com.google.common.collect.ImmutableList;

/**
 * The implementation of the base TMF project model element. It provides default implementation
 * of the <code>ITmfProjectModelElement</code> interface.
 * <p>
 * @version 1.0
 * @author Francois Chouinard
 */
public abstract class TmfProjectModelElement implements ITmfProjectModelElement, IAdaptable {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final String fName;

    /** The project model element resource */
    private final IResource fResource;

    /** The project model resource location (URI) */
    private final URI fLocation;

    /** The project model path of a resource */
    private final IPath fPath;

    private final ITmfProjectModelElement fParent;

    /** The list of children elements */
    private final @NonNull List<ITmfProjectModelElement> fChildren;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * Creates a base project model element.
     *
     * @param name
     *            The name of the element.
     * @param resource
     *            The element resource.
     * @param parent
     *            The parent model element.
     */
    protected TmfProjectModelElement(String name, IResource resource, ITmfProjectModelElement parent) {
        fName = name;
        fResource = resource;
        fPath = resource.getFullPath();
        fLocation = new File(resource.getLocationURI()).toURI();
        fParent = parent;
        fChildren = new CopyOnWriteArrayList<>();
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
    public TmfProjectElement getProject() {
        return fParent.getProject();
    }

    @Override
    public ITmfProjectModelElement getParent() {
        return fParent;
    }

    @Override
    public List<ITmfProjectModelElement> getChildren() {
        return ImmutableList.copyOf(fChildren);
    }

    @Override
    public void refresh() {
        // make sure the model is updated in the current thread
        refreshChildren();

        refreshViewer();
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
        if (!(other.getClass().equals(this.getClass()))) {
            return false;
        }
        TmfProjectModelElement element = (TmfProjectModelElement) other;
        return element.fPath.equals(fPath);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Refresh the children of this model element, adding new children and
     * removing dangling children as necessary. The remaining children should
     * also refresh their own children sub-tree.
     * <p>
     * The method implementation must be thread-safe.
     *
     * @since 2.0
     */
    protected abstract void refreshChildren();

    /**
     * Refresh the common navigator viewer starting with this element. Does not
     * refresh the model.
     *
     * @since 3.1
     */
    public void refreshViewer() {
        Display.getDefault().asyncExec(() -> {
            IWorkbench wb = PlatformUI.getWorkbench();
            IWorkbenchWindow wbWindow = wb.getActiveWorkbenchWindow();
            if (wbWindow == null) {
                return;
            }
            IWorkbenchPage activePage = wbWindow.getActivePage();
            if (activePage == null) {
                return;
            }

            for (IViewReference viewReference : activePage.getViewReferences()) {
                IViewPart viewPart = viewReference.getView(false);
                if (viewPart instanceof CommonNavigator) {
                    CommonViewer commonViewer = ((CommonNavigator) viewPart).getCommonViewer();
                    Object element = TmfProjectModelElement.this;
                    if (element instanceof TmfProjectElement) {
                        IProject project = (IProject) getResource();
                        if (project != null && !TmfProjectModelHelper.isShadowProject(project)) {
                            // for the project element the viewer uses the IProject resource
                            element = getResource();
                        }
                    }
                    commonViewer.refresh(element);
                }
            }
        });
    }

    /**
     * Add a new child element to this element.
     *
     * @param child
     *            The child to add
     */
    protected void addChild(ITmfProjectModelElement child) {
        fChildren.add(child);
    }

    /**
     * Remove an element from the current child elements.
     * <p>
     * Disposes the removed element. It should no longer be used.
     *
     * @param child
     *            The child to remove
     */
    protected void removeChild(ITmfProjectModelElement child) {
        fChildren.remove(child);
        child.dispose();
    }

    /**
     * Returns the trace specific supplementary folder under the project's
     * supplementary folder. The returned folder and its parent folders may not
     * exist.
     *
     * @param supplFolderPath
     *            folder path relative to the project's supplementary folder
     * @return the trace specific supplementary folder
     */
    public IFolder getTraceSupplementaryFolder(String supplFolderPath) {
        TmfProjectElement project = getProject();
        IFolder supplFolderParent = project.getSupplementaryFolder();
        return supplFolderParent.getFolder(supplFolderPath);
    }

    /**
     * Returns the trace specific supplementary folder under the project's
     * supplementary folder. Its parent folders will be created if they don't exist.
     * If createFolder is true, the returned folder will be created, otherwise it
     * may not exist.
     *
     * @param supplFolderPath
     *            folder path relative to the project's supplementary folder
     * @param createFolder
     *            if true, the returned folder will be created
     * @param progressMonitor
     *            the progress monitor
     * @return the trace specific supplementary folder
     * @since 4.0
     */
    public IFolder prepareTraceSupplementaryFolder(String supplFolderPath, boolean createFolder, IProgressMonitor progressMonitor) {
        SubMonitor subMonitor = SubMonitor.convert(progressMonitor);
        IFolder folder = getTraceSupplementaryFolder(supplFolderPath);
        IFolder propertiesFolder = folder.getFolder(TmfCommonConstants.TRACE_PROPERTIES_FOLDER);
        if ((createFolder && propertiesFolder.exists() && propertiesFolder.isHidden()) ||
                (!createFolder && folder.getParent().exists())) {
            return folder;
        }
        try {
            ICoreRunnable runnable = monitor -> {
                if (createFolder) {
                    TraceUtils.createFolder(propertiesFolder, monitor);
                    propertiesFolder.setHidden(true);
                } else {
                    TraceUtils.createFolder((IFolder) folder.getParent(), monitor);
                }
            };
            ResourcesPlugin.getWorkspace().run(runnable, folder.getProject(), IWorkspace.AVOID_UPDATE, subMonitor);
        } catch (CoreException e) {
            Activator.getDefault().logError("Error creating supplementary folder " + folder.getFullPath(), e); //$NON-NLS-1$
        }
        return folder;
    }

    /**
     * Returns the trace specific supplementary folder under the project's
     * supplementary folder. Its parent folders will be created if they don't exist.
     * If createFolder is true, the returned folder will be created, otherwise it
     * may not exist.
     *
     * @param supplFolderPath
     *            folder path relative to the project's supplementary folder
     * @param createFolder
     *            if true, the returned folder will be created
     * @return the trace specific supplementary folder
     */
    public IFolder prepareTraceSupplementaryFolder(String supplFolderPath, boolean createFolder) {
        return prepareTraceSupplementaryFolder(supplFolderPath, createFolder, new NullProgressMonitor());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + getPath() + ')';
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> @Nullable T getAdapter(Class<T> adapter) {
        if (adapter.getClass().isInstance(this)) {
            return (T)this;
        }
        if (IWorkbenchAdapter.class.isAssignableFrom(adapter)) {
            return ((@Nullable T) new WorkbenchAdapter() {
                @Override
                public String getLabel(Object object) {
                    return ((TmfProjectModelElement)object).getName();
                }
            });
        }
        return null;
    }
}
