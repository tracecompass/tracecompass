/*******************************************************************************
 * Copyright (c) 2013 Kalray
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.filter;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Handler for delete command in filter view
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 * @since 2.2
 */
public class DeleteHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }
        IWorkbenchPage page = window.getActivePage();
        FilterView part = (FilterView) page.getActivePart();
        ISelection sel = part.getViewSite().getSelectionProvider().getSelection();
        if (sel instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) sel;
            Object o = selection.getFirstElement();
            if (o instanceof ITmfFilterTreeNode) {
                ITmfFilterTreeNode node = (ITmfFilterTreeNode) o;
                node = node.remove();
                part.refresh();
            }
        }
        return null;
    }

    @Override
    public boolean isEnabled() {
        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return false;
        }

        // Get the selection
        IWorkbenchPage page = window.getActivePage();
        IWorkbenchPart part = page.getActivePart();
        if (part instanceof FilterView) {
            FilterView tcv = (FilterView) part;
            ISelection selection = tcv.getSite().getSelectionProvider().getSelection();
            if (!selection.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
