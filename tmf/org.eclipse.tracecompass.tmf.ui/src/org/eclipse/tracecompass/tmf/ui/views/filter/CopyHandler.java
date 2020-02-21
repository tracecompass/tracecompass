/*******************************************************************************
 * Copyright (c) 2013, 2014 Kalray
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Xavier Raynaud - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.filter;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Handler for copy command in filter view
 *
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
public class CopyHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }
        IWorkbenchPage page = window.getActivePage();
        FilterView part = (FilterView) page.getActivePart();
        ISelection selection = getSelection(part);

        LocalSelectionTransfer.getTransfer().setSelection(selection);
        LocalSelectionTransfer.getTransfer().setSelectionSetTime(System.currentTimeMillis());
        return null;
    }

    /**
     * Retrieve the current selection
     *
     * @param tcv
     *            the FilterView
     * @return the current selection in the FilterView
     */
    protected ISelection getSelection(FilterView tcv) {
        return tcv.getViewSite().getSelectionProvider().getSelection();
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
            // only enable if tree is in focus
            if (!selection.isEmpty() && tcv.isTreeInFocus()) {
                return true;
            }
        }
        return false;
    }
}
