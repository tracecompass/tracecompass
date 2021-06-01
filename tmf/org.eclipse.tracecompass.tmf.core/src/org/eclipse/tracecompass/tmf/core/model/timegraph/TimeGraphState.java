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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.TmfStrings;
import org.eclipse.tracecompass.tmf.core.model.CoreMetadataStrings;
import org.eclipse.tracecompass.tmf.core.model.OutputElement;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;

import com.google.common.collect.Multimap;

/**
 * Implementation of {@link ITimeGraphState}
 *
 * @author Simon Delisle
 * @since 4.0
 */
public class TimeGraphState extends OutputElement implements ITimeGraphState {
    private final long fStartTime;
    private final long fDuration;
    private final int fValue;
    private final @Nullable String fLabel;

    /**
     * Constructor
     *
     * @param time
     *            Time
     * @param duration
     *            State duration
     * @param value
     *            Type of state (event type)
     */
    public TimeGraphState(long time, long duration, int value) {
        super(null);
        fStartTime = time;
        fDuration = duration;
        fValue = value;
        fLabel = null;
    }

    /**
     * Constructor
     *
     * @param time
     *            Time
     * @param duration
     *            State duration
     * @param value
     *            Type of state (event type)
     * @param label
     *            State label
     */
    public TimeGraphState(long time, long duration, int value, String label) {
        this(time, duration, value, label, null);
    }

    /**
     * Constructor
     *
     * @param time
     *            Time
     * @param duration
     *            State duration
     * @param label
     *            State label
     * @param style
     *            Style
     * @since 5.2
     */
    public TimeGraphState(long time, long duration, @Nullable String label, @Nullable OutputElementStyle style) {
        this(time, duration, Integer.MIN_VALUE, label, style);
    }

    /**
     * Constructor
     *
     * @param time
     *            Time
     * @param duration
     *            State duration
     * @param value
     *            Type of state (event type)
     * @param label
     *            State label
     * @param style
     *            Style
     * @since 5.2
     */
    public TimeGraphState(long time, long duration, int value, @Nullable String label, @Nullable OutputElementStyle style) {
        super(style);
        fStartTime = time;
        fDuration = duration;
        fValue = value;
        fLabel = label;
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
    public @Nullable String getLabel() {
        return fLabel;
    }

    @Override
    public synchronized Multimap<String, Object> getMetadata() {
        Multimap<String, Object> metadata = super.getMetadata();
        String label = getLabel();
        if (label != null) {
            metadata.put(CoreMetadataStrings.LABEL_KEY, label);
        }
        metadata.put(TmfStrings.startTime(), fStartTime);
        metadata.put(TmfStrings.endTime(), fStartTime + fDuration);
        metadata.put(TmfStrings.duration(), fDuration);
        return metadata;
    }

    @Override
    public String toString() {
        return String.format("State: time: %d, duration: %d, value: %d, label: %s, style: %s", fStartTime, fDuration, fValue, fLabel, getStyle()); //$NON-NLS-1$
    }
}
