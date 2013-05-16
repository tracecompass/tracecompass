/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs;

import org.eclipse.swt.widgets.Shell;

/**
 * <p>
 * Interface for a confirmation dialog.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface IConfirmDialog {

    /**
     * Open a confirmation dialog
     *
     * @param parent
     *            The parent shell
     * @param title
     *            The title of the dialog window
     * @param message
     *            The message in the dialog window
     * @return If the user clicked OK (true) or Cancel (false)
     */
    boolean openConfirm(Shell parent, String title, String message);

}
