/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

/**
 * Utility class for common tmf.ui functionalities
 *
 * @since 2.1
 */
public class TraceUtils {

    /**
     * Displays an error message in a box
     *
     * @param boxTitle
     *            The message box title
     * @param errorMsg
     *            The error message to display
     */
    public static void displayErrorMsg(final String boxTitle, final String errorMsg) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                final MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
                mb.setText(boxTitle);
                mb.setMessage(errorMsg);
                mb.open();
            }
        });
    }
}
