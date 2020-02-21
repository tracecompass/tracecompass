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
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceChannelComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;

/**
 * Class containing parameter for the command execution.
 *
 * @author Bernd Hufmann
 */
@NonNullByDefault
public class ChannelCommandParameter extends CommandParameter {

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
    // Cloneable interface
    // ------------------------------------------------------------------------
    @Override
    public ChannelCommandParameter clone() {
        ChannelCommandParameter clone = (ChannelCommandParameter) super.clone();
        clone.fChannel = fChannel;
        return clone;
    }
}