/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson, École Polytechnique de Montréal
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
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtTrace;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomXmlTrace;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Base class for tracing project elements: it implements the common behavior of
 * all project elements: supplementary files, analysis, types, etc.
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public abstract class TmfCommonProjectElement extends TmfProjectModelElement {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // This trace type ID as defined in plugin.xml
    private String fTraceTypeId = null;

    private static final String BOOKMARKS_HIDDEN_FILE = ".bookmarks"; //$NON-NLS-1$

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
        parent.addChild(this);
        refreshTraceType();
        TmfSignalManager.register(this);
    }

    // ------------------------------------------------------------------------
    // TmfProjectModelElement
    // ------------------------------------------------------------------------

    @Override
    void refreshChildren() {

        /* Refreshes the analysis under this trace */
        Map<String, TmfAnalysisElement> childrenMap = new HashMap<>();
        for (TmfAnalysisElement analysis : getAvailableAnalysis()) {
            childrenMap.put(analysis.getAnalysisId(), analysis);
        }

        TraceTypeHelper helper = TmfTraceType.getInstance().getTraceType(getTraceType());

        Class<? extends ITmfTrace> traceClass = null;

        if (helper == null && getTraceType() != null) {
            if (fTraceTypeId.startsWith(CustomTxtTrace.class.getCanonicalName())) {
                for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
                    if (fTraceTypeId.equals(CustomTxtTrace.class.getCanonicalName() + ":" + def.definitionName)) { //$NON-NLS-1$
                        traceClass = CustomTxtTrace.class;
                    }
                }
            }
            if (fTraceTypeId.startsWith(CustomXmlTrace.class.getCanonicalName())) {
                for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
                    if (fTraceTypeId.equals(CustomXmlTrace.class.getCanonicalName() + ":" + def.definitionName)) { //$NON-NLS-1$
                        traceClass = CustomTxtTrace.class;
                    }
                }
            }
        } else if (helper != null) {
            traceClass = helper.getTraceClass();
        }

        /* Remove all analysis and return */
        if (traceClass == null) {
            for (TmfAnalysisElement analysis : childrenMap.values()) {
                removeChild(analysis);
            }
            return;
        }

        /** Get the base path to put the resource to */
        IPath path = fResource.getFullPath();

        /* Add all new analysis modules or refresh outputs of existing ones */
        for (IAnalysisModuleHelper module : TmfAnalysisManager.getAnalysisModules(traceClass).values()) {

            /* If the analysis is not a child of the trace, create it */
            TmfAnalysisElement analysis = childrenMap.remove(module.getId());
            if (analysis == null) {
                /**
                 * No need for the resource to exist, nothing will be done with
                 * it
                 */
                IFolder newresource = ResourcesPlugin.getWorkspace().getRoot().getFolder(path.append(module.getId()));
                analysis = new TmfAnalysisElement(module.getName(), newresource, this, module.getId());
            }
            analysis.refreshChildren();
        }

        /* Remove analysis that are not children of this trace anymore */
        for (TmfAnalysisElement analysis : childrenMap.values()) {
            removeChild(analysis);
        }
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Returns the trace type ID.
     *
     * @return trace type ID.
     */
    public String getTraceType() {
        return fTraceTypeId;
    }

    /**
     * Refreshes the trace type filed by reading the trace type persistent
     * property of the resource referenece.
     */
    public void refreshTraceType() {
        try {
            fTraceTypeId = getResource().getPersistentProperty(TmfCommonConstants.TRACETYPE);
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
     * @param traceType
     *            The canonical name to set as tracetype
     * @return The bookmark file
     * @throws CoreException
     *             if the bookmarks file cannot be created
     */
    protected IFile createBookmarksFile(IFolder bookmarksFolder, String traceType) throws CoreException {
        IFile file = getBookmarksFile();
        if (!file.exists()) {
            final IFile bookmarksFile = bookmarksFolder.getFile(BOOKMARKS_HIDDEN_FILE);
            if (!bookmarksFile.exists()) {
                final InputStream source = new ByteArrayInputStream(new byte[0]);
                bookmarksFile.create(source, true, null);
            }
            bookmarksFile.setHidden(true);
            file.createLink(bookmarksFile.getLocation(), IResource.REPLACE, null);
            file.setHidden(true);
            file.setPersistentProperty(TmfCommonConstants.TRACETYPE, traceType);
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
        final IFolder folder = (IFolder) fResource;
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
            copySupplementaryFolder(newName);
        }
        /* Copy the trace */
        try {
            getResource().copy(newPath, IResource.FORCE | IResource.SHALLOW, null);

            /* Delete any bookmarks file found in copied trace folder */
            IFolder folder = ((IFolder) getParent().getResource()).getFolder(newName);
            if (folder.exists()) {
                for (IResource member : folder.members()) {
                    if (TmfTrace.class.getCanonicalName().equals(member.getPersistentProperty(TmfCommonConstants.TRACETYPE))) {
                        member.delete(true, null);
                    } else if (TmfExperiment.class.getCanonicalName().equals(member.getPersistentProperty(TmfCommonConstants.TRACETYPE))) {
                        member.delete(true, null);
                    }
                }
            }
            return folder;
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
        List<ITmfProjectModelElement> children = getChildren();
        List<TmfAnalysisElement> analysis = new ArrayList<>();
        for (ITmfProjectModelElement child : children) {
            if (child instanceof TmfAnalysisElement) {
                analysis.add((TmfAnalysisElement) child);
            }
        }
        return analysis;
    }

    // ------------------------------------------------------------------------
    // Supplementary files operations
    // ------------------------------------------------------------------------

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
     * Renames the element specific supplementary folder according to the new
     * element name.
     *
     * @param newName
     *            The new element name
     */
    public void renameSupplementaryFolder(String newName) {
        IFolder oldSupplFolder = getTraceSupplementaryFolder(getResourceName());
        IFolder newSupplFolder = getTraceSupplementaryFolder(newName + getSuffix());

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
     * @param newName
     *            The new element name
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
     * @param destination
     *            The destination folder to copy to.
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
     * Refreshes the element specific supplementary folder information. It
     * creates the folder if not exists. It sets the persistence property of the
     * trace resource
     */
    public void refreshSupplementaryFolder() {
        createSupplementaryDirectory();
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
