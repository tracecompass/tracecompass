/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Close editors to release resources
 *   Geneviève Bastien - Moved the delete code to element model's classes
 *   Marc-Andre Laperle - Merged DeleteTraceHandler and DeleteFolderHandler
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.project.operations.TmfWorkspaceModifyOperation;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTracesFolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * An handler for deletion of both traces and trace folders. It allows mixing
 * both types of elements.
 */
public class DeleteTraceFolderElementHandler extends AbstractHandler {

    private TreeSelection fSelection = null;

    private enum DeleteType {
        /**
         * Only trace folders are selected.
         */
        DELETE_TRACE_FOLDERS,
        /**
         * Only traces are selected.
         */
        DELETE_TRACES,
        /**
         * A mix of different elements are selected.
         */
        DELETE_GENERIC,
        /**
         * Only Traces (top trace folders) are selected.
         */
        CLEAR_TRACES_FOLDER,
        /**
         * Only Traces under experiments are selected.
         */
        REMOVE_TRACES_FROM_EXPERIMENT
    }

    // ------------------------------------------------------------------------
    // Validation
    // ------------------------------------------------------------------------

    @Override
    public boolean isEnabled() {

        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return false;
        }

        // Get the selection
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IWorkbenchPart part = page.getActivePart();
        if (part == null) {
            return false;
        }
        ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
        if (selectionProvider == null) {
            return false;
        }
        ISelection selection = selectionProvider.getSelection();

        // Make sure selection contains only traces and trace folders
        fSelection = null;
        if (selection instanceof TreeSelection) {
            fSelection = (TreeSelection) selection;
            Iterator<Object> iterator = fSelection.iterator();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (!(element instanceof TmfTraceElement) && !(element instanceof TmfTraceFolder)) {
                    return false;
                }
            }
        }

        // If we get here, either nothing is selected or everything is a trace or folder
        return !selection.isEmpty();
    }

    // ------------------------------------------------------------------------
    // Execution
    // ------------------------------------------------------------------------

    private static DeleteType getDeleteType(ISelection selection) {
        int numTracesFolder = 0;
        int numTraceFolder = 0;
        int numTraces = 0;
        int numTracesUnderExperiment = 0;

        @SuppressWarnings("rawtypes")
        Iterator iterator = ((IStructuredSelection) selection).iterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if ((next instanceof TmfTracesFolder)) {
                numTracesFolder++;
            } else if (next instanceof TmfTraceFolder) {
                numTraceFolder++;
            } else if (next instanceof TmfTraceElement) {
                TmfTraceElement traceElement = (TmfTraceElement) next;
                if (traceElement.getParent() instanceof TmfExperimentElement) {
                    numTracesUnderExperiment++;
                } else {
                    numTraces++;
                }
            }
        }

        int total = numTraceFolder + numTracesFolder + numTracesUnderExperiment + numTraces;

        if (numTracesFolder == total) {
            return DeleteType.CLEAR_TRACES_FOLDER;
        }

        if (numTraceFolder == total) {
            return DeleteType.DELETE_TRACE_FOLDERS;
        }

        if (numTraces == total) {
            return DeleteType.DELETE_TRACES;
        }

        if (numTracesUnderExperiment == total) {
            return DeleteType.REMOVE_TRACES_FROM_EXPERIMENT;
        }

        return DeleteType.DELETE_GENERIC;
    }

    private static String getTitle(final DeleteType deleteType) {
        switch (deleteType)
        {
        case DELETE_GENERIC:
        case DELETE_TRACES:
        case DELETE_TRACE_FOLDERS:
            return Messages.DeleteDialog_Title;
        case CLEAR_TRACES_FOLDER:
            return Messages.ClearDialog_Title;
        case REMOVE_TRACES_FROM_EXPERIMENT:
            return Messages.RemoveDialog_Title;
        default:
            throw new IllegalArgumentException();
        }
    }

    private static String getMessage(DeleteType deleteType) {
        switch (deleteType)
        {
        case DELETE_GENERIC:
            return Messages.DeleteTraceHandlerGeneric_Message;
        case DELETE_TRACES:
            return Messages.DeleteTraceHandler_Message;
        case CLEAR_TRACES_FOLDER:
            return Messages.DeleteFolderHandlerClear_Message;
        case DELETE_TRACE_FOLDERS:
            return Messages.DeleteFolderHandler_Message;
        case REMOVE_TRACES_FROM_EXPERIMENT:
            return Messages.RemoveTraceFromExperimentHandler_Message;
        default:
            throw new IllegalArgumentException();
        }
    }

    private static String getTraceErrorMessage(DeleteType deleteType) {
        return deleteType == DeleteType.REMOVE_TRACES_FROM_EXPERIMENT ? Messages.RemoveTraceFromExperimentHandler_Error : Messages.DeleteFolderHandler_Error;
    }

    private static String getFolderErrorMessage(DeleteType deleteType) {
        return deleteType == DeleteType.CLEAR_TRACES_FOLDER ? Messages.DeleteFolderHandlerClear_Error : Messages.DeleteFolderHandler_Error;
    }

    private static String getTraceTaskName(DeleteType deleteType) {
        return deleteType == DeleteType.REMOVE_TRACES_FROM_EXPERIMENT ? Messages.RemoveTraceFromExperimentHandler_TaskName : Messages.DeleteFolderHandler_TaskName;
    }

    private static String getTraceFolderTaskName(DeleteType deleteType) {
        return deleteType == DeleteType.CLEAR_TRACES_FOLDER ? Messages.DeleteFolderHandlerClear_TaskName : Messages.DeleteFolderHandler_TaskName;
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }

        // Get the selection
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (!(selection instanceof IStructuredSelection)) {
            return null;
        }
        final DeleteType deleteType = getDeleteType(selection);

        // Confirm the operation
        Shell shell = window.getShell();
        MessageBox confirmOperation = new MessageBox(shell, SWT.ICON_QUESTION | SWT.CANCEL | SWT.OK);
        confirmOperation.setText(getTitle(deleteType));
        confirmOperation.setMessage(getMessage(deleteType));
        if (confirmOperation.open() != SWT.OK) {
            return null;
        }

        final Iterator<Object> iterator = fSelection.iterator();
        final int nbElements = fSelection.size();

        TmfWorkspaceModifyOperation operation = new TmfWorkspaceModifyOperation() {
            @Override
            public void execute(IProgressMonitor monitor) throws CoreException {
                SubMonitor subMonitor = SubMonitor.convert(monitor, nbElements);

                while (iterator.hasNext()) {
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                    Object element = iterator.next();
                    SubProgressMonitor elementSubMonitor = new SubProgressMonitor(subMonitor, 1);
                    if (element instanceof TmfTraceElement) {
                        final TmfTraceElement trace = (TmfTraceElement) element;
                        if (!trace.getResource().exists()) {
                            continue;
                        }
                        subMonitor.setTaskName(getTraceTaskName(deleteType) + " " + trace.getElementPath()); //$NON-NLS-1$
                        try {
                            SubMonitor deleteSubMonitor = SubMonitor.convert(elementSubMonitor, 1);
                            trace.delete(deleteSubMonitor);
                        } catch (final CoreException e) {
                            Display.getDefault().asyncExec(new Runnable() {
                                @Override
                                public void run() {
                                    final MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
                                    mb.setText(getTraceErrorMessage(deleteType) + ' ' + trace.getName());
                                    mb.setMessage(e.getMessage());
                                    mb.open();
                                }
                            });
                            Activator.getDefault().logError(getTraceErrorMessage(deleteType) + trace.getName(), e);
                        }
                    } else if (element instanceof TmfTraceFolder) {
                        final TmfTraceFolder folder = (TmfTraceFolder) element;
                        final IResource resource = folder.getResource();
                        if (!resource.exists()) {
                            continue;
                        }

                        subMonitor.setTaskName(getTraceFolderTaskName(deleteType) + " " + folder.getPath()); //$NON-NLS-1$

                        try {
                            // delete all traces under this folder
                            SubMonitor childrenSubMonitor = SubMonitor.convert(elementSubMonitor, folder.getTraces().size() + 1);
                            for (TmfTraceElement traceElement : folder.getTraces()) {
                                SubProgressMonitor deleteSubMonitor = new SubProgressMonitor(childrenSubMonitor, 1);
                                traceElement.delete(deleteSubMonitor);
                            }

                            // Finally, delete the folder. For the Traces
                            // folder, we only delete the children since the
                            // folder should always be there.
                            final SubProgressMonitor deleteSubMonitor = new SubProgressMonitor(subMonitor, 1);
                            if (folder instanceof TmfTracesFolder) {
                                resource.accept(new IResourceVisitor() {
                                    @Override
                                    public boolean visit(IResource visitedResource) throws CoreException {
                                        if (visitedResource != resource) {
                                            visitedResource.delete(true, deleteSubMonitor);
                                        }
                                        return true;
                                    }
                                }, IResource.DEPTH_ONE, 0);
                            } else {
                                resource.delete(true, deleteSubMonitor);
                            }
                            childrenSubMonitor.done();
                        } catch (final CoreException e) {
                            Display.getDefault().asyncExec(new Runnable() {
                                @Override
                                public void run() {
                                    final MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
                                    mb.setText(getFolderErrorMessage(deleteType) + ' ' + folder.getName());
                                    mb.setMessage(e.getMessage());
                                    mb.open();
                                }
                            });
                            Activator.getDefault().logError(getFolderErrorMessage(deleteType) + folder.getName(), e);
                        }
                    }
                    subMonitor.setTaskName(""); //$NON-NLS-1$
                    elementSubMonitor.done();
                }
           }
        };

        try {
            PlatformUI.getWorkbench().getProgressService().run(true, true, operation);
        } catch (InterruptedException e) {
            return null;
        } catch (InvocationTargetException e) {
            MessageDialog.openError(window.getShell(), e.toString(), e.getTargetException().toString());
            return null;
        }
        return null;
    }

}