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
package org.eclipse.linuxtools.lttng.ui.views.control.dialogs;


/**
 * <b><u>ICreateChannelOnSessionDialog</u></b>
 * <p>
 * Interface for the create channel dialog when domain is known, i.e. dialog 
 * was opened on session level.
 * </p>
 */
public interface ICreateChannelOnSessionDialog extends ICreateChannelDialog {

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * @return true for Kernel domain. False for UST.
     */
    public boolean isKernel();

}
