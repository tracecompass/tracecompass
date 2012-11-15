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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEditorInput;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

/**
 * <b><u>OpenExperimentHandler</u></b>
 * <p>
 */
public class OpenExperimentHandler extends AbstractHandler {

    private static final String BOOKMARKS_HIDDEN_FILE = ".bookmarks"; //$NON-NLS-1$

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
        return (fExperiment != null);
    }

    // ------------------------------------------------------------------------
    // Execution
    // ------------------------------------------------------------------------

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {

        // Check if we are closing down
        final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return false;
        }

        try {
            final IFile bookmarksFile = fExperiment.getProject().getExperimentsFolder().getResource().getFile(BOOKMARKS_HIDDEN_FILE);
            if (!bookmarksFile.exists()) {
                final InputStream source = new ByteArrayInputStream(new byte[0]);
                bookmarksFile.create(source, true, null);
            }
            bookmarksFile.setHidden(true);

            final IFile file = fExperiment.getResource().getFile(fExperiment.getName() + '_');
            if (!file.exists()) {
                file.createLink(bookmarksFile.getLocation(), IResource.REPLACE, null);
            }
            file.setHidden(true);
            file.setPersistentProperty(TmfCommonConstants.TRACETYPE, TmfExperiment.class.getCanonicalName());

            // Instantiate the experiment's traces
            final List<TmfTraceElement> traceEntries = fExperiment.getTraces();
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
                    displayErrorMsg(Messages.OpenExperimentHandler_NoTraceType);
                    for (int j = 0; j < i; j++) {
                        traces[j].dispose();
                    }
                    return null;
                }
                try {
                    trace.initTrace(element.getResource(), element.getLocation().getPath(), traceEvent.getClass());
                } catch (final TmfTraceException e) {
                    displayErrorMsg(""); //$NON-NLS-1$
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
            TmfExperiment experiment = new TmfExperiment(ITmfEvent.class, fExperiment.getName(), traces, cacheSize);
            experiment.setBookmarksFile(file);

            final IEditorInput editorInput = new TmfEditorInput(file, experiment);
            final IWorkbench wb = PlatformUI.getWorkbench();
            final IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();

            final IEditorPart editor = activePage.findEditor(new FileEditorInput(file));
            if ((editor != null) && (editor instanceof IReusableEditor)) {
                activePage.reuseEditor((IReusableEditor) editor, editorInput);
                activePage.activate(editor);
            } else {
                activePage.openEditor(editorInput, commonEditorId);
            }
            IDE.setDefaultEditor(file, commonEditorId);
            // editor should dispose the experiment on close
        } catch (final CoreException e) {
            displayErrorMsg(e.getMessage());
        }

        return null;
    }

    private static void displayErrorMsg(final String errorMsg) {
        final MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        mb.setText(Messages.OpenExperimentHandler_Title);
        mb.setMessage(errorMsg);
        mb.open();
    }

}
