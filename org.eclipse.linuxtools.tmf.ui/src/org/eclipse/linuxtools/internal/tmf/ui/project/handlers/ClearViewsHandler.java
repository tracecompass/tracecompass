/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.tmf.core.signal.TmfClearExperimentSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.ui.PlatformUI;

/**
 * Sends the clear signal to the TmfView:s
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class ClearViewsHandler extends AbstractHandler {

    // ------------------------------------------------------------------------
    // Execution
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        boolean clearViewConfirmed = MessageDialog.openConfirm(
                PlatformUI.getWorkbench().getDisplay().getActiveShell(),
                Messages.ClearViewsHandler_title, Messages.ClearViewsHandler_message);
        if (clearViewConfirmed) {
            TmfSignalManager.dispatchSignal(new TmfClearExperimentSignal(this));
        }
        return null;
    }

}
