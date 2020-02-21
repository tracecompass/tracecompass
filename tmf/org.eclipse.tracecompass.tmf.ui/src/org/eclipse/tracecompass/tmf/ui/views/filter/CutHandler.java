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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;

/**
 * Handler for cut command in filter view
 *
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
public class CutHandler extends CopyHandler {

    @Override
    protected ISelection getSelection(FilterView tcv) {
        ISelection sel = super.getSelection(tcv);
        if (sel instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) sel;
            Object o = selection.getFirstElement();
            if (o instanceof ITmfFilterTreeNode) {
                ITmfFilterTreeNode node = (ITmfFilterTreeNode) o;
                node = node.remove();
                tcv.refresh();
                return new StructuredSelection(node);
            }
        }
        return sel;
    }

}
