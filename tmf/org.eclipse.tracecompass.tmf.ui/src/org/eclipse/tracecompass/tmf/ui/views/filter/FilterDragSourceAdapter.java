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

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;

/**
 * DragSourceListener for filter view
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
class FilterDragSourceAdapter extends DragSourceAdapter {

    private FilterViewer fViewer;

    /**
     * Constructor
     *
     * @param viewer
     *            the content of the FilterView
     */
    public FilterDragSourceAdapter(FilterViewer viewer) {
        super();
        this.fViewer = viewer;
    }

    @Override
    public void dragStart(DragSourceEvent event) {
        ISelection s = fViewer.getTreeViewer().getSelection();
        LocalSelectionTransfer.getTransfer().setSelection(s);
        LocalSelectionTransfer.getTransfer().setSelectionSetTime(event.time & 0xFFFFFFFFL);
    }

    @Override
    public void dragSetData(DragSourceEvent event) {
        event.data = LocalSelectionTransfer.getTransfer().getSelection();
    }

    @Override
    public void dragFinished(DragSourceEvent event) {
        if (event.detail == DND.DROP_MOVE) {
            IStructuredSelection selection = (IStructuredSelection) LocalSelectionTransfer.getTransfer().getSelection();
            for (Object data : selection.toList()) {
                if (data instanceof ITmfFilterTreeNode) {
                    ITmfFilterTreeNode e = (ITmfFilterTreeNode) data;
                    e.remove();
                    fViewer.refresh();
                }
            }
        }
        LocalSelectionTransfer.getTransfer().setSelection(null);
        LocalSelectionTransfer.getTransfer().setSelectionSetTime(0);
    }

}
