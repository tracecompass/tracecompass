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

import java.util.List;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.ITraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.LogLevelType;

/**
 * Interface for providing base information about UST events to be enabled.
 *
 * @author Bruno Roy
 */
public interface IBaseEnableUstEvents {

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return a flag whether events using log levels should be enabled
     */
    boolean isLogLevel();

    /**
     * @return a log level type (loglevel or loglevel-only)
     */
    LogLevelType getLogLevelType();

    /**
     * @return a log level
     */
    ITraceLogLevel getLogLevel();

    /**
     * @return a flag indicating whether all tracepoints shall be enabled or not.
     */
    boolean isAllTracePoints();

    /**
     * @return a list of logger names to be enabled.
     */
    List<String> getEventNames();
}
