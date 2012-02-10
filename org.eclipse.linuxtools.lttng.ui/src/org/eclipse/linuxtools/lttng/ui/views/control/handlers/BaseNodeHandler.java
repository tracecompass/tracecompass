/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.control.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.lttng.ui.views.control.ControlView;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TargetNodeComponent;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>BaseNodeHandler</u></b>
 * <p>
 * Command handler implementation to delete a target host.
 * </p>
 */
abstract public class BaseNodeHandler extends AbstractHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The target node component the command is to be executed on. 
     */
    protected TargetNodeComponent fTargetNode = null;
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    public TargetNodeComponent getTargetNode() {
        return fTargetNode;
    }
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        fTargetNode = null;

        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return false;
        }

        // Check if we are in the Project View
        IWorkbenchPage page = window.getActivePage();
        if (page == null) {
            return false;
        }

        IWorkbenchPart part = page.getActivePart();
        if (!(part instanceof ControlView)) {
            return false;
        }

        // Check if a project is selected
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {
            Object element = ((StructuredSelection) selection).getFirstElement();
            fTargetNode = (element instanceof TargetNodeComponent) ? (TargetNodeComponent) element : null;
        }
        return fTargetNode != null;
    }
}