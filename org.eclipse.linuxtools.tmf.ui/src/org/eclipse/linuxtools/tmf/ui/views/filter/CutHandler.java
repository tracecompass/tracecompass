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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.tmf.core.filter.model.ITmfFilterTreeNode;

/**
 * Handler for cut command in filter view
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 * @since 3.0
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
