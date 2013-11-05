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

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;

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
