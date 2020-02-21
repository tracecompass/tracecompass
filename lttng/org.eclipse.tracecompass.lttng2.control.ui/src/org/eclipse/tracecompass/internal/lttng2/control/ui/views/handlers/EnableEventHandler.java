/**********************************************************************
 * Copyright (c) 2012, 2016 Ericsson
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
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.handlers;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEnablement;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEventType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceChannelComponent;

/**
 * <p>
 * Command handler implementation to enable one or more events session, domain and channel.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class EnableEventHandler extends ChangeEventStateHandler {

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
    protected void changeState(TraceChannelComponent channel, List<String> eventNames, TraceLogLevel logLevel, LogLevelType logLevelType, TraceEventType eventType, String probe, IProgressMonitor monitor) throws ExecutionException{
        if (logLevelType.equals(LogLevelType.LOGLEVEL_NONE) || logLevelType.equals(LogLevelType.LOGLEVEL_ALL)) {
            switch (eventType) {
            case FUNCTION:
                channel.enableProbe(String.join(",", eventNames), true, probe, monitor); //$NON-NLS-1$
                break;
            case PROBE:
                channel.enableProbe(String.join(",", eventNames), false, probe, monitor); //$NON-NLS-1$
                break;
            case SYSCALL:
                channel.enableSyscalls(eventNames, monitor);
                break;
            case TRACEPOINT:
                channel.enableEvents(eventNames, monitor);
                break;
            case UNKNOWN:
                break;
            default:
                break;
            }
        } else {
            channel.enableLogLevel(eventNames, logLevelType, logLevel, null, channel.getDomain(), monitor);
        }
    }
}
