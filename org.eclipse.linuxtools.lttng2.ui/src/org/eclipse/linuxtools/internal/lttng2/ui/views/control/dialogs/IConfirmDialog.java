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
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs;

import org.eclipse.swt.widgets.Shell;

/**
 * <b><u>IConfirmDialog</u></b>
 * <p>
 * Interface for a confirmation dialog.
 * </p>
 */
public interface IConfirmDialog {
    
    public boolean openConfirm(Shell parent, String title, String message);
    
}
