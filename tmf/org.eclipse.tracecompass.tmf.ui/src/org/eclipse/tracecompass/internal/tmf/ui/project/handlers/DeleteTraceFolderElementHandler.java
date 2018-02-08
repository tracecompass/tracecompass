/*******************************************************************************
 * Copyright (c) 2009, 2018 Ericsson, École Polytechnique de Montréal
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

package org.eclipse.tracecompass.internal.tmf.ui.project.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.tmf.core.io.ResourceUtil;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTracesFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * An handler for deletion of both traces and trace folders. It allows mixing
 * both types of elements.
 */
public class DeleteTraceFolderElementHandler extends AbstractHandler {

    private IStructuredSelection fSelection = null;

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
        CLEAR_TRACES_FOLDER
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
        if (selection instanceof IStructuredSelection) {
            fSelection = (IStructuredSelection) selection;
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

    private static DeleteType getDeleteType(IStructuredSelection selection) {
        DeleteType deleteType = DeleteType.DELETE_GENERIC;
        Iterator<Object> iterator = selection.iterator();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            if ((element instanceof TmfTracesFolder)) {
                if (deleteType == DeleteType.DELETE_GENERIC) {
                    deleteType = DeleteType.CLEAR_TRACES_FOLDER;
                } else if (deleteType != DeleteType.CLEAR_TRACES_FOLDER) {
                    return DeleteType.DELETE_GENERIC;
                }
            } else if (element instanceof TmfTraceFolder) {
                if (deleteType == DeleteType.DELETE_GENERIC) {
                    deleteType = DeleteType.DELETE_TRACE_FOLDERS;
                } else if (deleteType != DeleteType.DELETE_TRACE_FOLDERS) {
                    return DeleteType.DELETE_GENERIC;
                }
            } else if (element instanceof TmfTraceElement) {
                if (deleteType == DeleteType.DELETE_GENERIC) {
                    deleteType = DeleteType.DELETE_TRACES;
                } else if (deleteType != DeleteType.DELETE_TRACES) {
                    return DeleteType.DELETE_GENERIC;
                }
            }
        }
        return deleteType;
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
        default:
            throw new IllegalArgumentException();
        }
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
        fSelection = (IStructuredSelection) selection;
        final DeleteType deleteType = getDeleteType(fSelection);

        // Confirm the operation
        Shell shell = window.getShell();
        MessageDialog dialog = new MessageDialog(
                shell,
                getTitle(deleteType),
                null,
                getMessage(deleteType),
                MessageDialog.QUESTION,
                new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL },
                1);
        if (dialog.open() != 0) {
            return null;
        }

        IRunnableWithProgress operation = monitor -> {
            Multimap<TmfProjectElement, TmfTraceElement> tracesToDelete = LinkedHashMultimap.create();
            Multimap<IProject, IResource> resourcesToDelete = LinkedHashMultimap.create();
            Multimap<IProject, IFolder> foldersToCreate = LinkedHashMultimap.create();
            for (Object element : fSelection.toList()) {
                if (element instanceof TmfTraceElement) {
                    TmfTraceElement traceElement = ((TmfTraceElement) element).getElementUnderTraceFolder();
                    tracesToDelete.put(traceElement.getProject(), traceElement);
                    IResource resource = traceElement.getResource();
                    resourcesToDelete.put(resource.getProject(), resource);
                } else if (element instanceof TmfTracesFolder) {
                    /*
                     * It is much faster to delete the Traces and Experiments folders then recreate
                     * them than to delete each of their child members individually
                     */
                    TmfTracesFolder tracesFolder = (TmfTracesFolder) element;
                    tracesToDelete.putAll(tracesFolder.getProject(), tracesFolder.getTraces());
                    IFolder resource = tracesFolder.getResource();
                    IProject project = resource.getProject();
                    resourcesToDelete.put(project, resource);
                    foldersToCreate.put(project, resource);
                    TmfExperimentFolder experimentsFolder = tracesFolder.getProject().getExperimentsFolder();
                    if (experimentsFolder != null) {
                        resource = experimentsFolder.getResource();
                        resourcesToDelete.put(project, resource);
                        foldersToCreate.put(project, resource);
                    }
                } else if (element instanceof TmfTraceFolder) {
                    TmfTraceFolder traceFolder = (TmfTraceFolder) element;
                    tracesToDelete.putAll(traceFolder.getProject(), traceFolder.getTraces());
                    IFolder resource = traceFolder.getResource();
                    resourcesToDelete.put(resource.getProject(), resource);
                }
            }

            Iterator<IResource> iterator = resourcesToDelete.values().iterator();
            while (iterator.hasNext()) {
                IResource resource = iterator.next();
                for (IResource parent : resourcesToDelete.get(resource.getProject())) {
                    // remove resource if any of its parent folders is included
                    if (parent.getFullPath().isPrefixOf(resource.getFullPath()) && !parent.equals(resource)) {
                        iterator.remove();
                        break;
                    }
                }
            }

            // Close the traces to be deleted
            monitor.setTaskName(Messages.CloseTraces_TaskName);
            tracesToDelete.values().forEach(traceElement -> {
                Display.getDefault().syncExec(traceElement::closeEditors);
            });

            SubMonitor subMon = SubMonitor.convert(monitor, tracesToDelete.size() + resourcesToDelete.size() + foldersToCreate.size());
            subMon.setTaskName(Messages.DeleteTraceHandlerGeneric_TaskName);
            for (Entry<TmfProjectElement, Collection<TmfTraceElement>> entry : tracesToDelete.asMap().entrySet()) {
                IProject project = entry.getKey().getResource();
                try {
                    ResourcesPlugin.getWorkspace().run(mon -> {
                        for (IResource resource : resourcesToDelete.get(project)) {
                            ResourceUtil.deleteResource(resource, subMon.split(1));
                        }
                        for (TmfTraceElement traceElement : entry.getValue()) {
                            traceElement.delete(subMon.split(1), false, false);
                        }
                        for (IFolder folder : foldersToCreate.get(project)) {
                            TraceUtils.createFolder(folder, subMon.split(1));
                        }
                        subMon.subTask(""); //$NON-NLS-1$
                    }, project, IWorkspace.AVOID_UPDATE, null);
                } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                }
            }
        };

        try {
            PlatformUI.getWorkbench().getProgressService().run(true, true, operation);
        } catch (InterruptedException e) {
            return null;
        } catch (InvocationTargetException e) {
            TraceUtils.displayErrorMsg(Messages.DeleteTraceHandlerGeneric_Error, e.getTargetException().toString(), e.getTargetException());
            return null;
        }
        return null;
    }

}
