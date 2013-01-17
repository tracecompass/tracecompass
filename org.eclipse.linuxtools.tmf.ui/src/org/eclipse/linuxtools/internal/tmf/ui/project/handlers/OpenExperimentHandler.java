/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
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

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEditorInput;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TraceUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

/**
 * <b><u>OpenExperimentHandler</u></b>
 * <p>
 */
public class OpenExperimentHandler extends AbstractHandler {

    private TmfExperimentElement fExperiment = null;

    // ------------------------------------------------------------------------
    // Validation
    // ------------------------------------------------------------------------

    @Override
    public boolean isEnabled() {

        // Check if we are closing down
        final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return false;
        }

        // Get the selection
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final IWorkbenchPart part = page.getActivePart();
        if (part == null) {
            return false;
        }
        final ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
        if (selectionProvider == null) {
            return false;
        }
        final ISelection selection = selectionProvider.getSelection();

        // Make sure there is only one selection and that it is an experiment
        fExperiment = null;
        if (selection instanceof TreeSelection) {
            final TreeSelection sel = (TreeSelection) selection;
            // There should be only one item selected as per the plugin.xml
            final Object element = sel.getFirstElement();
            if (element instanceof TmfExperimentElement) {
                fExperiment = (TmfExperimentElement) element;
            }
        }

        // We only enable opening from the Traces folder for now
        return ((fExperiment != null) && (fExperiment.getTraces().size() > 0));
    }

    // ------------------------------------------------------------------------
    // Execution
    // ------------------------------------------------------------------------

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {

        // Check if we are closing down
        final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return false;
        }

        final TmfExperimentElement experimentElement = fExperiment;

        Thread thread = new Thread() {
            @Override
            public void run() {

                final IFile file;
                try {
                    file = experimentElement.createBookmarksFile();
                } catch (final CoreException e) {
                    Activator.getDefault().logError("Error opening experiment " + experimentElement.getName(), e); //$NON-NLS-1$
                    TraceUtils.displayErrorMsg(Messages.OpenExperimentHandler_Title, Messages.OpenExperimentHandler_Error + "\n\n" + e.getMessage()); //$NON-NLS-1$
                    return;
                }

                /* FIXME Unlike traces, there is no instanceExperiment, so we call this function
                 * here alone.  Maybe it would be better to do this on experiment's element
                 * constructor?
                 */
                experimentElement.refreshSupplementaryFolder();

                // Instantiate the experiment's traces
                final List<TmfTraceElement> traceEntries = experimentElement.getTraces();
                experimentElement.refreshSupplementaryFolder();
                final int nbTraces = traceEntries.size();
                int cacheSize = Integer.MAX_VALUE;
                String commonEditorId = null;
                final ITmfTrace[] traces = new ITmfTrace[nbTraces];
                for (int i = 0; i < nbTraces; i++) {
                    TmfTraceElement element = traceEntries.get(i);

                    // Since trace is under an experiment, use the original trace from the traces folder
                    element = element.getElementUnderTraceFolder();

                    final ITmfTrace trace = element.instantiateTrace();
                    final ITmfEvent traceEvent = element.instantiateEvent();
                    if ((trace == null) || (traceEvent == null)) {
                        TraceUtils.displayErrorMsg(Messages.OpenExperimentHandler_Title, Messages.OpenExperimentHandler_NoTraceType);
                        for (int j = 0; j < i; j++) {
                            traces[j].dispose();
                        }
                        if (trace != null) {
                            trace.dispose();
                        }
                        return;
                    }
                    try {
                        trace.initTrace(element.getResource(), element.getLocation().getPath(), traceEvent.getClass());
                    } catch (final TmfTraceException e) {
                        TraceUtils.displayErrorMsg(Messages.OpenExperimentHandler_Title, Messages.OpenTraceHandler_InitError + '\n'+'\n' + e);
                        for (int j = 0; j < i; j++) {
                            traces[j].dispose();
                        }
                        trace.dispose();
                        return;
                    }
                    cacheSize = Math.min(cacheSize, trace.getCacheSize());

                    // If all traces use the same editorId, use it, otherwise use the default
                    final String editorId = element.getEditorId();
                    if (commonEditorId == null) {
                        commonEditorId = (editorId != null) ? editorId : TmfEventsEditor.ID;
                    } else if (!commonEditorId.equals(editorId)) {
                        commonEditorId = TmfEventsEditor.ID;
                    }
                    traces[i] = trace;
                }

                // Create the experiment
                final TmfExperiment experiment = new TmfExperiment(ITmfEvent.class, experimentElement.getName(), traces, cacheSize, experimentElement.getResource());
                experiment.setBookmarksFile(file);

                final String editorId = commonEditorId;
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final IEditorInput editorInput = new TmfEditorInput(file, experiment);
                            final IWorkbench wb = PlatformUI.getWorkbench();
                            final IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();

                            final IEditorPart editor = activePage.findEditor(new FileEditorInput(file));
                            if ((editor != null) && (editor instanceof IReusableEditor)) {
                                activePage.reuseEditor((IReusableEditor) editor, editorInput);
                                activePage.activate(editor);
                            } else {
                                activePage.openEditor(editorInput, editorId);
                            }
                            IDE.setDefaultEditor(file, editorId);
                            // editor should dispose the experiment on close
                        } catch (final CoreException e) {
                            Activator.getDefault().logError("Error opening experiment " + experimentElement.getName(), e); //$NON-NLS-1$
                            TraceUtils.displayErrorMsg(Messages.OpenExperimentHandler_Title, Messages.OpenExperimentHandler_Error + "\n\n" + e.getMessage()); //$NON-NLS-1$
                            experiment.dispose();
                            return;
                        }
                    }
                });
            }
        };

        thread.start();

        return null;
    }

}
