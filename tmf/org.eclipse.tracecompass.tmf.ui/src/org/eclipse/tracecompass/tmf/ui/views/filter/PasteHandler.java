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
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterNode;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Handler for paste command in filter view
 *
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
public class PasteHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }

        // Get the selection
        IWorkbenchPage page = window.getActivePage();
        IWorkbenchPart part = page.getActivePart();
        if (!(part instanceof FilterView)) {
            return null;
        }
        FilterView v = (FilterView) part;

        ITmfFilterTreeNode objectToPaste = FilterEditUtils.getTransferredTreeNode();
        objectToPaste = objectToPaste.clone();
        ITmfFilterTreeNode sel = v.getSelection();
        if (sel == null || TmfFilterNode.NODE_NAME.equals(objectToPaste.getNodeName())) {
            sel = v.getFilterRoot();
        }

        sel.addChild(objectToPaste);
        v.refresh();
        v.setSelection(objectToPaste);
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
        if (!(part instanceof FilterView)) {
            return false;
        }
        FilterView v = (FilterView) part;
        ITmfFilterTreeNode sel = v.getSelection();
        if (sel == null) {
            sel = v.getFilterRoot();
        }
        ITmfFilterTreeNode objectToPaste = FilterEditUtils.getTransferredTreeNode();
        return (v.isTreeInFocus() && objectToPaste != null &&
                (sel.getValidChildren().contains(objectToPaste.getNodeName())
                        || TmfFilterNode.NODE_NAME.equals(objectToPaste.getNodeName())));
    }

}
