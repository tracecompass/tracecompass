/**********************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.handlers;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;

/**
 * Class containing parameter for the command execution.
 *
 * @author Bernd Hufmann
 */
@NonNullByDefault
public class CommandParameter {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The trace session component.
     */
    private TraceSessionComponent fSession;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param session a trace session component.
     */
    public CommandParameter(TraceSessionComponent session) {
        fSession = session;
    }

    /**
     * Copy constructor
     *
     * @param param
     *            a command parameter to copy
     */
    public CommandParameter(CommandParameter param) {
        fSession = param.fSession;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the session component.
     */
    public TraceSessionComponent getSession() {
        return fSession;
    }
}