/**********************************************************************
 * Copyright (c) 2012, 2015 Ericsson
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceEventComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;

/**
 * Class containing parameter for a command execution.
 *
 * @author Bernd Hufmann
 */
@NonNullByDefault
public class EventCommandParameter extends CommandParameter {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private TraceEventComponent fEvent;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param session - a trace session component.
     * @param event - a trace event component
     */
    public EventCommandParameter(TraceSessionComponent session, TraceEventComponent event) {
        super(session);
        fEvent = event;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the trace event component
     */
    public TraceEventComponent getEvent() {
        return fEvent;
    }

    // ------------------------------------------------------------------------
    // Cloneable interface
    // ------------------------------------------------------------------------
    @Override
    public EventCommandParameter clone() {
        EventCommandParameter clone = (EventCommandParameter) super.clone();
        clone.fEvent = fEvent;
        return clone;
    }

}