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
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.handlers;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEnablement;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceJulLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceDomainComponent;

/**
 * Command handler implementation to enable one or more loggers.
 *
 * @author Bruno Roy
 */
public class EnableLoggerHandler extends ChangeLoggerStateHandler {

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    protected TraceEnablement getNewState() {
        return TraceEnablement.ENABLED;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected void changeState(TraceDomainComponent domain, List<String> loggerNames, TraceJulLogLevel logLevel, LogLevelType logLevelType, IProgressMonitor monitor) throws ExecutionException {
        // This conditional statement makes it possible to enable the same logger multiple
        // times with different log levels (with the right-click enable, directly on the logger)
        if (logLevelType.equals(LogLevelType.LOGLEVEL_ALL)) {
            domain.enableEvents(loggerNames, null, null, monitor);
        } else {
            domain.enableLogLevel(loggerNames, logLevelType, logLevel, null, TraceDomainType.JUL, monitor);
        }
    }
}
