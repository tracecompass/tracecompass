/*******************************************************************************
 * Copyright (c) 2011, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * <b><u>RefreshHandler</u></b>
 * <p>
 * TODO: Handle multiple selections
 */
public class RefreshHandler extends AbstractHandler {

    // ------------------------------------------------------------------------
    // Execution
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
        if (selection instanceof IStructuredSelection) {
            for (Object element : ((IStructuredSelection) selection).toList()) {
                if (element instanceof ITmfProjectModelElement) {
                    IResource resource = ((ITmfProjectModelElement) element).getResource();
                    if (resource != null) {
                        try {
                            resource.refreshLocal(IResource.DEPTH_INFINITE, null);
                        } catch (CoreException e) {
                            Activator.getDefault().logError("Error refreshing projects", e); //$NON-NLS-1$
                        }
                    }
                }
            }
        }
        return null;
    }

}
