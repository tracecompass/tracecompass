/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.controlflow;

import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeEvent;

/**
 * Time Event specific to the control flow view
 */
public class ControlFlowEvent extends TimeEvent {

    private final int fStatus;

    /**
     * Constructor
     *
     * @param entry
     *            The entry to which this time event is assigned
     * @param time
     *            The timestamp of this event
     * @param duration
     *            The duration of this event
     * @param status
     *            The status assigned to the event
     */
    public ControlFlowEvent(ITimeGraphEntry entry, long time, long duration,
            int status) {
        super(entry, time, duration);
        fStatus = status;
    }

    /**
     * Get this event's status
     *
     * @return The integer matching this status
     */
    public int getStatus() {
        return fStatus;
    }

    @Override
    public String toString() {
        return "ControlFlowEvent start=" + fTime + " end=" + (fTime + fDuration) + " duration=" + fDuration + " status=" + fStatus; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}
