/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TraceResource;
import org.eclipse.linuxtools.internal.lttng.ui.Activator;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.Messages;
import org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.dialogs.ImportTraceDialog;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.FileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

/**
 * <b><u>ImportToProject</u></b>
 * <p>
 * Action implementation to import a trace to a LTTng project.
 * </p>
 */
public class ImportToProject implements IObjectActionDelegate, IWorkbenchWindowActionDelegate, IViewActionDelegate {

	public static final String TRACE_FOLDER_NAME = "Traces"; //$NON-NLS-1$
	
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private List<TraceResource> fSelectedTraces;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public ImportToProject() {
        fSelectedTraces = new ArrayList<TraceResource>();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
     */
    @Override
    public void setActivePart(IAction arg0, IWorkbenchPart arg1) {
    }

    /**
     * Gets the first selected trace.
     * @return first selected trace
     */
    protected TraceResource getFirstSelectedTrace() {
        if (fSelectedTraces.size() > 0) {
            return (TraceResource) fSelectedTraces.get(0);
        }
        return null;
    }

    /**
     * Gets the trace SubSystem for the selected trace.
     * 
     * @return trace SubSystem
     */
    protected ISubSystem getSubSystem() {
        return getFirstSelectedTrace().getSubSystem();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction arg0) {
        for (int i = 0; i < fSelectedTraces.size(); i++) {

            final TraceResource trace = (TraceResource) fSelectedTraces.get(i);
            ImportTraceDialog dialog = new ImportTraceDialog(getShell(), trace);
            int result = dialog.open();
            if (result == Dialog.OK) {
            	IProject project = dialog.getProject();
            	String traceName = dialog.getTraceName();
            	if (trace.getTraceConfig().isNetworkTrace()) {
            		if (dialog.getLinkOnly()) {
            			linkTrace(getShell(), trace, project, traceName);
            		} else {
            			copyTrace(trace, project, traceName);
            		}
            	} else {
            		downloadTrace(trace, project, traceName);
            	}
            }
        }
    }

    /*
     * method to download a trace from the remote system.
     */
    private void downloadTrace(TraceResource trace, IProject project, String traceName) {
		try {
			IHost host = trace.getSubSystem().getHost();
			ISubSystem[] sss = RSECorePlugin.getTheSystemRegistry().getSubsystems(host, IFileServiceSubSystem.class);
			if (sss.length == 0 || !(sss[0] instanceof FileServiceSubSystem)) {
				MessageDialog.openWarning(getShell(),
						Messages.ImportToProject_ImportFailed,
						Messages.ImportToProject_NoFileServiceSubsystem);
				return;
			}
			FileServiceSubSystem fsss = (FileServiceSubSystem) sss[0];
			
			IFolder traceFolder = project.getFolder(TRACE_FOLDER_NAME);
			if (!traceFolder.exists()) {
				MessageDialog.openWarning(getShell(),
						Messages.ImportToProject_ImportFailed,
						Messages.ImportToProject_NoProjectTraceFolder);
				return;
			}

			IRemoteFile remoteFolder = fsss.getRemoteFileObject(trace.getTraceConfig().getTracePath(), new NullProgressMonitor());
			if (remoteFolder == null || !remoteFolder.exists()) {
				MessageDialog.openWarning(getShell(),
						Messages.ImportToProject_ImportFailed,
						Messages.ImportToProject_NoRemoteTraceFolder);
				return;
			}
			
			IFolder folder = traceFolder.getFolder(traceName);
			if (folder.exists()) {
				MessageDialog.openWarning(getShell(),
						Messages.ImportToProject_ImportFailed,
						Messages.ImportToProject_AlreadyExists);
				return;
			} else {
				folder.create(true, true, null);
			}
			
			IRemoteFile[] sources = fsss.list(remoteFolder, IFileService.FILE_TYPE_FILES, new NullProgressMonitor());

			String[] destinations = new String[sources.length];
			String[] encodings = new String[sources.length];
			for (int i = 0; i < sources.length; i++) {
				destinations[i] = folder.getLocation().addTrailingSeparator().append(sources[i].getName()).toString();
				encodings[i] = null;
			}
			
			fsss.downloadMultiple(sources, destinations, encodings, new NullProgressMonitor());
			
		} catch (SystemMessageException e) {
			MessageDialog.openWarning(getShell(),
					Messages.ImportToProject_ImportFailed,
					e.getMessage());
		} catch (CoreException e) {
			MessageDialog.openWarning(getShell(),
					Messages.ImportToProject_ImportFailed,
					e.getMessage());
		}
	}

    /*
     * Method to copy a trace residing on the local host. 
     */
	private void copyTrace(TraceResource trace, IProject project, String traceName) {
		IFolder traceFolder = project.getFolder(TRACE_FOLDER_NAME);
		if (!traceFolder.exists()) {
			MessageDialog.openWarning(getShell(),
					Messages.ImportToProject_ImportFailed,
					Messages.ImportToProject_NoProjectTraceFolder);
			return;
		}
		
		IPath containerPath = traceFolder.getFullPath().addTrailingSeparator().append(traceName);
		
		File sourceFolder = new File(trace.getTraceConfig().getTracePath());
		
		IOverwriteQuery overriteImplementor = new IOverwriteQuery(){
			@Override
			public String queryOverwrite(String pathString) {
				MessageDialog.openWarning(getShell(),
						Messages.ImportToProject_ImportFailed,
						Messages.ImportToProject_AlreadyExists);
				return IOverwriteQuery.NO_ALL;
			}};
		
		ImportOperation operation = new ImportOperation(
				containerPath,
				sourceFolder,
				FileSystemStructureProvider.INSTANCE,
				overriteImplementor);
		operation.setCreateContainerStructure(false);
		
		try {
			operation.run(new NullProgressMonitor());
		} catch (InvocationTargetException e) {
			MessageDialog.openWarning(getShell(),
					Messages.ImportToProject_ImportFailed,
					e.getMessage());
		} catch (InterruptedException e) {
			MessageDialog.openWarning(getShell(),
					Messages.ImportToProject_ImportFailed,
					e.getMessage());
		}
	}

    /*
     * Method to create a symbolic link to a trace residing on the local host. 
     */
    public static void linkTrace(Shell shell, TraceResource trace, IProject project, String traceName) {
        IFolder traceFolder = project.getFolder(TRACE_FOLDER_NAME);
        if (!traceFolder.exists()) {
            MessageDialog.openWarning(shell,
                    Messages.ImportToProject_ImportFailed,
                    Messages.ImportToProject_NoProjectTraceFolder);
            return;
        }

        IFolder folder = traceFolder.getFolder(traceName);
        if (folder.exists()) {
            MessageDialog.openWarning(shell,
                    Messages.ImportToProject_ImportFailed,
                    Messages.ImportToProject_AlreadyExists);
            return;
        }

        File sourceFolder = new File(trace.getTraceConfig().getTracePath());

        try {
            folder.createLink(sourceFolder.toURI(), IResource.REPLACE, null);
            // Set the trace properties for this resource
            // FIXME: update from extension point properties
            folder.setPersistentProperty(TmfTraceElement.TRACEBUNDLE, Activator.PLUGIN_ID);
            folder.setPersistentProperty(TmfTraceElement.TRACETYPE, "org.eclipse.linuxtools.lttng.tracetype.kernel"); //$NON-NLS-1$
            folder.setPersistentProperty(TmfTraceElement.TRACEICON, "icons/obj16/tux2.png"); //$NON-NLS-1$
        } catch (CoreException e) {
            MessageDialog.openWarning(shell,
                    Messages.ImportToProject_ImportFailed,
                    e.getMessage());
        }
    }

	/*
     * (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            fSelectedTraces.clear();
            // store the selected targets to be used when running
            Iterator<IStructuredSelection> theSet = ((IStructuredSelection) selection).iterator();
            while (theSet.hasNext()) {
                Object obj = theSet.next();
                if (obj instanceof TraceResource) {
                    fSelectedTraces.add((TraceResource)obj);
                }
            }
        }
    }

    /**
     * Set selected traces
     * @param traces
     */
    public void setSelectedTraces(List<TraceResource> traces) {
        fSelectedTraces = traces; 
    }
    
    
    /**
     * Returns the active workbench shell of this plug-in.
     * 
     * @return active workbench shell.
     */
    protected Shell getShell() {
        return SystemBasePlugin.getActiveWorkbenchShell();
    }


    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
    public void init(IWorkbenchWindow window) {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    @Override
    public void dispose() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
    public void init(IViewPart view) {
    }
}
