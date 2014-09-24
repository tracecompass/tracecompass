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

/**
 * Utilities for cut/copy/paste/dnd in filter view
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
class FilterEditUtils {

    /**
     * Gets the ITmfFilterTreeNode in LocalSelectionTransfer, if any
     * @return a ITmfFilterTreeNode or <code>null</code>
     */
    public static ITmfFilterTreeNode getTransferredTreeNode() {
        ITmfFilterTreeNode treeNodeToDrop = null;
        ISelection sel = LocalSelectionTransfer.getTransfer().getSelection();
        if (sel instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) sel;
            for (Object data : selection.toList()) {
                if (!(data instanceof ITmfFilterTreeNode)) {
                    return null;
                } else if (treeNodeToDrop != null) {
                    // should never occur, since tree has SWT.SINGLE style
                    return null;
                } else {
                    treeNodeToDrop = (ITmfFilterTreeNode) data;
                }
            }
        }
        return treeNodeToDrop;
    }
}
