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

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources;

import org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources.ResourcesEntry.Type;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeEvent;

/**
 * Time Event implementation specific to the Resource View
 *
 * @author Patrick Tasse
 */
public class ResourcesEvent extends TimeEvent {

    private final Type fType;
    private int fValue;

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
     *            The value type associated to this event
     */
    public ResourcesEvent(ResourcesEntry entry, long time, long duration,
            int value) {
        super(entry, time, duration);
        fType = entry.getType();
        fValue = value;
    }

    /**
     * Base constructor, with no value assigned
     *
     * @param entry
     *            The entry that this event affects
     * @param time
     *            The start time of the event
     * @param duration
     *            The duration of the event
     */
    public ResourcesEvent(ResourcesEntry entry, long time, long duration) {
        super(entry, time, duration);
        fType = Type.NULL;
    }

    /**
     * Retrieve the value associated with this event
     *
     * @return The integer value
     */
    public int getValue() {
        return fValue;
    }

    /**
     * Retrieve the type of this entry. Uses the ResourcesEntry.Type interface.
     *
     * @return The entry type
     */
    public Type getType() {
        return fType;
    }

    @Override
    public String toString() {
        return "ResourcesEvent start=" + fTime + " end=" + (fTime + fDuration) + " duration=" + fDuration + " type=" + fType + " value=" + fValue; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }
}
