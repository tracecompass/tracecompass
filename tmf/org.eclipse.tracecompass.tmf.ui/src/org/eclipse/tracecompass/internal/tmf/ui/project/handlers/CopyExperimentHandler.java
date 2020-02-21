/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
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
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.wizards.CopyExperimentDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * <b><u>CopyExperimentHandler</u></b>
 * <p>
 */
public class CopyExperimentHandler extends AbstractHandler {

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
        TmfExperimentElement experiment = (TmfExperimentElement) ((IStructuredSelection) selection).getFirstElement();

        // Fire the Copy Experiment dialog
        Shell shell = HandlerUtil.getActiveShellChecked(event);
        CopyExperimentDialog dialog = new CopyExperimentDialog(shell, experiment);
        dialog.open();

        return null;
    }

}
