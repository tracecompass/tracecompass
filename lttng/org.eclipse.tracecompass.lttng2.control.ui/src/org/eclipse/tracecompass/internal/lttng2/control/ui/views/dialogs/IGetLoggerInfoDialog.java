/**********************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bruno Roy - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.ITraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;

/**
 * Interface for a dialog box for collecting information about the loggers to enable.
 *
 * @author Bruno Roy
 */
public interface IGetLoggerInfoDialog  extends IBaseGetInfoDialog {

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return a log level type (loglevel or loglevel-only)
     */
    LogLevelType getLogLevelType();

    /**
     * @return a log level
     */
    ITraceLogLevel getLogLevel();

    /**
     * Sets the logger domain type
     * @param domain - the domain type ({@link TraceDomainType})
     */
    void setLoggerDomain(TraceDomainType domain);
}
