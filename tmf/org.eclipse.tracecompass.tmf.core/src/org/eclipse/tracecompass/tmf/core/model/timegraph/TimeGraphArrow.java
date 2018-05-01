/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.timegraph;

/**
 * Implementation of {@link ITimeGraphArrow}.
 *
 * @author Simon Delisle
 * @since 4.0
 */
public class TimeGraphArrow implements ITimeGraphArrow {
    private final long fSourceId;
    private final long fDestinationId;
    private final long fStartTime;
    private final long fDuration;
    private final int fValue;

    /**
     * Constructor
     *
     * @param sourceId
     *            ID of source element
     * @param destinationId
     *            ID of destination element
     * @param time
     *            Time
     * @param duration
     *            Duration
     * @param value
     *            value payload associated with this arrow
     */
    public TimeGraphArrow(long sourceId, long destinationId, long time, long duration, int value) {
        fSourceId = sourceId;
        fDestinationId = destinationId;
        fStartTime = time;
        fDuration = duration;
        fValue = value;
    }

    /**
     * Constructor
     *
     * @param sourceId
     *            ID of source element
     * @param destinationId
     *            ID of destination element
     * @param time
     *            Time
     * @param duration
     *            Duration
     */
    public TimeGraphArrow(long sourceId, long destinationId, long time, long duration) {
        this(sourceId, destinationId, time, duration, Integer.MIN_VALUE);
    }

    @Override
    public long getSourceId() {
        return fSourceId;
    }

    @Override
    public long getDestinationId() {
        return fDestinationId;
    }

    @Override
    public long getStartTime() {
        return fStartTime;
    }

    @Override
    public long getDuration() {
        return fDuration;
    }

    @Override
    public int getValue() {
        return fValue;
    }
}
