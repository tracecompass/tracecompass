/**********************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bruno Roy - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs;

import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;

/**
 * Interface for the base class to get information about the event(s) /
 * logger(s) to enable.
 *
 * @author Bruno Roy
 */
public interface IBaseGetInfoDialog {

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * @return the session the events shall be enabled.
     */
    TraceSessionComponent getSession();

    /**
     * Sets available session.
     *
     * @param sessions
     *            - a array of available sessions.
     */
    void setSessions(TraceSessionComponent[] sessions);

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * @return returns the open return value
     */
    int open();

}
