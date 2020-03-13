/*******************************************************************************
 * Copyright (c) 2017, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.model.timegraph.IMetadataStrings;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphState;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * {@link TimeEvent} with a label.
 *
 * @since 3.3
 * @author Loic Prieur-Drevon
 */
public class NamedTimeEvent extends TimeEvent {
    private final @NonNull String fLabel;

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
     * @param label
     *            This event's label
     */
    public NamedTimeEvent(ITimeGraphEntry entry, long time, long duration,
            int value, @NonNull String label) {
        super(entry, time, duration, value);
        fLabel = label.intern();
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
     * @param label
     *            This event's label
     * @param activeProperties
     *            The active properties
     * @since 4.0
     */
    public NamedTimeEvent(TimeGraphEntry entry, long time, long duration, int value, String label, int activeProperties) {
        super(entry, time, duration, value, activeProperties);
        fLabel = label.intern();
    }

    /**
     * Constructor
     *
     * @param entry
     *            The entry to which this time event is assigned
     * @param label
     *            This event's label
     * @param stateModel
     *            {@link ITimeGraphState} that represents this time event
     * @since 5.2
     */
    public NamedTimeEvent(ITimeGraphEntry entry, String label, ITimeGraphState stateModel) {
        super(entry, stateModel);
        fLabel = label.intern();
    }

    @Override
    public @NonNull String getLabel() {
        return fLabel;
    }

    /**
     * @since 4.0
     * @deprecated As of 5.3, use the {@link #getMetadata()} instead
     */
    @Deprecated
    @Override
    public @NonNull Map<@NonNull String, @NonNull String> computeData() {
        Map<@NonNull String, @NonNull String> data = super.computeData();
        data.put(IMetadataStrings.LABEL_KEY, getLabel());
        return data;
    }

    @Override
    public @NonNull Multimap<@NonNull String, @NonNull Object> getMetadata() {
        Multimap<@NonNull String, @NonNull Object> metadata = super.getMetadata();
        String entryName = getEntry().getName();
        if (entryName == null) {
            return metadata;
        }
        com.google.common.collect.ImmutableMultimap.Builder<String, Object> builder = ImmutableMultimap.builder();
        builder.putAll(super.getMetadata());
        builder.put(IMetadataStrings.ENTRY_NAME_KEY, entryName);
        return builder.build();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NamedTimeEvent) {
            return super.equals(obj) && Objects.equals(fLabel, ((NamedTimeEvent) obj).fLabel);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fLabel);
    }
}
