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

import org.eclipse.linuxtools.internal.lttng2.core.control.model.IChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceDomainComponent;

/**
 * <p>
 * Interface for the enable channel dialog when domain is known.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface IEnableChannelDialog {

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

    /**
     * Sets the whether dialog is for Kernel or UST
     * @param isKernel true for kernel domain else UST
     */
    public void setHasKernel(boolean isKernel);

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * @return the open return value
     */
    int open();
}
