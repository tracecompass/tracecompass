/**********************************************************************
 * Copyright (c) 2017, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.timegraph;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.TmfStrings;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Implementation of {@link ITimeGraphState}
 *
 * @author Simon Delisle
 * @since 4.0
 */
public class TimeGraphState implements ITimeGraphState {
    private final long fStartTime;
    private final long fDuration;
    private final int fValue;
    private final @Nullable String fLabel;
    private final @Nullable OutputElementStyle fStyle;
    private @Nullable Multimap<String, Object> fMetadata = null;

    /**
     * A bitmap of properties to activate or deactivate
     */
    private int fActiveProperties = 0;

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
        fStartTime = time;
        fDuration = duration;
        fValue = value;
        fLabel = null;
        fStyle = null;
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
        fStartTime = time;
        fDuration = duration;
        fValue = value;
        fLabel = label;
        fStyle = null;
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
        fStartTime = time;
        fDuration = duration;
        fValue = Integer.MIN_VALUE;
        fLabel = label;
        fStyle = style;
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
    public @Nullable OutputElementStyle getStyle() {
        return fStyle;
    }

    @Override
    public synchronized Multimap<String, Object> getMetadata() {
        Multimap<String, Object> metadata = fMetadata;
        if (metadata == null) {
            metadata = HashMultimap.create();
            String label = getLabel();
            if (label != null) {
                metadata.put(IMetadataStrings.LABEL_KEY, label);
            }
            metadata.put(TmfStrings.startTime(), fStartTime);
            metadata.put(TmfStrings.endTime(), fStartTime + fDuration);
            metadata.put(TmfStrings.duration(), fDuration);
            fMetadata = metadata;
        }
        return metadata;
    }

    @Override
    public int getActiveProperties() {
        return fActiveProperties;
    }

    @Override
    public void setActiveProperties(int activeProperties) {
        fActiveProperties = activeProperties;
    }

    @Deprecated
    @Override
    public Map<String, String> computeData() {
        Map<String, String> toTest = new HashMap<>();
        String label = getLabel();
        if (label != null) {
            toTest.put(IElementResolver.LABEL_KEY, label);
        }
        return toTest;
    }
}
