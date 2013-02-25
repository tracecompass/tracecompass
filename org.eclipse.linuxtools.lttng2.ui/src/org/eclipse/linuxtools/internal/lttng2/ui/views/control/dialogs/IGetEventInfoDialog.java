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
 *   Bernd Hufmann - Updated for support of LTTng Tools 2.1
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs;

import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceChannelComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent;

/**
 * <p>
 * Interface for a dialog box for collecting information about the events to enable.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface IGetEventInfoDialog {

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * @return the session the events shall be enabled.
     */
    public TraceSessionComponent getSession();

    /**
     * @return the channel the events shall be enabled. Null for default channel.
     */
    public TraceChannelComponent getChannel();

    /**
     * Sets flag about domain.
     * @param isKernel - true for kernel, false for UST
     */
    public void setIsKernel(boolean isKernel);

    /**
     * Sets available session.
     * @param sessions - a array of available sessions.
     */
    public void setSessions(TraceSessionComponent[] sessions);

    /**
     * Returns the filter expression.
     * @return the filter expression or null for no filtering
     */
    public String getFilterExpression();

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * @return returns the open return value
     */
    int open();
}
