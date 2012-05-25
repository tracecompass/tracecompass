/*******************************************************************************
* Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

/**
 * Drop adapter assistant for project explorer
 */
public class DropAdapterAssistant extends CommonDropAdapterAssistant {

    /**
     * Default constructor
     */
    public DropAdapterAssistant() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#isSupportedType(org.eclipse.swt.dnd.TransferData)
     */
    @Override
    public boolean isSupportedType(TransferData aTransferType) {
        return super.isSupportedType(aTransferType) || FileTransfer.getInstance().isSupportedType(aTransferType);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
     */
    @Override
    public IStatus validateDrop(Object target, int operation, TransferData transferType) {
        if (target instanceof TmfTraceFolder) {
            getCommonDropAdapter().overrideOperation(DND.DROP_COPY);
            return Status.OK_STATUS;
        }
        if (target instanceof TmfExperimentElement) {
            getCommonDropAdapter().overrideOperation(DND.DROP_LINK);
            return Status.OK_STATUS;
        }
        if (target instanceof TmfTraceElement) {
            ITmfProjectModelElement parent = ((TmfTraceElement) target).getParent();
            if (parent instanceof TmfTraceFolder) {
                getCommonDropAdapter().overrideOperation(DND.DROP_COPY);
                return Status.OK_STATUS;
            }
            if (parent instanceof TmfExperimentElement) {
                getCommonDropAdapter().overrideOperation(DND.DROP_LINK);
                return Status.OK_STATUS;
            }
        }
        if (target instanceof IProject) {
            getCommonDropAdapter().overrideOperation(DND.DROP_COPY);
            return Status.OK_STATUS;
        }
        return Status.CANCEL_STATUS;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#handleDrop(org.eclipse.ui.navigator.CommonDropAdapter, org.eclipse.swt.dnd.DropTargetEvent, java.lang.Object)
     */
    @Override
    public IStatus handleDrop(CommonDropAdapter aDropAdapter, DropTargetEvent aDropTargetEvent, Object aTarget) {
        boolean ok = false;

        // Use local variable to avoid parameter assignment
        Object targetToUse = aTarget;

        // If target is a trace, use its parent (either trace folder or experiment)
        if (targetToUse instanceof TmfTraceElement) {
            targetToUse = ((TmfTraceElement) targetToUse).getParent();
        }

        // If target is a project, use its trace folder
        if (targetToUse instanceof IProject) {
            TmfProjectElement projectElement = TmfProjectRegistry.getProject((IProject) targetToUse);
            if (projectElement != null) {
                targetToUse = projectElement.getTracesFolder();
            }
        }

        if (aDropTargetEvent.data instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) aDropTargetEvent.data;
            for (Object source : selection.toArray()) {
                if (source instanceof IResource) {
                    // If source resource is a trace, use the trace element
                    IResource sourceResource = (IResource) source;
                    TmfProjectElement projectElement = TmfProjectRegistry.getProject(sourceResource.getProject());
                    if (projectElement != null && projectElement.getTracesFolder() != null) {
                        for (TmfTraceElement trace : projectElement.getTracesFolder().getTraces()) {
                            if (trace.getResource().equals(sourceResource)) {
                                source = trace;
                                break;
                            }
                        }
                    }
                }
                if (source instanceof TmfTraceElement) {
                    TmfTraceElement sourceTrace = (TmfTraceElement) source;
                    // If source trace is under an experiment, use the original trace from the traces folder
                    if (sourceTrace.getParent() instanceof TmfExperimentElement) {
                        for (TmfTraceElement trace : sourceTrace.getProject().getTracesFolder().getTraces()) {
                            if (trace.getName().equals(sourceTrace.getName())) {
                                sourceTrace = trace;
                                break;
                            }
                        }
                    }
                    if (targetToUse instanceof TmfExperimentElement) {
                        TmfExperimentElement targetExperiment = (TmfExperimentElement) targetToUse;
                        ok |= drop(sourceTrace, targetExperiment);
                    } else if (targetToUse instanceof TmfTraceFolder) {
                        TmfTraceFolder traceFolder = (TmfTraceFolder) targetToUse;
                        ok |= drop(sourceTrace, traceFolder);
                    }
                } else if (source instanceof IResource) {
                    IResource sourceResource = (IResource) source;
                    if (sourceResource.getType() != IResource.FILE && sourceResource.getType() != IResource.FOLDER) {
                        continue;
                    }
                    if (targetToUse instanceof TmfExperimentElement) {
                        TmfExperimentElement targetExperiment = (TmfExperimentElement) targetToUse;
                        ok |= drop(sourceResource, targetExperiment);
                    } else if (targetToUse instanceof TmfTraceFolder) {
                        TmfTraceFolder traceFolder = (TmfTraceFolder) targetToUse;
                        ok |= drop(sourceResource, traceFolder);
                    }
                }
            }
        } else if (aDropTargetEvent.data instanceof String[]) {
            String[] sources = (String[]) aDropTargetEvent.data;
            for (String source : sources) {
                Path path = new Path(source);
                if (targetToUse instanceof TmfExperimentElement) {
                    TmfExperimentElement targetExperiment = (TmfExperimentElement) targetToUse;
                    ok |= drop(path, targetExperiment);
                } else if (targetToUse instanceof TmfTraceFolder) {
                    TmfTraceFolder traceFolder = (TmfTraceFolder) targetToUse;
                    ok |= drop(path, traceFolder);
                }
            }
        }
        return (ok ? Status.OK_STATUS : Status.CANCEL_STATUS);
    }

    
    /**
     * Drop a trace by copying a resource in a target experiment
     * 
     * @param sourceTrace the source trace element to copy
     * @param targetExperiment the target experiment
     * @return true if successful
     */
    private boolean drop(TmfTraceElement sourceTrace, TmfExperimentElement targetExperiment) {
        
        IResource sourceResource = sourceTrace.getResource();
        
        if (drop(sourceResource, targetExperiment)) {
            IFolder destinationSupplementaryFolder = targetExperiment.getTraceSupplementaryFolder(sourceResource.getName());
            if (!destinationSupplementaryFolder.exists()) {
                sourceTrace.copySupplementaryFolder(destinationSupplementaryFolder);
            }
            return true;
        }
        return false;
    }
    
    /**
     * Drop a trace by copying a resource in a target experiment
     * 
     * @param sourceResource the source resource
     * @param targetExperiment the target experiment
     * @return true if successful
     */
    private boolean drop(IResource sourceResource, TmfExperimentElement targetExperiment) {
        boolean doit = true;

        //Use local variable to avoid parameter assignment
        IResource sourceResourceToUse = sourceResource;

        TmfProjectElement projectElement = TmfProjectRegistry.getProject(sourceResourceToUse.getProject());
        for (TmfTraceElement trace : targetExperiment.getTraces()) {
            if (trace.getName().equals(sourceResourceToUse.getName())) {
                doit = false;
                break;
            }
        }
        if (doit && !targetExperiment.getProject().equals(projectElement)) {
            for (TmfTraceElement trace : targetExperiment.getProject().getTracesFolder().getTraces()) {
                if (trace.getName().equals(sourceResourceToUse.getName())) {
                    doit = false;
                    break;
                }
            }
            if (doit) {
                try {
                    IPath destination = targetExperiment.getProject().getTracesFolder().getResource().getFullPath().addTrailingSeparator().append(sourceResourceToUse.getName());
                    sourceResourceToUse.copy(destination, false, null);
                    cleanupBookmarks(destination);
                    // use the copied resource for the experiment
                    if (sourceResourceToUse.getType() == IResource.FILE) {
                        sourceResourceToUse = targetExperiment.getProject().getTracesFolder().getResource().getFile(sourceResourceToUse.getName());
                    } else if (sourceResourceToUse.getType() == IResource.FOLDER) {
                        sourceResourceToUse = targetExperiment.getProject().getTracesFolder().getResource().getFolder(sourceResourceToUse.getName());
                    }
                } catch (CoreException e) {
                    doit = false;
                    displayException(e);
                }
            }
        }
        if (doit) {
            if (sourceResourceToUse != null && sourceResourceToUse.exists()) {
                createLink(targetExperiment.getResource(), sourceResourceToUse);
                return true;
            }
        }
        return false;
    }

    /**
     * Drop a trace by copying it's a trace element in a trace folder
     * 
     * @param sourceTrace the source trace
     * @param traceFolder the target trace folder
     * @return true if successful
     */
    private boolean drop(TmfTraceElement sourceTrace, TmfTraceFolder traceFolder) {
        IResource sourceResource = sourceTrace.getResource();
        if (drop(sourceResource, traceFolder)) {
            IFolder destinationSupplementaryFolder = traceFolder.getTraceSupplementaryFolder(sourceResource.getName());
            sourceTrace.copySupplementaryFolder(destinationSupplementaryFolder);
            return true;
        }
        return false;
    }

    /**
     * Drop a trace by copying a resource in a trace folder
     * 
     * @param sourceResource the source resource
     * @param traceFolder the target trace folder
     * @return true if successful
     */
    private boolean drop(IResource sourceResource, TmfTraceFolder traceFolder) {
        boolean doit = true;

        for (TmfTraceElement trace : traceFolder.getTraces()) {
            if (trace.getName().equals(sourceResource.getName())) {
                doit = false;
                break;
            }
        }
        if (doit) {
            try {
                IPath destination = traceFolder.getResource().getFullPath().addTrailingSeparator().append(sourceResource.getName());
                sourceResource.copy(destination, false, null);

                cleanupBookmarks(destination);
                return true;
            } catch (CoreException e) {
                displayException(e);
            }
        }
        return false;
    }
    
    /**
     * Drop a trace by importing a path in a target experiment
     * 
     * @param path the source path
     * @param targetExperiment the target experiment
     * @return true if successful
     */
    private boolean drop(Path path, TmfExperimentElement targetExperiment) {
        boolean doit = true;
        
        // Use local variable to avoid parameter assignment
        Path pathToUse = path;

        for (TmfTraceElement trace : targetExperiment.getTraces()) {
            if (trace.getName().equals(pathToUse.lastSegment())) {
                doit = false;
                break;
            }
        }
        if (doit && !pathToUse.toString().startsWith(targetExperiment.getProject().getResource().getLocation().toString())) {
            for (TmfTraceElement trace : targetExperiment.getProject().getTracesFolder().getTraces()) {
                if (trace.getName().equals(pathToUse.lastSegment())) {
                    doit = false;
                    break;
                }
            }
            if (doit) {
                importTrace(targetExperiment.getProject().getTracesFolder().getResource(), pathToUse);
                // use the imported trace for the experiment
                pathToUse = new Path(targetExperiment.getProject().getTracesFolder().getResource().getFile(pathToUse.lastSegment()).getLocation().toString());
            }
        }
        if (doit) {
            IResource resource = null;
            File file = new File(pathToUse.toString());
            if (file.exists() && file.isFile()) {
                resource = targetExperiment.getProject().getTracesFolder().getResource().getFile(pathToUse.lastSegment());
            } else if (file.exists() && file.isDirectory()) {
                resource = targetExperiment.getProject().getTracesFolder().getResource().getFolder(pathToUse.lastSegment());
            }
            if (resource != null && resource.exists()) {
                createLink(targetExperiment.getResource(), resource);
                return true;
            }
        }
        return false;
    }

    /**
     * Drop a trace by importing a path in a trace folder
     * 
     * @param path the source path
     * @param traceFolder the target trace folder
     * @return true if successful
     */
    private boolean drop(Path path, TmfTraceFolder traceFolder) {
        boolean doit = true;
        for (TmfTraceElement trace : traceFolder.getTraces()) {
            if (trace.getName().equals(path.lastSegment())) {
                doit = false;
                break;
            }
        }
        if (doit) {
            importTrace(traceFolder.getResource(), path);
            return true;
        }
        return false;
    }

    /**
     * Import a trace to the trace folder
     * 
     * @param resource the trace folder resource
     * @param path the path to the trace to import
     */
    private void importTrace(IFolder resource, Path path) {
        IPath containerPath = resource.getFullPath();
        File file = new File(path.toString());
        File source = file.getParentFile();
        IOverwriteQuery overwriteImplementor = new IOverwriteQuery() {
            @Override
            public String queryOverwrite(String pathString) {
                return IOverwriteQuery.NO_ALL;
            }
        };
        List<File> filesToImport = new LinkedList<File>();
        filesToImport.add(file);
        ImportOperation operation = new ImportOperation(
                containerPath,
                source,
                FileSystemStructureProvider.INSTANCE,
                overwriteImplementor,
                filesToImport);
        operation.setCreateContainerStructure(false);
        try {
            operation.run(new NullProgressMonitor());
        } catch (InvocationTargetException e) {
            displayException(e);
        } catch (InterruptedException e) {
            displayException(e);
        }
    } 

    /**
     * Create a link to the actual trace and set the trace type
     * 
     * @param parentFolder the parent folder
     * @param resource the resource
     */
    private void createLink(IFolder parentFolder, IResource resource) {
        IPath location = resource.getLocation();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {
            Map<QualifiedName, String> properties = resource.getPersistentProperties();
            String bundleName = properties.get(TmfCommonConstants.TRACEBUNDLE);
            String traceType = properties.get(TmfCommonConstants.TRACETYPE);
            String iconUrl = properties.get(TmfCommonConstants.TRACEICON);
            String supplFolder = properties.get(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER);

            if (resource instanceof IFolder) {
                IFolder folder = parentFolder.getFolder(resource.getName());
                if (workspace.validateLinkLocation(folder, location).isOK()) {
                    folder.createLink(location, IResource.REPLACE, null);
                    setProperties(folder, bundleName, traceType, iconUrl, supplFolder);

                } else {
                    Activator.getDefault().logError("Invalid Trace Location"); //$NON-NLS-1$
                }
            } else {
                IFile file = parentFolder.getFile(resource.getName());
                
                if (workspace.validateLinkLocation(file, location).isOK()) {
                    file.createLink(location, IResource.REPLACE, null);
                    setProperties(file, bundleName, traceType, iconUrl, supplFolder);
                } else {
                    Activator.getDefault().logError("Invalid Trace Location"); //$NON-NLS-1$
                }
            }
        } catch (CoreException e) {
            displayException(e);
        }
    }

    /**
     * Cleanup bookmarks file in copied trace
     */
    private void cleanupBookmarks(IPath path) {
        IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
        if (folder.exists()) {
            try {
                for (IResource member : folder.members()) {
                    if (TmfTrace.class.getCanonicalName().equals(member.getPersistentProperty(TmfCommonConstants.TRACETYPE))) {
                        member.delete(true, null);
                    }
                }
            } catch (CoreException e) {
                displayException(e);
            }
        }
    }

    /**
     * Set the trace persistent properties
     * 
     * @param resource the trace resource
     * @param bundleName the bundle name
     * @param traceType the trace type
     * @param iconUrl the icon URL
     * @param supplFolder the directory of the directory for supplementary information or null to ignore the property 
     * @throws CoreException
     */
    private void setProperties(IResource resource, String bundleName, String traceType, String iconUrl, String supplFolder) throws CoreException {
        resource.setPersistentProperty(TmfCommonConstants.TRACEBUNDLE, bundleName);
        resource.setPersistentProperty(TmfCommonConstants.TRACETYPE, traceType);
        resource.setPersistentProperty(TmfCommonConstants.TRACEICON, iconUrl);
        resource.setPersistentProperty(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER, supplFolder);
    }

    /**
     * Display an exception in a message box
     * 
     * @param e the exception
     */
    private void displayException(Exception e) {
        MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        mb.setText(e.getClass().getName());
        mb.setMessage(e.getMessage());
        mb.open();
    }

}
