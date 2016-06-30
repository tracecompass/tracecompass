/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.callstack;

import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;

/**
 * Time Event implementation specific to the Call Stack View
 *
 * @author Patrick Tasse
 */
public class CallStackEvent extends TimeEvent {

    /**
     * Standard constructor
     *
     * @param entry
     *            The entry that this event affects
     * @param time
     *            The start time of the event
     * @param duration
     *            The duration of the event
     * @param value
     *            The event value (1-256)
     */
    public CallStackEvent(CallStackEntry entry, long time, long duration, int value) {
        super(entry, time, duration, value);
    }

    /**
     * @since 2.1
     */
    @Override
    public CallStackEntry getEntry() {
        /* Type enforced at constructor */
        return (CallStackEntry) fEntry;
    }

}
