/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfAnalysisElement;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Handler for when user wants to open the analysis help text
 *
 * @author Geneviève Bastien
 */
public class OpenAnalysisHelpHandler extends AbstractHandler {

    private TmfAnalysisElement fAnalysis;

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

        // Make sure there is only one selection and that it is a trace
        fAnalysis = null;
        if (selection instanceof TreeSelection) {
            final TreeSelection sel = (TreeSelection) selection;
            // There should be only one item selected as per the plugin.xml
            final Object element = sel.getFirstElement();
            if (element instanceof TmfAnalysisElement) {
                fAnalysis = (TmfAnalysisElement) element;
            }
        }

        return (fAnalysis != null);
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        // Check if we are closing down
        final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }

        // Check that the trace is valid
        if (fAnalysis == null) {
            return null;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                displayHelpMsg(fAnalysis.getHelpMessage());
            }
        };

        thread.start();

        return null;
    }

    private static void displayHelpMsg(final String errorMsg) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                /*
                 * TODO: A message box is not the best place to show help.
                 * Something should be done with the Eclipse help
                 */
                final MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
                mb.setText(Messages.AnalysisModule_Help);
                mb.setMessage(errorMsg);
                mb.open();
            }
        });
    }

}
