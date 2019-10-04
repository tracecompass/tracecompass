/*******************************************************************************
 * Copyright (c) 2012, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Geneviève Bastien - Added the fValue parameter to avoid subclassing
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.model.timegraph.IFilterProperty;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphState;

import com.google.common.collect.Multimap;

/**
 * Generic TimeEvent implementation
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TimeEvent implements ITimeEvent {

    /** TimeGraphEntry matching this time event */
    protected ITimeGraphEntry fEntry;

    private final ITimeGraphState fModel;

    /** Beginning timestamp of this time event */
    protected long fTime;

    /** Duration of this time event */
    protected long fDuration;

    /**
     * Default value when no other value present
     */
    private static final int NOVALUE = Integer.MIN_VALUE;

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
        this(entry, time, duration, NOVALUE);
    }

    /**
     * Constructor
     *
     * @param entry
     *            The entry to which this time event is assigned
     * @param time
     *            The timestamp of this event
     * @param duration
     *            The duration of this event
     * @param value
     *            The status assigned to the event
     */
    public TimeEvent(ITimeGraphEntry entry, long time, long duration, int value) {
        this(entry, new TimeGraphState(time, duration, value));
    }

    /**
     * Constructor
     *
     * @param entry
     *            The entry to which this time event is assigned
     * @param time
     *            The timestamp of this event
     * @param duration
     *            The duration of this event
     * @param value
     *            The status assigned to the event
     * @param activeProperties
     *            The active properties of the event represented by a bitmask
     *            value. Each bit represents a property. Available properties
     *            could be find in {@link IFilterProperty}.
     * @since 4.0
     */
    public TimeEvent(ITimeGraphEntry entry, long time, long duration, int value, int activeProperties) {
        this(entry, new TimeGraphState(time, duration, value));
        fModel.setActiveProperties(activeProperties);
    }

    /**
     * Constructor
     *
     * @param stateModel
     *            {@link ITimeGraphState} that represents this time event
     * @param entry
     *            The entry to which this time event is assigned
     * @since 5.2
     */
    public TimeEvent(ITimeGraphEntry entry, ITimeGraphState stateModel) {
        fEntry = entry;
        fTime = stateModel.getStartTime();
        fDuration = stateModel.getDuration();
        fModel = stateModel;
    }

    /**
     * Get this event's status
     *
     * @return The integer matching this status
     */
    public int getValue() {
        return fModel.getValue();
    }

    /**
     * Return whether an event has a value
     *
     * @return true if the event has a value
     */
    public boolean hasValue() {
        return (getValue() != NOVALUE);
    }

    @Override
    public ITimeGraphEntry getEntry() {
        return fEntry;
    }

    @Override
    public long getTime() {
        return fModel.getStartTime();
    }

    @Override
    public long getDuration() {
        return fModel.getDuration();
    }

    /**
     * Get the model associated with this time event
     *
     * @return State model
     * @since 5.2
     */
    public ITimeGraphState getStateModel() {
        return fModel;
    }

    @Override
    public ITimeEvent splitBefore(long splitTime) {
        return (splitTime > fTime ?
                new TimeEvent(fEntry, fTime, Math.min(fDuration, splitTime - fTime), getValue(), getActiveProperties()) :
                null);
    }

    @Override
    public ITimeEvent splitAfter(long splitTime) {
        return (splitTime < fTime + fDuration ?
                new TimeEvent(fEntry, Math.max(fTime, splitTime), fDuration - Math.max(0, splitTime - fTime),
                        getValue(), getActiveProperties()) :
                null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fEntry, fTime, fDuration, getValue(), getActiveProperties());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TimeEvent other = (TimeEvent) obj;
        return Objects.equals(fEntry, other.getEntry()) &&
                Objects.equals(fTime, other.getTime()) &&
                Objects.equals(fDuration, other.getDuration()) &&
                Objects.equals(getValue(), other.getValue()) &&
                Objects.equals(getActiveProperties(), other.getActiveProperties());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " start=" + fTime + " end=" + (fTime + fDuration) + " duration=" + fDuration + (hasValue() ? (" value=" + getValue()) : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }

    /**
     * @since 4.0
     */
    @Override
    public int getActiveProperties() {
        return fModel.getActiveProperties();
    }

    /**
     * @since 4.0
     */
    @Override
    public void setActiveProperties(int activeProperties) {
        fModel.setActiveProperties(activeProperties);
    }

    /**
     * @since 4.1
     */
    @Override
    public @NonNull Multimap<@NonNull String, @NonNull Object> getMetadata() {
        return fModel.getMetadata();
    }
}
