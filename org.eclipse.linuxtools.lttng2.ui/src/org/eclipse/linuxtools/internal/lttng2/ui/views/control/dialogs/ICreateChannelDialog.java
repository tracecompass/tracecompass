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

import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceDomainComponent;

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
    
    /**
     * Sets the domain component
     * @param domain - the trace domain component
     */
    public void setDomainComponent(TraceDomainComponent domain);
    
    /**
     * @return true for Kernel domain. False for UST.
     */
    public boolean isKernel();

    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * @return the open return value
     */
    int open();
}
