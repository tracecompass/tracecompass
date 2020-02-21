/**********************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.handlers;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.ui.IWorkbenchPage;

/**
 * <p>
 * Command handler implementation to delete a target host.
 * </p>
 *
 * @author Bernd Hufmann
 */
public abstract class BaseNodeHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The target node component the command is to be executed on.
     */
    @Nullable protected TargetNodeComponent fTargetNode = null;

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
            fTargetNode = null;
            if (isEnabled) {
                fTargetNode = node;
            }
        } finally {
            fLock.unlock();
        }
        return isEnabled;
    }


}