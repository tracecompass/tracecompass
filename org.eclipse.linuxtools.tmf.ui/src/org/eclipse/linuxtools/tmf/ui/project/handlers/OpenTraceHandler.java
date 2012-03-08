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

package org.eclipse.linuxtools.tmf.ui.project.handlers;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.ui.editors.EventsViewEditor;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEditorInput;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

/**
 * <b><u>OpenTraceHandler</u></b>
 * <p>
 * TODO: Add support for multiple trace selection
 */
public class OpenTraceHandler extends AbstractHandler {

    private static final String BOOKMARKS_HIDDEN_FILE = ".bookmarks"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private TmfTraceElement fTrace = null;

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

        // Make sure there is only one selection and that it is a trace
        fTrace = null;
        if (selection instanceof TreeSelection) {
            TreeSelection sel = (TreeSelection) selection;
            // There should be only one item selected as per the plugin.xml
            Object element = sel.getFirstElement();
            if (element instanceof TmfTraceElement) {
                fTrace = (TmfTraceElement) element;
            }
        }

        // We only enable opening from the Traces folder for now
        return (fTrace != null);
    }

    // ------------------------------------------------------------------------
    // Execution
    // ------------------------------------------------------------------------

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object execute(ExecutionEvent event) throws ExecutionException {

        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null)
            return null;

        // Check that the trace is valid
        if (fTrace == null) {
            return null;
        }

        // If trace is under an experiment, use the original trace from the traces folder
        if (fTrace.getParent() instanceof TmfExperimentElement) {
            for (TmfTraceElement trace : fTrace.getProject().getTracesFolder().getTraces()) {
                if (trace.getName().equals(fTrace.getName())) {
                    fTrace = trace;
                    break;
                }
            }
        }

        ITmfTrace trace = fTrace.instantiateTrace();
        TmfEvent traceEvent = fTrace.instantiateEvent();
        if (trace == null || traceEvent == null) {
            displayErrorMsg(Messages.OpenTraceHandler_NoTraceType);
            return null;
        }
        trace.setResource(fTrace.getResource());

        // Get the editor_id from the extension point
        String editorId = fTrace.getEditorId();
        boolean usesEditor = editorId != null && editorId.length() > 0;

        try {
            trace.initTrace(fTrace.getName(), fTrace.getLocation().getPath(), traceEvent.getClass(), usesEditor);
        } catch (FileNotFoundException e) {
            displayErrorMsg(Messages.OpenTraceHandler_NoTrace);
            return null;
        }

        IResource resource = fTrace.getResource();
        IFile file = null;
        if (resource instanceof IFile) {
            file = (IFile) resource;
        } else if (resource instanceof IFolder){
            try {
                IFile bookmarksFile = fTrace.getProject().getTracesFolder().getResource().getFile(BOOKMARKS_HIDDEN_FILE);
                if (!bookmarksFile.exists()) {
                    InputStream source = new ByteArrayInputStream(new byte[0]);
                    bookmarksFile.create(source, true, null);
                }
                bookmarksFile.setHidden(true);

                IFolder folder = (IFolder) resource;
                file = folder.getFile(fTrace.getName() + '_');
                if (!file.exists()) {
                    file.createLink(bookmarksFile.getLocation(), IResource.REPLACE, null);
                }
                file.setHidden(true);
                if (usesEditor) {
                    file.setPersistentProperty(TmfTraceElement.TRACETYPE, fTrace.getTraceType());
                } else {
                    file.setPersistentProperty(TmfTraceElement.TRACETYPE, TmfTrace.class.getCanonicalName());
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }

        if (usesEditor) {
            if (trace instanceof TmfTrace) {
                ((TmfTrace) trace).setResource(file);
            }
            try {
                IEditorInput editorInput = new TmfEditorInput(file, trace);
                IWorkbench wb = PlatformUI.getWorkbench();
                IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();

                IEditorPart editor = activePage.findEditor(new FileEditorInput(file));
                if (editor != null && editor instanceof IReusableEditor) {
                    activePage.reuseEditor((IReusableEditor) editor, editorInput);
                    activePage.activate(editor);
                } else {
                    editor = activePage.openEditor(editorInput, editorId);
                    if (resource instanceof IFile) {
                        IDE.setDefaultEditor((IFile) resource, editorId);
                    }
                }
            } catch (PartInitException e) {
                e.printStackTrace();
            }
        } else {
            // Create the experiment
            ITmfTrace[] traces = new ITmfTrace[] { trace };
            TmfExperiment experiment = new TmfExperiment(traceEvent.getClass(), fTrace.getName(), traces, trace.getCacheSize());
            experiment.setBookmarksFile(file);
    
            TmfExperiment.setCurrentExperiment(experiment);
            TmfSignalManager.dispatchSignal(new TmfExperimentSelectedSignal(this, experiment));
            IDE.setDefaultEditor(file, EventsViewEditor.ID);
        }
        return null;
    }

    private void displayErrorMsg(String errorMsg) {
        MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        mb.setText(Messages.OpenTraceHandler_Title);
        mb.setMessage(errorMsg);
        mb.open();
    }

}
