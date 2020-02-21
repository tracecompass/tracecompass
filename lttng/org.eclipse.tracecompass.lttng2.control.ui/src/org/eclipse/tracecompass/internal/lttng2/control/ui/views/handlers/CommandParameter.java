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
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;

/**
 * Class containing parameter for the command execution.
 *
 * @author Bernd Hufmann
 */
@NonNullByDefault
public class CommandParameter implements Cloneable {

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

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the session component.
     */
    public TraceSessionComponent getSession() {
        return fSession;
    }

    // ------------------------------------------------------------------------
    // Cloneable interface
    // ------------------------------------------------------------------------
    @Override
    protected CommandParameter clone() {
        CommandParameter clone;
        try {
            clone = (CommandParameter) super.clone();
            clone.fSession = fSession;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException();
        }
        return clone;
    }
}