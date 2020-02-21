/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IChannelInfo;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceDomainComponent;

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
    IChannelInfo getChannelInfo();

    /**
     * Sets the domain component
     * @param domain - the trace domain component
     */
    void setDomainComponent(TraceDomainComponent domain);

    /**
     * Set the targent node component
     * @param node - the node component
     */
    void setTargetNodeComponent(TargetNodeComponent node);

    /**
     * @return The domain type ({@link TraceDomainType})
     */
    TraceDomainType getDomain();

    /**
     * Sets the whether dialog is for Kernel or UST
     * @param isKernel true for kernel domain else UST
     */
    void setHasKernel(boolean isKernel);

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * @return the open return value
     */
    int open();
}
