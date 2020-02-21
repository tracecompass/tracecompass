/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfAnalysisElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Handler to programatically launch an analysis
 *
 * @author Matthew Khouzam
 */
public class OpenAnalysisHandler extends AbstractHandler {

    private TmfAnalysisElement fAnalysisElement;

    @Override
    public boolean isEnabled() {
        /* Check if we are closing down */
        final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return false;
        }

        /* Get the selection */
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

        /* Make sure there is only one selection and that it is an analysis output */
        fAnalysisElement = null;
        if (selection instanceof TreeSelection) {
            final TreeSelection sel = (TreeSelection) selection;
            // There should be only one item selected as per the plugin.xml
            final Object element = sel.getFirstElement();
            if (element instanceof TmfAnalysisElement) {
                fAnalysisElement = (TmfAnalysisElement) element;
            }
        }

        return (fAnalysisElement != null);
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        /* Check if we are closing down */
        final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }

        /* Check that the view is valid */
        if (fAnalysisElement == null) {
            return null;
        }

        IStatus analysisStatus = fAnalysisElement.scheduleAnalysis();
        if (!analysisStatus.isOK()) {
            Activator.getDefault().logInfo(analysisStatus.getMessage(), analysisStatus.getException());
        }

        return null;
    }

}
