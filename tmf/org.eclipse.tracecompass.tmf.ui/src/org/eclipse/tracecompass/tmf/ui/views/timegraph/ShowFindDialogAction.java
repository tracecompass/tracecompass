/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.views.timegraph;

import org.eclipse.jface.action.Action;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView.FindTarget;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Action to show a time graph find dialog to search for a
 * {@link ITimeGraphEntry}
 *
 * @author Jean-Christian Kouame
 */
class ShowFindDialogAction extends Action {

    private static TimeGraphFindDialog fDialog;
    private FindTarget fFindTarget;

    /**
     * Constructor
     */
    public ShowFindDialogAction() {
        setText(Messages.ShowFindDialogAction_Search);
        setToolTipText(Messages.ShowFindDialogAction_ShowSearchDialog);
        setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_SEARCH));
    }

    @Override
    public void run() {
        FindTarget findTarget = fFindTarget;
        if (findTarget == null) {
            return;
        }
        checkShell(findTarget);
        if (fDialog == null) {
            fDialog = new TimeGraphFindDialog(findTarget.getShell());
        }
        if (fDialog != null) {
            fDialog.update(findTarget);
            fDialog.open();
        }
    }

    /**
     * Checks if the dialogs shell is the same as the given <code>shell</code>
     * and if not clears the stub and closes the dialog.
     *
     * @param target
     *            the target that owns the shell to check
     */
    public void checkShell(FindTarget target) {
        if (fDialog != null && !fDialog.isDialogParentShell(target.getShell())) {
            fDialog.close();
            fDialog = null;
        }
    }

    /**
     * Define what to do when a part is activated.
     *
     * @param part
     *            The activated workbenchPart
     */
    public synchronized void partActivated(IWorkbenchPart part) {
        FindTarget newTarget = null;
        if (part instanceof AbstractTimeGraphView) {
            newTarget = ((AbstractTimeGraphView) part).getFindTarget();
            if (newTarget != fFindTarget) {
                fFindTarget = newTarget;
            }
        }
        /*
         *  Update target in all for all parts. If it is null the dialog will
         *  disable the find button.
         */
        if (fDialog != null) {
            fDialog.updateTarget(newTarget, false);
        }
    }

}
