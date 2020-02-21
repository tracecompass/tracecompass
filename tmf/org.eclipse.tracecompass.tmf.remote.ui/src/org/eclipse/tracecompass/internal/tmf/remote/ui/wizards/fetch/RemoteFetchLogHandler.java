/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;


/**
 * Command handler for opening the remote fetch wizard.
 *
 * @author Bernd Hufmann
 *
 */
public class RemoteFetchLogHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        RemoteFetchLogWizard w = new RemoteFetchLogWizard();
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        if (window == null) {
            return false;
        }

        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
        IStructuredSelection sec = StructuredSelection.EMPTY;
        if (currentSelection instanceof IStructuredSelection) {
            sec = (IStructuredSelection) currentSelection;
        }

        w.init(PlatformUI.getWorkbench(), sec);
        WizardDialog dialog = new WizardDialog(window.getShell(), w);
        dialog.open();
        return null;
    }
}
