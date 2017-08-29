/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.timegraph;

/**
 * Implementation of {@link ITimeGraphArrow}.
 *
 * @since 3.2
 * @author Simon Delisle
 */
public class TimeGraphArrow implements ITimeGraphArrow {
    private final long fSourceId;
    private final long fDestinationId;
    private final long fStartTime;
    private final long fDuration;

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
        fSourceId = sourceId;
        fDestinationId = destinationId;
        fStartTime = time;
        fDuration = duration;
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
}
