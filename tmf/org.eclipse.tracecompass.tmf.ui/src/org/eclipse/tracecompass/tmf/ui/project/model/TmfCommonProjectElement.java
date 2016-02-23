/*******************************************************************************
 * Copyright (c) 2010, 2015 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Added supplementary files handling (in class TmfTraceElement)
 *   Geneviève Bastien - Copied supplementary files handling from TmfTracElement
 *                 Moved to this class code to copy a model element
 *                 Renamed from TmfWithFolderElement to TmfCommonProjectElement
 *   Patrick Tasse - Add support for folder elements
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.editors.ITmfEventsEditorConstants;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType.TraceElementType;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.osgi.framework.Bundle;

/**
 * Base class for tracing project elements: it implements the common behavior of
 * all project elements: supplementary files, analysis, types, etc.
 *
 * @author Geneviève Bastien
 */
public abstract class TmfCommonProjectElement extends TmfProjectModelElement {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static final String BOOKMARKS_HIDDEN_FILE = ".bookmarks"; //$NON-NLS-1$

    private TmfViewsElement fViewsElement = null;

    /** This trace type ID as defined in plugin.xml */
    private String fTraceTypeId = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor. Creates model element.
     *
     * @param name
     *            The name of the element
     * @param resource
     *            The resource.
     * @param parent
     *            The parent element
     */
    public TmfCommonProjectElement(String name, IResource resource, TmfProjectModelElement parent) {
        super(name, resource, parent);
        refreshTraceType();
        TmfSignalManager.register(this);
    }

    // ------------------------------------------------------------------------
    // TmfProjectModelElement
    // ------------------------------------------------------------------------

    /**
     * @since 2.0
     */
    @Override
    protected void refreshChildren() {
        /* Get the base path to put the resource to */
        IPath tracePath = getResource().getFullPath();

        if (fViewsElement == null) {
            /* Add the "Views" node */
            IFolder viewsNodeRes = ResourcesPlugin.getWorkspace().getRoot().getFolder(tracePath.append(TmfViewsElement.PATH_ELEMENT));
            fViewsElement = new TmfViewsElement(viewsNodeRes, this);
            addChild(fViewsElement);
        }
        fViewsElement.refreshChildren();
    }

    /**
     * @since 2.0
     */
    @Override
    public Image getIcon() {
        String traceType = getTraceType();
        if (traceType == null || TmfTraceType.getTraceType(traceType) == null) {
            // request the label to the Eclipse platform
            Image icon = TmfProjectModelIcons.WORKSPACE_LABEL_PROVIDER.getImage(getResource());
            return (icon == null ? TmfProjectModelIcons.DEFAULT_TRACE_ICON : icon);
        }

        IConfigurationElement traceUIAttributes = TmfTraceTypeUIUtils.getTraceUIAttributes(traceType,
                (this instanceof TmfTraceElement) ? TraceElementType.TRACE : TraceElementType.EXPERIMENT);
        if (traceUIAttributes != null) {
            String iconAttr = traceUIAttributes.getAttribute(TmfTraceTypeUIUtils.ICON_ATTR);
            if (iconAttr != null) {
                String name = traceUIAttributes.getContributor().getName();
                if (name != null) {
                    Bundle bundle = Platform.getBundle(name);
                    if (bundle != null) {
                        Image image = TmfProjectModelIcons.loadIcon(bundle, iconAttr);
                        if (image != null) {
                            return image;
                        }
                    }
                }
            }
        }
        /* Let subclasses specify an icon */
        return null;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Get the child element "Views". There should always be one.
     *
     * @return The child element
     * @since 2.0
     */
    protected TmfViewsElement getChildElementViews() {
        return fViewsElement;
    }

    /**
     * Returns the trace type ID.
     *
     * @return trace type ID.
     */
    public String getTraceType() {
        return fTraceTypeId;
    }

    /**
     * Refreshes the trace type field by reading the trace type persistent
     * property of the resource.
     */
    public void refreshTraceType() {
        try {
            fTraceTypeId = TmfTraceType.getTraceTypeId(getResource());
        } catch (CoreException e) {
            Activator.getDefault().logError(NLS.bind(Messages.TmfCommonProjectElement_ErrorRefreshingProperty, getName()), e);
        }
    }

    /**
     * Instantiate a <code>ITmfTrace</code> object based on the trace type and
     * the corresponding extension.
     *
     * @return the <code>ITmfTrace</code> or <code>null</code> for an error
     */
    public abstract ITmfTrace instantiateTrace();

    /**
     * Return the supplementary folder path for this element. The returned path
     * is relative to the project's supplementary folder.
     *
     * @return The supplementary folder path for this element
     */
    protected String getSupplementaryFolderPath() {
        return getElementPath() + getSuffix();
    }

    /**
     * Return the element path relative to its common element (traces folder,
     * experiments folder or experiment element).
     *
     * @return The element path
     */
    public @NonNull String getElementPath() {
        ITmfProjectModelElement parent = getParent();
        while (!(parent instanceof TmfTracesFolder || parent instanceof TmfExperimentElement || parent instanceof TmfExperimentFolder)) {
            parent = parent.getParent();
        }
        IPath path = getResource().getFullPath().makeRelativeTo(parent.getPath());
        return checkNotNull(path.toString());
    }

    /**
     * @return The suffix for the supplementary folder
     */
    protected String getSuffix() {
        return ""; //$NON-NLS-1$
    }

    /**
     * Returns a list of TmfTraceElements contained in project element.
     *
     * @return a list of TmfTraceElements, empty list if none
     */
    public List<TmfTraceElement> getTraces() {
        return new ArrayList<>();
    }

    /**
     * Get the instantiated trace associated with this element.
     *
     * @return The instantiated trace or null if trace is not (yet) available
     */
    public ITmfTrace getTrace() {
        for (ITmfTrace trace : TmfTraceManager.getInstance().getOpenedTraces()) {
            if (trace.getResource().equals(getResource())) {
                return trace;
            }
        }
        return null;
    }

    /**
     * Returns the file resource used to store bookmarks after creating it if
     * necessary. If the trace resource is a file, it is returned directly. If
     * the trace resource is a folder, a linked file is returned. The file will
     * be created if it does not exist.
     *
     * @return the bookmarks file
     * @throws CoreException
     *             if the bookmarks file cannot be created
     */
    public abstract IFile createBookmarksFile() throws CoreException;

    /**
     * Actually returns the bookmark file or creates it in the project element's
     * folder
     *
     * @param bookmarksFolder
     *            Folder where to put the bookmark file
     * @param editorInputType
     *            The editor input type to set (trace or experiment)
     * @return The bookmark file
     * @throws CoreException
     *             if the bookmarks file cannot be created
     */
    protected IFile createBookmarksFile(IFolder bookmarksFolder, String editorInputType) throws CoreException {
        IFile file = getBookmarksFile();
        if (!file.exists()) {
            final IFile bookmarksFile = bookmarksFolder.getFile(BOOKMARKS_HIDDEN_FILE);
            if (!bookmarksFile.exists()) {
                final InputStream source = new ByteArrayInputStream(new byte[0]);
                bookmarksFile.create(source, IResource.FORCE | IResource.HIDDEN, null);
            }
            file.createLink(bookmarksFile.getLocation(), IResource.REPLACE | IResource.HIDDEN, null);
            file.setPersistentProperty(TmfCommonConstants.TRACETYPE, editorInputType);
        }
        return file;
    }

    /**
     * Returns the optional editor ID from the trace type extension.
     *
     * @return the editor ID or <code>null</code> if not defined.
     */
    public abstract String getEditorId();

    /**
     * Returns the file resource used to store bookmarks. The file may not
     * exist.
     *
     * @return the bookmarks file
     */
    public IFile getBookmarksFile() {
        final IFolder folder = (IFolder) getResource();
        IFile file = folder.getFile(getName() + '_');
        return file;
    }

    /**
     * Close open editors associated with this experiment.
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
                        Activator.getDefault().logError(NLS.bind(Messages.TmfCommonProjectElement_ErrorClosingEditor, getName()), e);
                    }
                }
            }
        }
    }

    /**
     * Get a friendly name for the type of element this common project element
     * is, to be displayed in UI messages.
     *
     * @return A string for the type of project element this object is, for
     *         example "trace" or "experiment"
     */
    public abstract String getTypeName();

    /**
     * Copy this model element
     *
     * @param newName
     *            The name of the new element
     * @param copySuppFiles
     *            Whether to copy supplementary files or not
     * @return the new Resource object
     */
    public IResource copy(final String newName, final boolean copySuppFiles) {

        final IPath newPath = getParent().getResource().getFullPath().addTrailingSeparator().append(newName);

        /* Copy supplementary files first, only if needed */
        if (copySuppFiles) {
            String newElementPath = new Path(getElementPath()).removeLastSegments(1).append(newName).toString();
            copySupplementaryFolder(newElementPath);
        }
        /* Copy the trace */
        try {
            getResource().copy(newPath, IResource.FORCE | IResource.SHALLOW, null);
            IResource trace = ((IFolder) getParent().getResource()).findMember(newName);

            /* Delete any bookmarks file found in copied trace folder */
            if (trace instanceof IFolder) {
                IFolder folderTrace = (IFolder) trace;
                for (IResource member : folderTrace.members()) {
                    String traceTypeId = TmfTraceType.getTraceTypeId(member);
                    if (ITmfEventsEditorConstants.TRACE_INPUT_TYPE_CONSTANTS.contains(traceTypeId)) {
                        member.delete(true, null);
                    } else if (ITmfEventsEditorConstants.EXPERIMENT_INPUT_TYPE_CONSTANTS.contains(traceTypeId)) {
                        member.delete(true, null);
                    }
                }
            }
            return trace;
        } catch (CoreException e) {

        }
        return null;
    }

    /**
     * Get the list of analysis elements
     *
     * @return Array of analysis elements
     */
    public List<TmfAnalysisElement> getAvailableAnalysis() {
        return getChildElementViews().getChildren().stream()
            .map(elem -> (TmfAnalysisElement) elem)
            .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------------
    // Supplementary files operations
    // ------------------------------------------------------------------------

    /**
     * Deletes this element specific supplementary folder.
     */
    public void deleteSupplementaryFolder() {
        IFolder supplFolder = getTraceSupplementaryFolder(getSupplementaryFolderPath());
        try {
            deleteFolder(supplFolder);
        } catch (CoreException e) {
            Activator.getDefault().logError("Error deleting supplementary folder " + supplFolder, e); //$NON-NLS-1$
        }
    }

    private static void deleteFolder(IFolder folder) throws CoreException {
        if (folder.exists()) {
            folder.delete(true, new NullProgressMonitor());
        }
        IContainer parent = folder.getParent();
        // delete empty folders up to the parent project
        if (parent instanceof IFolder && (!parent.exists() || parent.members().length == 0)) {
            deleteFolder((IFolder) parent);
        }
    }

    /**
     * Renames the element specific supplementary folder according to the new
     * element name or path.
     *
     * @param newElementPath
     *            The new element name or path
     */
    public void renameSupplementaryFolder(String newElementPath) {
        IFolder oldSupplFolder = getTraceSupplementaryFolder(getSupplementaryFolderPath());

        // Rename supplementary folder
        try {
            if (oldSupplFolder.exists()) {
                IFolder newSupplFolder = prepareTraceSupplementaryFolder(newElementPath + getSuffix(), false);
                oldSupplFolder.move(newSupplFolder.getFullPath(), true, new NullProgressMonitor());
            }
            deleteFolder(oldSupplFolder);
        } catch (CoreException e) {
            Activator.getDefault().logError("Error renaming supplementary folder " + oldSupplFolder, e); //$NON-NLS-1$
        }
    }

    /**
     * Copies the element specific supplementary folder to the new element name
     * or path.
     *
     * @param newElementPath
     *            The new element name or path
     */
    public void copySupplementaryFolder(String newElementPath) {
        IFolder oldSupplFolder = getTraceSupplementaryFolder(getSupplementaryFolderPath());

        // copy supplementary folder
        if (oldSupplFolder.exists()) {
            try {
                IFolder newSupplFolder = prepareTraceSupplementaryFolder(newElementPath + getSuffix(), false);
                oldSupplFolder.copy(newSupplFolder.getFullPath(), true, new NullProgressMonitor());
            } catch (CoreException e) {
                Activator.getDefault().logError("Error renaming supplementary folder " + oldSupplFolder, e); //$NON-NLS-1$
            }
        }
    }

    /**
     * Copies the element specific supplementary folder a new folder.
     *
     * @param destination
     *            The destination folder to copy to.
     */
    public void copySupplementaryFolder(IFolder destination) {
        IFolder oldSupplFolder = getTraceSupplementaryFolder(getSupplementaryFolderPath());

        // copy supplementary folder
        if (oldSupplFolder.exists()) {
            try {
                TraceUtils.createFolder((IFolder) destination.getParent(), new NullProgressMonitor());
                oldSupplFolder.copy(destination.getFullPath(), true, new NullProgressMonitor());
            } catch (CoreException e) {
                Activator.getDefault().logError("Error copying supplementary folder " + oldSupplFolder, e); //$NON-NLS-1$
            }
        }
    }

    /**
     * Refreshes the element specific supplementary folder information. It
     * creates the folder if not exists. It sets the persistence property of the
     * trace resource
     */
    public void refreshSupplementaryFolder() {
        IFolder supplFolder = createSupplementaryFolder();
        try {
            supplFolder.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        } catch (CoreException e) {
            Activator.getDefault().logError("Error refreshing supplementary folder " + supplFolder, e); //$NON-NLS-1$
        }
    }

    /**
     * Checks if supplementary resource exist or not.
     *
     * @return <code>true</code> if one or more files are under the element
     *         supplementary folder
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
        IFolder supplFolder = getTraceSupplementaryFolder(getSupplementaryFolderPath());
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
     * @param resources
     *            array of resources to delete.
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

    private IFolder createSupplementaryFolder() {
        IFolder supplFolder = prepareTraceSupplementaryFolder(getSupplementaryFolderPath(), true);

        try {
            getResource().setPersistentProperty(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER, supplFolder.getLocation().toOSString());
        } catch (CoreException e) {
            Activator.getDefault().logError("Error setting persistant property " + TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER, e); //$NON-NLS-1$
        }
        return supplFolder;
    }

    // -------------------------------------------------------
    // Signal handlers
    // -------------------------------------------------------

    /**
     * Handler for the Trace Opened signal
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        IResource resource = signal.getTrace().getResource();
        if ((resource == null) || !resource.equals(getResource())) {
            return;
        }

        getParent().refresh();
    }

}
