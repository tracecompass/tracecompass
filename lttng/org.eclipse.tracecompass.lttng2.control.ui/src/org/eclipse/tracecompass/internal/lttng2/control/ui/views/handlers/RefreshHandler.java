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

import java.util.Iterator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TargetNodeState;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceControlComponent;
import org.eclipse.ui.IWorkbenchPage;

/**
 * <p>
 * Command handler implementation to refresh node configuration.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class RefreshHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The node component reference.
     */
    @Nullable private TargetNodeComponent fNode;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        TargetNodeComponent node;
        fLock.lock();
        try {
            node = fNode;
        } finally {
            fLock.unlock();
        }
        if (node != null) {
            node.refresh();
        }
        return null;
    }

    @Override
    public boolean isEnabled() {

        // Get workbench page for the Control View
        IWorkbenchPage page = getWorkbenchPage();
        if (page == null) {
            return false;
        }

        TargetNodeComponent node = null;
        // Check if one or more session are selected
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {

            StructuredSelection structered = ((StructuredSelection) selection);
            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = iterator.next();
                if (element instanceof TraceControlComponent) {
                    TraceControlComponent component = (TraceControlComponent) element;
                    boolean isConnected = component.getTargetNodeState() == TargetNodeState.CONNECTED;
                    if (isConnected) {
                        while ((component != null) && component.getClass() != TargetNodeComponent.class) {
                            component = (TraceControlComponent) component.getParent();
                        }
                        if (component != null) {
                            node = (TargetNodeComponent) component;
                        }
                    }
                }
            }
        }

        boolean isEnabled = node != null;

        fLock.lock();
        try {
            fNode = null;
            if (isEnabled) {
                fNode = node;
            }
        } finally {
            fLock.unlock();
        }

        return isEnabled;
    }
}
