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

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model;

/**
 * Generic TimeEvent implementation
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TimeEvent implements ITimeEvent {

    /** TimeGraphEntry matching this time event */
    protected ITimeGraphEntry fEntry;

    /** Beginning timestamp of this time event */
    protected long fTime;

    /** Duration of this time event */
    protected long fDuration;

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
    public TimeEvent(ITimeGraphEntry entry, long time, long duration) {
        fEntry = entry;
        fTime = time;
        fDuration = duration;
    }

    @Override
    public ITimeGraphEntry getEntry() {
        return fEntry;
    }

    @Override
    public long getTime() {
        return fTime;
    }

    @Override
    public long getDuration() {
        return fDuration;
    }

    @Override
    public String toString() {
        return "TimeEvent start=" + fTime + " end=" + (fTime + fDuration) + " duration=" + fDuration; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
