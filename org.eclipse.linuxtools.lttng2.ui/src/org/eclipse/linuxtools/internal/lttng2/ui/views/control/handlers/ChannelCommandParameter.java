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

import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceChannelComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceSessionComponent;

/**
 *  Class containing parameter for the command execution. 
 */
public class ChannelCommandParameter extends CommandParameter implements Cloneable {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private TraceChannelComponent fChannel;


    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param session - a trace session component.
     * @param channel - a trace channel component
     */
    public ChannelCommandParameter(TraceSessionComponent session, TraceChannelComponent channel) {
        super(session);
        fChannel = channel;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the trace channel component
     */
    public TraceChannelComponent getChannel() {
        return fChannel;
    }
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public ChannelCommandParameter clone() {
        ChannelCommandParameter clone = (ChannelCommandParameter) super.clone();
        clone.fChannel = fChannel;
        return clone;
    }
}