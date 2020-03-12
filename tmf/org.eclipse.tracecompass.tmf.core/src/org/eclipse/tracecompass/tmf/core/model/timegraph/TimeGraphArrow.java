/**********************************************************************
 * Copyright (c) 2017, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.timegraph;

import org.eclipse.tracecompass.tmf.core.model.OutputElement;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;

/**
 * Implementation of {@link ITimeGraphArrow}.
 *
 * @author Simon Delisle
 * @since 4.0
 */
public class TimeGraphArrow extends OutputElement implements ITimeGraphArrow {
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
        super(null);
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
     * @param style
     *            Style
     * @since 5.2
     */
    public TimeGraphArrow(long sourceId, long destinationId, long time, long duration, OutputElementStyle style) {
        super(style);
        fSourceId = sourceId;
        fDestinationId = destinationId;
        fStartTime = time;
        fDuration = duration;
        fValue = Integer.MIN_VALUE;
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

    @Override
    public String toString() {
        return String.format("Arrow: sourceId:%d, destinationId:%d, time: %d, duration: %d, value: %d, style: %s", fSourceId, fDestinationId, fStartTime, fDuration, fValue, getStyle()); //$NON-NLS-1$
    }
}
