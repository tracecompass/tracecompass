/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Fix propagation to experiment traces
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
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
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
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

    private static final String BUNDLE_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.select_trace_type.bundle"; //$NON-NLS-1$
    private static final String TYPE_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.select_trace_type.type"; //$NON-NLS-1$
    private static final String ICON_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.select_trace_type.icon"; //$NON-NLS-1$

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
        if (window == null) {
            return null;
        }
        List<IStatus> statuses = new ArrayList<>();
        boolean ok = true;
        for (Object element : fSelection.toList()) {
            TmfTraceElement trace = (TmfTraceElement) element;
            trace = trace.getElementUnderTraceFolder();
            IResource resource = trace.getResource();
            if (resource != null) {
                try {
                    // Set the properties for this resource
                    String bundleName = event.getParameter(BUNDLE_PARAMETER);
                    String traceType = event.getParameter(TYPE_PARAMETER);
                    String iconUrl = event.getParameter(ICON_PARAMETER);
                    String previousTraceType = trace.getTraceType();
                    IStatus status = propagateProperties(trace, bundleName, traceType, iconUrl);
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
                } catch (CoreException e) {
                    Activator.getDefault().logError(Messages.SelectTraceTypeHandler_ErrorSelectingTrace + trace.getName(), e);
                }
            }
        }
        ((ITmfProjectModelElement) fSelection.getFirstElement()).getProject().refresh();

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

    private static IStatus propagateProperties(TmfTraceElement trace,
            String bundleName, String traceType, String iconUrl)
            throws CoreException {

        IResource svResource = trace.getResource();
        String svBundleName = svResource.getPersistentProperty(TmfCommonConstants.TRACEBUNDLE);
        String svTraceType = svResource.getPersistentProperty(TmfCommonConstants.TRACETYPE);
        String svIconUrl = svResource.getPersistentProperty(TmfCommonConstants.TRACEICON);

        setProperties(trace.getResource(), bundleName, traceType, iconUrl);
        trace.refreshTraceType();
        final IStatus validateTraceType = validateTraceType(trace);
        if (!validateTraceType.isOK()) {
            setProperties(trace.getResource(), svBundleName, svTraceType, svIconUrl);
            trace.refreshTraceType();
            return validateTraceType;
        }

        trace.refreshTraceType();

        if (trace.getParent() instanceof TmfTraceFolder) {
            TmfExperimentFolder experimentFolder = trace.getProject().getExperimentsFolder();
            for (final ITmfProjectModelElement experiment : experimentFolder.getChildren()) {
                for (final ITmfProjectModelElement child : experiment.getChildren()) {
                    if (child instanceof TmfTraceElement) {
                        TmfTraceElement linkedTrace = (TmfTraceElement) child;
                        if (linkedTrace.getName().equals(trace.getName())) {
                            IResource resource = linkedTrace.getResource();
                            setProperties(resource, bundleName, traceType, iconUrl);
                            linkedTrace.refreshTraceType();
                        }
                    }
                }
            }
        }

        return Status.OK_STATUS;
    }

    private static void setProperties(IResource resource, String bundleName,
            String traceType, String iconUrl) throws CoreException {
        resource.setPersistentProperty(TmfCommonConstants.TRACEBUNDLE, bundleName);
        resource.setPersistentProperty(TmfCommonConstants.TRACETYPE, traceType);
        resource.setPersistentProperty(TmfCommonConstants.TRACEICON, iconUrl);
    }

    private static IStatus validateTraceType(TmfTraceElement trace) {
        IProject project = trace.getProject().getResource();
        ITmfTrace tmfTrace = null;
        IStatus validate = null;
        try {
            tmfTrace = trace.instantiateTrace();
            if (tmfTrace != null) {
                validate = tmfTrace.validate(project, trace.getLocation().getPath());
            }
            else{
                validate =  new Status(IStatus.ERROR, trace.getName(), "File does not exist : " + trace.getLocation().getPath()); //$NON-NLS-1$
            }
        } finally {
            if (tmfTrace != null) {
                tmfTrace.dispose();
            }
        }
        if (validate == null) {
            validate = new Status(IStatus.ERROR, "unknown", "unknown"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return validate;
    }

}
