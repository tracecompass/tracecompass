/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model;

/**
 * A null time event. Used to represent an event that should not be drawn,
 * for example as a zoomed event that overshadows an underlying event.
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

    @Override
    public ITimeEvent splitBefore(long splitTime) {
        return (splitTime > fTime ?
                new NullTimeEvent(fEntry, fTime, Math.min(fDuration, splitTime - fTime)) :
                null);
    }

    @Override
    public ITimeEvent splitAfter(long splitTime) {
        return (splitTime < fTime + fDuration ?
                new NullTimeEvent(fEntry, Math.max(fTime, splitTime), fDuration - Math.max(0, splitTime - fTime)) :
                null);
    }

}
