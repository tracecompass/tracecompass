/*******************************************************************************
 * Copyright (c) 2011 Ericsson
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

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
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

    private static final String BUNDLE_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.project.trace.select_trace_type.bundle"; //$NON-NLS-1$
    private static final String TYPE_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.project.trace.select_trace_type.type"; //$NON-NLS-1$
    private static final String ICON_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.project.trace.select_trace_type.icon"; //$NON-NLS-1$

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
                    ok &= propagateProperties(trace, bundleName, traceType, iconUrl);
                } catch (CoreException e) {
                    Activator.getDefault().logError("Error selecting trace type for trace" + trace.getName(), e); //$NON-NLS-1$
                }
            }
        }
        ((ITmfProjectModelElement) fSelection.getFirstElement()).getProject().refresh();

        if (!ok) {
            MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR);
            mb.setText(Messages.SelectTraceTypeHandler_Title);
            mb.setMessage(Messages.SelectTraceTypeHandler_InvalidTraceType);
            mb.open();
        }

        return null;
    }

    private static boolean propagateProperties(TmfTraceElement trace,
            String bundleName, String traceType, String iconUrl)
            throws CoreException {

        IResource svResource = trace.getResource();
        String svBundleName = svResource.getPersistentProperty(TmfCommonConstants.TRACEBUNDLE);
        String svTraceType = svResource.getPersistentProperty(TmfCommonConstants.TRACETYPE);
        String svIconUrl = svResource.getPersistentProperty(TmfCommonConstants.TRACEICON);

        setProperties(trace.getResource(), bundleName, traceType, iconUrl);
        trace.refreshTraceType();
        if (!validateTraceType(trace)) {
            setProperties(trace.getResource(), svBundleName, svTraceType, svIconUrl);
            trace.refreshTraceType();
            return false;
        }

        trace.refreshTraceType();

        if (trace.getParent() instanceof TmfTraceFolder) {
            TmfExperimentFolder experimentFolder = trace.getProject().getExperimentsFolder();
            for (final ITmfProjectModelElement experiment : experimentFolder.getChildren()) {
                for (final ITmfProjectModelElement child : experiment.getChildren()) {
                    if (child instanceof TmfTraceElement) {
                        TmfTraceElement linkedTrace = (TmfTraceElement) child;
                        if (linkedTrace.equals(trace)) {
                            IResource resource = linkedTrace.getResource();
                            setProperties(resource, bundleName, traceType, iconUrl);
                            linkedTrace.refreshTraceType();
                        }
                    }
                }
            }
        }

        return true;
    }

    private static void setProperties(IResource resource, String bundleName,
            String traceType, String iconUrl) throws CoreException {
        resource.setPersistentProperty(TmfCommonConstants.TRACEBUNDLE, bundleName);
        resource.setPersistentProperty(TmfCommonConstants.TRACETYPE, traceType);
        resource.setPersistentProperty(TmfCommonConstants.TRACEICON, iconUrl);
    }

    private static boolean validateTraceType(TmfTraceElement trace) {
        IProject project = trace.getProject().getResource();
        ITmfTrace tmfTrace = null;
        try {
            tmfTrace = trace.instantiateTrace();
            return (tmfTrace != null && tmfTrace.validate(project, trace.getLocation().getPath()));
        } finally {
            if (tmfTrace != null) {
                tmfTrace.dispose();
            }
        }
    }

}
