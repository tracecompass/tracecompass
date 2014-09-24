/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Fix propagation to experiment traces
 *   Geneviève Bastien - Add support of experiment types
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfCommonProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceTypeUIUtils;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>SetTraceTypeHandler</u></b>
 * <p>
 */
public class SelectTraceTypeHandler extends AbstractHandler {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String TYPE_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.select_trace_type.type"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private TreeSelection fSelection = null;

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

        // Make sure selection contains only traces
        fSelection = null;
        if (selection instanceof TreeSelection) {
            fSelection = (TreeSelection) selection;
            Iterator<Object> iterator = fSelection.iterator();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (!(element instanceof TmfCommonProjectElement)) {
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
        if (window == null) {
            return null;
        }
        List<IStatus> statuses = new ArrayList<>();
        Set<TmfProjectElement> projects = new HashSet<>();
        boolean ok = true;
        for (Object element : fSelection.toList()) {
            TmfCommonProjectElement trace = (TmfCommonProjectElement) element;
            if (trace instanceof TmfTraceElement) {
                trace = ((TmfTraceElement) trace).getElementUnderTraceFolder();
            }
            IResource resource = trace.getResource();
            if (resource != null) {
                try {
                    // Set the trace type for this resource
                    String traceType = event.getParameter(TYPE_PARAMETER);
                    String previousTraceType = trace.getTraceType();
                    IStatus status = propagateProperties(trace, traceType);
                    ok &= status.isOK();

                    if (status.isOK()) {
                        if ((previousTraceType != null) && (!traceType.equals(previousTraceType))) {
                            // Close the trace if open
                            trace.closeEditors();
                            // Delete all supplementary resources
                            trace.deleteSupplementaryResources();
                        }
                    } else {
                        statuses.add(status);
                    }
                    projects.add(trace.getProject());
                } catch (CoreException e) {
                    Activator.getDefault().logError(Messages.SelectTraceTypeHandler_ErrorSelectingTrace + trace.getName(), e);
                }
            }
            trace.getProject();
        }
        for (TmfProjectElement project : projects) {
            project.refresh();
        }

        if (!ok) {
            final Shell shell = window.getShell();
            MultiStatus info = new MultiStatus(Activator.PLUGIN_ID, 1, Messages.SelectTraceTypeHandler_TraceFailedValidation, null);
            if (statuses.size() > 1)
            {
                info = new MultiStatus(Activator.PLUGIN_ID, 1, Messages.SelectTraceTypeHandler_TracesFailedValidation, null);
            }
            for (IStatus status : statuses) {
                info.add(status);
            }
            ErrorDialog.openError(shell, Messages.SelectTraceTypeHandler_Title, Messages.SelectTraceTypeHandler_InvalidTraceType, info);
        }
        return null;
    }

    private static IStatus propagateProperties(TmfCommonProjectElement element, String traceType)
            throws CoreException {

        TraceTypeHelper traceTypeHelper = TmfTraceType.getTraceType(traceType);
        if (traceTypeHelper == null) {
            return Status.CANCEL_STATUS;
        }
        final IStatus validateTraceType = traceTypeHelper.validate(element.getResource().getLocation().toOSString());
        if (!validateTraceType.isOK()) {
            return validateTraceType;
        }

        IResource resource = element.getResource();
        TmfTraceTypeUIUtils.setTraceType(resource, traceTypeHelper);

        TmfExperimentFolder experimentFolder = element.getProject().getExperimentsFolder();
        for (final TmfExperimentElement experiment : experimentFolder.getExperiments()) {
            for (final TmfTraceElement child : experiment.getTraces()) {
                if (child.getName().equals(element.getName())) {
                    TmfTraceTypeUIUtils.setTraceType(child.getResource(), traceTypeHelper);
                    break;
                }
            }
        }

        return Status.OK_STATUS;
    }

}
