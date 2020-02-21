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
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;

/**
 * Utilities for cut/copy/paste/dnd in filter view
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
class FilterEditUtils {

    private FilterEditUtils() {
        // Do nothing, private constructor
    }

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
