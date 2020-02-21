/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
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

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph;

import java.util.EventObject;

/**
 * Notifier for the time graph that the time range has been updated.
 *
 * @author Patrick Tasse
 */
public class TimeGraphRangeUpdateEvent extends EventObject {

    /**
     * Default serial version UID for this class.
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
