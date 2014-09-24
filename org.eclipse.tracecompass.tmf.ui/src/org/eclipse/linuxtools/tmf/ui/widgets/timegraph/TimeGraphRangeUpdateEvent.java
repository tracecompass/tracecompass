/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph;

import java.util.EventObject;

/**
 * Notifier for the time graph that the time range has been updated.
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TimeGraphRangeUpdateEvent extends EventObject {

    /**
     * Default serial version UID for this class.
     * @since 1.0
     */
    private static final long serialVersionUID = 1L;

    /**
     * The start time.
     */
    private final long fStartTime;

    /**
     * The end time.
     */
    private final long fEndTime;

    /**
     * Standard constructor
     *
     * @param source
     *            The source of this event
     * @param startTime
     *            The start time
     * @param endTime
     *            The end time
     */
    public TimeGraphRangeUpdateEvent(Object source, long startTime, long endTime) {
        super(source);
        fStartTime = startTime;
        fEndTime = endTime;
    }

    /**
     * @return the start time
     */
    public long getStartTime() {
        return fStartTime;
    }

    /**
     * @return the end time
     */
    public long getEndTime() {
        return fEndTime;
    }

}
