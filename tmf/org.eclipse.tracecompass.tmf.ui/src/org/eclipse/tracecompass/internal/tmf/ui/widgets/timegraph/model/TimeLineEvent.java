/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.widgets.timegraph.model;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;

/**
 * Generic TimeLineEvent implementation, basically a point with multiple
 * potential Y values and one X
 *
 * @author Matthew Khouzam
 */
public class TimeLineEvent extends TimeEvent {

    /**
     * One time line X and mutliple Ys. Able to support multiple lines for a
     * series.
     */
    private final List<@Nullable Long> fValues;

    /**
     * Label cache for tooltips
     */
    private String fLabel = null;

    /**
     * Standard constructor
     *
     * @param entry
     *            The entry matching this event
     * @param time
     *            The timestamp of this event
     */
    public TimeLineEvent(ITimeGraphEntry entry, long time) {
        this(entry, time, new ArrayList<>());
    }

    /**
     * Standard constructor
     *
     * @param entry
     *            The entry matching this event
     * @param time
     *            The timestamp of this event
     * @param values
     *            The values to display at this given timestamp
     */
    public TimeLineEvent(ITimeGraphEntry entry, long time, List<@Nullable Long> values) {
        super(entry, time, 0);
        fValues = values;
    }

    /**
     * Add a value
     *
     * @param value
     *            the value to add, it will be displayed as a line
     */
    public void addValue(@Nullable Long value) {
        fValues.add(value);
        fLabel = null;
    }

    @Override
    public String getLabel() {
        String label = fLabel;
        if (label == null) {
            StringJoiner sj = new StringJoiner(", "); //$NON-NLS-1$
            getValues().forEach((Long value) -> sj.add(value == null ? String.valueOf(value) : NumberFormat.getNumberInstance(Locale.getDefault()).format(value)));
            label = sj.toString();
            fLabel = label;
        }
        return label;
    }

    /**
     * Get the values to display
     *
     * @return the values to be displayed
     */
    public List<Long> getValues() {
        return Collections.unmodifiableList(fValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fValues);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (obj instanceof TimeLineEvent) {
            TimeLineEvent lineEvent = (TimeLineEvent) obj;
            return Objects.equals(getValues(), lineEvent.getValues());
        }
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " time=" + fTime + " value=[" + getLabel() + ']'; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
