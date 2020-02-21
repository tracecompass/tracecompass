/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
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
 *   Patrick Tasse - Remove enable check
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.wizards.CopyTraceDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * <b><u>CopyTraceHandler</u></b>
 * <p>
 */
public class CopyTraceHandler extends AbstractHandler {

    // ------------------------------------------------------------------------
    // Execution
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        // Get selection already validated by handler in plugin.xml
        ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
        if (!(selection instanceof IStructuredSelection)) {
            return null;
        }
        TmfTraceElement trace = (TmfTraceElement) ((IStructuredSelection) selection).getFirstElement();

        // Fire the Copy Trace dialog
        Shell shell = HandlerUtil.getActiveShellChecked(event);
        CopyTraceDialog dialog = new CopyTraceDialog(shell, trace);
        dialog.open();

        return null;
    }

}
