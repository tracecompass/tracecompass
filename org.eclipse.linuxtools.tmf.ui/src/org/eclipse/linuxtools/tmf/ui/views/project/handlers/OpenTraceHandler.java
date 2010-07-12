/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.project.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEditorInput;
import org.eclipse.linuxtools.tmf.ui.parsers.ParserProviderManager;
import org.eclipse.linuxtools.tmf.ui.views.project.ProjectView;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfExperimentNode;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfTraceNode;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>OpenTraceHandler</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class OpenTraceHandler extends AbstractHandler {

	private TmfTraceNode fTrace = null;

	// ------------------------------------------------------------------------
	// Validation
	// ------------------------------------------------------------------------

	public boolean isEnabled() {
	    
        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null)
            return false;

        // Check if a trace is selected
        IWorkbenchPage page = window.getActivePage();
        if (!(page.getActivePart() instanceof ProjectView))
            return false;

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

	public Object execute(ExecutionEvent event) throws ExecutionException {

        IResource resource = fTrace.getResource();
        if (fTrace.getParent() instanceof TmfExperimentNode) {
            resource = fTrace.getProject().getTracesFolder().getTraceForLocation(resource.getLocation()).getResource();
        }
        
        ITmfTrace trace = ParserProviderManager.getTrace(resource);
        if (trace == null) {
            return null;
        }
        
        try {
            IEditorInput editorInput = new TmfEditorInput(resource, trace);
            IWorkbench wb = PlatformUI.getWorkbench();
            IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();
  
            String editorId = ParserProviderManager.getEditorId(resource);
            IEditorPart editor = activePage.findEditor(editorInput);
            if (editor != null && editor instanceof IReusableEditor) {
                activePage.reuseEditor((IReusableEditor)editor, editorInput);
                activePage.activate(editor);
            } else {
                editor = activePage.openEditor(editorInput, editorId);
            }
            
        } catch (PartInitException e) {
            e.printStackTrace();
        }
        
//        ITmfTrace[] traces = new ITmfTrace[]{trace};
//        TmfExperiment<TmfEvent> experiment = new TmfExperiment<TmfEvent>(TmfEvent.class, resource.getName(), traces, trace.getCacheSize());
//        experiment.indexExperiment(false);
//        TmfSignalManager.dispatchSignal(new TmfExperimentSelectedSignal<TmfEvent>(this, experiment));
        
		return null;
	}

}
