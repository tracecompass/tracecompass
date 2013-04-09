/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.ControlView;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TargetNodeComponent;
import org.eclipse.ui.IWorkbenchPage;

/**
 * <p>
 * Command handler implementation to delete a target host.
 * </p>
 *
 * @author Bernd Hufmann
 */
abstract public class BaseNodeHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The target node component the command is to be executed on.
     */
    protected TargetNodeComponent fTargetNode = null;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public boolean isEnabled() {

        // Get workbench page for the Control View
        IWorkbenchPage page = getWorkbenchPage();
        if (page == null) {
            return false;
        }

        TargetNodeComponent node = null;
        // Check if the node component is selected
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {
            Object element = ((StructuredSelection) selection).getFirstElement();
            node = (element instanceof TargetNodeComponent) ? (TargetNodeComponent) element : null;
        }
        boolean isEnabled = node != null;
        fLock.lock();
        try {
            if (isEnabled) {
                fTargetNode = node;
            }
        } finally {
            fLock.unlock();
        }
        return isEnabled;
    }


}