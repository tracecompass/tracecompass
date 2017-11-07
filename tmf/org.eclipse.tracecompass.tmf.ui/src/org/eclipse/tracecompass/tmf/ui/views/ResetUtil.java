/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jface.action.Action;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;

/**
 * Reset action helper
 *
 * @author Matthew Khouzam
 * @since 3.2
 */
@NonNullByDefault
public final class ResetUtil {

    private ResetUtil() {
        // do nothing
    }

    /**
     * Create reset action to add to toolbar or use on doubleclick
     *
     * @param element
     *            View to reset
     * @return a reset time range action
     */
    public static Action createResetAction(ITimeReset element) {
        Action resetAction = new Action(Messages.TmfView_ResetScaleActionNameText, Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_HOME_MENU)) {
            @Override
            public void run() {
                element.resetStartFinishTime();
            }
        };
        resetAction.setToolTipText(Messages.TmfTimeGraphViewer_ResetScaleActionToolTipText);
        return resetAction;
    }

}
