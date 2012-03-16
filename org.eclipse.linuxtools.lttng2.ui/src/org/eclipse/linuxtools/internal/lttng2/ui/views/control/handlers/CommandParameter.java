/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.handlers;

import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent;

/**
 *  Class containing parameter for the command execution. 
 */
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
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public CommandParameter clone() {
        CommandParameter clone = null;
        try {
            clone = (CommandParameter) super.clone();
            clone.fSession = fSession;
        } catch (CloneNotSupportedException e) {
        }
        return clone;
    }
}