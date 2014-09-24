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
import org.eclipse.linuxtools.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.linuxtools.tmf.core.filter.model.TmfFilterNode;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.widgets.TreeItem;

/**
 * DropTargetListener for filter view
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
class FilterDropTargetAdapter extends DropTargetAdapter {

    private FilterViewer fViewer;

    /**
     * Constructor
     * @param viewer the content of the FilterView
     */
    public FilterDropTargetAdapter(FilterViewer viewer) {
        super();
        this.fViewer = viewer;
    }

    /**
     * Returns <code>true</code> if droppedNode is an ancestor of node.
     *
     * @param droppedNode
     *            the ITmfFilterTreeNode to drop or paste
     * @param node
     *            the ITmfFilterTreeNode receiving a new child
     * @return <code>true</code> if droppedNode is and ancestor of node,
     *         <code>false</code> otherwise.
     */
    private static boolean isAncestor(ITmfFilterTreeNode droppedNode, ITmfFilterTreeNode node) {
        ITmfFilterTreeNode tmp = node;

        while (tmp != null) {
            ITmfFilterTreeNode n = tmp.getParent();
            if (n == droppedNode) {
                return true;
            }
            tmp = n;
        }
        return false;
    }

    @Override
    public void dropAccept(DropTargetEvent event) {
        ITmfFilterTreeNode treeNodeToDrop = null;
        if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
            treeNodeToDrop = FilterEditUtils.getTransferredTreeNode();
        }
        if (treeNodeToDrop == null) {
            // should never occur
            event.detail = DND.DROP_NONE;
            return;
        }
        if (event.item instanceof TreeItem) {
            Object data = event.item.getData();
            if (data instanceof ITmfFilterTreeNode) {
                ITmfFilterTreeNode node = (ITmfFilterTreeNode) data;
                if (node.getValidChildren().contains(treeNodeToDrop.getNodeName())) {
                    if (isAncestor(treeNodeToDrop, node) && event.detail != DND.DROP_COPY) {
                        // do nothing in this case
                        event.detail = DND.DROP_NONE;
                    }
                    return;
                }
            }
        } else { // accept only TmfFilterNode
            if (!TmfFilterNode.NODE_NAME.equals(treeNodeToDrop.getNodeName())) {
                event.detail = DND.DROP_NONE;
            }
            return;
        }
        event.detail = DND.DROP_NONE;
        return;
    }

    @Override
    public void drop(DropTargetEvent event) {
        ITmfFilterTreeNode treeNodeToDrop = FilterEditUtils.getTransferredTreeNode();
        if (event.item instanceof TreeItem) {
            Object data = event.item.getData();
            if (data instanceof ITmfFilterTreeNode) {
                ITmfFilterTreeNode node = (ITmfFilterTreeNode) data;
                if (node.getValidChildren().contains(treeNodeToDrop.getNodeName())) {
                    treeNodeToDrop = treeNodeToDrop.clone();
                    node.addChild(treeNodeToDrop);
                    fViewer.refresh();
                    fViewer.setSelection(treeNodeToDrop, true);
                    return;
                }
            }
        } else { // accept only TmfFilterNode
            if (TmfFilterNode.NODE_NAME.equals(treeNodeToDrop.getNodeName())) {
                ITmfFilterTreeNode root = fViewer.getInput();
                treeNodeToDrop = treeNodeToDrop.clone();
                root.addChild(treeNodeToDrop);
                fViewer.refresh();
                fViewer.setSelection(treeNodeToDrop, true);
                return;
            }
        }
    }

}
