/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model;

/**
 * A null time event. Used to represent an event that should not be drawn,
 * for example as a zoomed event that overshadows an underlying event.
 *
 * @since 2.0
 */
public class NullTimeEvent extends TimeEvent {

    /**
     * Standard constructor
     *
     * @param entry
     *            The entry matching this event
     * @param time
     *            The timestamp of this event
     * @param duration
     *            The duration of the event
     */
    public NullTimeEvent(ITimeGraphEntry entry, long time, long duration) {
        super(entry, time, duration);
    }

}
