/*******************************************************************************
 * Copyright (c) 2009, 2010, 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>DeleteTraceHandler</u></b>
 * <p>
 */
public class DeleteTraceHandler extends AbstractHandler {

    private TreeSelection fSelection = null;

    // ------------------------------------------------------------------------
    // Validation
    // ------------------------------------------------------------------------

    @Override
    public boolean isEnabled() {

        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null)
            return false;

        // Get the selection
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IWorkbenchPart part = page.getActivePart();
        ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
        if (selectionProvider == null)
            return false;
        ISelection selection = selectionProvider.getSelection();

        // Make sure selection contains only traces
        fSelection = null;
        if (selection instanceof TreeSelection) {
            fSelection = (TreeSelection) selection;
            @SuppressWarnings("unchecked")
            Iterator<Object> iterator = fSelection.iterator();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (!(element instanceof TmfTraceElement)) {
                    return false;
                }
            }
        }

        // If we get here, either nothing is selected or everything is a trace
        return !selection.isEmpty();
    }

    // ------------------------------------------------------------------------
    // Execution
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null)
            return null;

        // Confirm the operation
        Shell shell = window.getShell();
        MessageBox confirmOperation = new MessageBox(shell, SWT.ICON_QUESTION | SWT.CANCEL | SWT.OK);
        confirmOperation.setText(Messages.DeleteDialog_Title);
        confirmOperation.setMessage(Messages.DeleteTraceHandler_Message);
        if (confirmOperation.open() != SWT.OK)
            return null;

        @SuppressWarnings("unchecked")
        Iterator<Object> iterator = fSelection.iterator();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            if (element instanceof TmfTraceElement) {
                TmfTraceElement trace = (TmfTraceElement) element;
                IResource resource = trace.getResource();
                try {
                    IPath path = resource.getLocation();
                    if (path != null && (trace.getParent() instanceof TmfTraceFolder)) {
                        String location = path.toFile().getCanonicalPath();
                        TmfExperimentFolder experimentFolder = trace.getProject().getExperimentsFolder();

                        // Propagate the removal to traces
                        for (ITmfProjectModelElement experiment : experimentFolder.getChildren()) {
                            List<ITmfProjectModelElement> toRemove = new LinkedList<ITmfProjectModelElement>();
                            for (ITmfProjectModelElement child : experiment.getChildren()) {
                                if (child.getResource().getLocation().toString().equals(location)) {
                                    toRemove.add(child);
                                }
                            }
                            for (ITmfProjectModelElement child : toRemove) {
                                experiment.removeChild(child);
                                child.getResource().delete(true, null);
                            }
                        }
                    }

                    // Finally, delete the trace
                    resource.delete(true, new NullProgressMonitor());

                    // Delete supplementary files
                    trace.deleteSupplementaryFiles();

                    // Refresh the project
                    trace.getProject().refresh();

                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (CoreException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}
