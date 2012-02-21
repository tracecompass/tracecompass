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

import org.eclipse.linuxtools.lttng.ui.views.control.model.IChannelInfo;

/**
 * <b><u>ICreateChannelDialog</u></b>
 * <p>
 * Interface for the create channel dialog when domain is known.
 * </p>
 */
public interface ICreateChannelDialog {
    
    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * @return the configuration info for the new channel.
     */
    public IChannelInfo getChannelInfo();
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * @return the open return value
     */
    int open();
}
