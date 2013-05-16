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

import java.util.List;

import org.eclipse.linuxtools.internal.lttng2.core.control.model.LogLevelType;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceLogLevel;

/**
 * <p>
 * Interface for providing information about UST events to be enabled.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface IEnableUstEvents {

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return a flag whether the tracepoints shall be configured.
     */
    boolean isTracepoints();

    /**
     * @return a flag indicating whether all tracepoints shall be enabled or not.
     */
    boolean isAllTracePoints();

    /**
     * @return a list of event names to be enabled.
     */
    List<String> getEventNames();

    /**
     * @return a flag whether events using wildcards should be enabled
     */
    boolean isWildcard();

    /**
     * @return a wildcard
     */
    String getWildcard();

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
    TraceLogLevel getLogLevel();

    /**
     * @return a event name for the log level enable action
     */
    String getLogLevelEventName();

    /**
     * @return a filter expression
     */
    String getFilterExpression();

}
