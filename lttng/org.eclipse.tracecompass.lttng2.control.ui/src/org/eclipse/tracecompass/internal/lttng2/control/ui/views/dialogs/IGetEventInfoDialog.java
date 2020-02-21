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
 *   Bernd Hufmann - Updated for support of LTTng Tools 2.1
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceChannelComponent;

/**
 * <p>
 * Interface for a dialog box for collecting information about the events to enable.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface IGetEventInfoDialog extends IBaseGetInfoDialog {

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the channel the events shall be enabled. Null for default channel.
     */
    TraceChannelComponent getChannel();

    /**
     * Sets the domain type.
     * @param domain domain type ({@link TraceDomainType})
     */
    void setDomain(TraceDomainType domain);

    /**
     * Returns the filter expression.
     * @return the filter expression or null for no filtering
     */
    String getFilterExpression();
}
