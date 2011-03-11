/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.project.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.parsers.ParserProviderManager;
import org.eclipse.linuxtools.tmf.ui.signal.TmfTraceParserUpdatedSignal;
import org.eclipse.linuxtools.tmf.ui.views.project.ProjectView;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfExperimentNode;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfTraceNode;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>OpenTraceHandler</u></b>
 * <p>
 */
public class SelectParserHandler extends AbstractHandler {

    private TmfTraceNode fTrace = null;
    private ProjectView fProjectView = null;

    // ------------------------------------------------------------------------
    // Validation
    // ------------------------------------------------------------------------

	@Override
    public boolean isEnabled() {
        
        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null)
            return false;

        // Check if a trace is selected
        IWorkbenchPage page = window.getActivePage();
        if (!(page.getActivePart() instanceof ProjectView))
            return false;
        fProjectView = (ProjectView) page.getActivePart();

        // Check if a trace is selected
        ISelection selection = page.getSelection(ProjectView.ID);
        if (selection instanceof StructuredSelection) {
            Object element = ((StructuredSelection) selection).getFirstElement();
            fTrace = (element instanceof TmfTraceNode) ? (TmfTraceNode) element : null;
        }

        return (fTrace != null);
    }

    // ------------------------------------------------------------------------
    // Execution
    // ------------------------------------------------------------------------

    @Override
	@SuppressWarnings("unchecked")
	public Object execute(ExecutionEvent event) throws ExecutionException {
        IResource resource = fTrace.getResource();
        if (fTrace.getParent() instanceof TmfExperimentNode) {
            resource = fTrace.getProject().getTracesFolder().getTraceForLocation(resource.getLocation()).getResource();
        }
        String parser = event.getParameter("org.eclipse.linuxtools.tmf.ui.commandparameter.project.trace.selectparser.parser"); //$NON-NLS-1$
        try {
            resource.setPersistentProperty(ParserProviderManager.PARSER_PROPERTY, parser);
            fProjectView.broadcast(new TmfTraceParserUpdatedSignal(fProjectView, resource));
            fTrace.getProject().refresh();
            
            TmfExperiment<TmfEvent> currentExperiment = (TmfExperiment<TmfEvent>) TmfExperiment.getCurrentExperiment();
            if (currentExperiment != null) {
                for (int i = 0; i < currentExperiment.getTraces().length; i++) {
                    ITmfTrace trace = currentExperiment.getTraces()[i];
                    if (resource.getLocation().toOSString().equals(trace.getPath())) {
                        ITmfTrace newTrace = ParserProviderManager.getTrace(resource);
                        if (newTrace != null) {
                            currentExperiment.getTraces()[i] = newTrace;
                            fProjectView.broadcast(new TmfExperimentSelectedSignal<TmfEvent>(new Object(), currentExperiment));
                        }
                    }
                }
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return null;
    }

}
