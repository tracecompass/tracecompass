/*******************************************************************************
 * Copyright (c) 2010, 2016 Ericsson
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

package org.eclipse.tracecompass.tmf.core.parsers.custom;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.parsers.custom.CustomExtraField;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition.OutputColumn;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition.Tag;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Base event for custom text parsers.
 *
 * @author Patrick Tassé
 */
public class CustomEvent extends TmfEvent {

    /** Payload data map key
     * @since 2.1*/
    protected enum Key {
        /** Timestamp input format */
        TIMESTAMP_INPUT_FORMAT
    }

    /** Empty message */
    protected static final String NO_MESSAGE = ""; //$NON-NLS-1$

    /** Replacement for the super-class' timestamp field */
    private @NonNull ITmfTimestamp customEventTimestamp;

    /** Replacement for the super-class' content field */
    private ITmfEventField customEventContent;

    /** Replacement for the super-class' type field */
    private ITmfEventType customEventType;

    /** The trace to which this event belongs */
    protected CustomTraceDefinition fDefinition;

    /**
     * The payload data of this event, where the key is one of: the {@link Tag},
     * the field name string if the tag is {@link Tag#OTHER}, or
     * {@link Key#TIMESTAMP_INPUT_FORMAT}.
     */
    protected Map<Object, String> fData;

    /**
     * Basic constructor.
     *
     * @param definition
     *            The trace definition to which this event belongs
     */
    public CustomEvent(CustomTraceDefinition definition) {
        super(null, ITmfContext.UNKNOWN_RANK, null, null, null);
        fDefinition = definition;
        fData = new LinkedHashMap<>();
        customEventTimestamp = TmfTimestamp.ZERO;
    }

    /**
     * Build a new CustomEvent from an existing TmfEvent.
     *
     * @param definition
     *            The trace definition to which this event belongs
     * @param other
     *            The TmfEvent to copy
     */
    public CustomEvent(CustomTraceDefinition definition, @NonNull TmfEvent other) {
        super(other);
        fDefinition = definition;
        fData = new LinkedHashMap<>();

        /* Set our overridden fields */
        customEventTimestamp = other.getTimestamp();
        customEventContent = other.getContent();
        customEventType = other.getType();
    }

    /**
     * Full constructor
     *
     * @param definition
     *            Trace definition of this event
     * @param parentTrace
     *            Parent trace object
     * @param timestamp
     *            Timestamp of this event
     * @param type
     *            Event type
     */
    public CustomEvent(CustomTraceDefinition definition, ITmfTrace parentTrace,
            ITmfTimestamp timestamp, TmfEventType type) {
        /* Do not use upstream's fields for stuff we override */
        super(parentTrace, ITmfContext.UNKNOWN_RANK, null, null, null);
        fDefinition = definition;
        fData = new LinkedHashMap<>();

        /* Set our overridden fields */
        if (timestamp == null) {
            customEventTimestamp = TmfTimestamp.ZERO;
        } else {
            customEventTimestamp = timestamp;
        }
        customEventContent = null;
        customEventType = type;
    }

    // ------------------------------------------------------------------------
    // Overridden getters
    // ------------------------------------------------------------------------

    @Override
    public ITmfTimestamp getTimestamp() {
        if (fData != null) {
            processData();
        }
        return customEventTimestamp;
    }

    @Override
    public ITmfEventField getContent() {
        if (fData != null) {
            processData();
        }
        return customEventContent;
    }

    @Override
    public ITmfEventType getType() {
        return customEventType;
    }

    @Override
    public String getName() {
        if (fData != null) {
            processData();
        }
        return super.getName();
    }

    // ------------------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------------------

    /**
     * Set this event's timestamp
     *
     * @param timestamp
     *            The new timestamp
     */
    protected void setTimestamp(@NonNull ITmfTimestamp timestamp) {
        customEventTimestamp = timestamp;
    }

    /**
     * Set this event's content
     *
     * @param content
     *            The new content
     */
    protected void setContent(ITmfEventField content) {
        customEventContent = content;
    }

    /**
     * Get this event's content value.
     * <p>
     * This does not process the payload data and is therefore safe to call in
     * the middle of parsing an event.
     *
     * @return the event's content value.
     */
    Object getContentValue() {
        return customEventContent.getValue();
    }

    /**
     * Set this event's type
     *
     * @param type
     *            The new type
     */
    protected void setType(ITmfEventType type) {
        customEventType = type;
    }

    // ------------------------------------------------------------------------
    // Other operations
    // ------------------------------------------------------------------------

    private void processData() {
        // Remove the values as they are processed, so we can process the extra values at the end
        String timestampString = fData.remove(Tag.TIMESTAMP);
        String timestampInputFormat = fData.remove(Key.TIMESTAMP_INPUT_FORMAT);
        ITmfTimestamp timestamp = null;
        if (timestampInputFormat != null && timestampString != null) {
            TmfTimestampFormat timestampFormat = new TmfTimestampFormat(timestampInputFormat);
            try {
                long time = timestampFormat.parseValue(timestampString);
                timestamp = TmfTimestamp.fromNanos(getTrace().getTimestampTransform().transform(time));
                setTimestamp(timestamp);
            } catch (ParseException e) {
                setTimestamp(TmfTimestamp.ZERO);
            }
        } else {
            setTimestamp(TmfTimestamp.ZERO);
        }

        // Update the custom event type of this event if set
        String eventName = fData.remove(Tag.EVENT_TYPE);
        ITmfEventType type = getType();
        if (eventName != null && type instanceof CustomEventType) {
            ((CustomEventType) type).setName(eventName);
        }

        Map<String, TmfEventField> fieldMap = new LinkedHashMap<>();
        for (OutputColumn outputColumn : fDefinition.outputs) {
            if (outputColumn.tag.equals(Tag.TIMESTAMP)) {
                if (timestamp != null && fDefinition.timeStampOutputFormat != null && !fDefinition.timeStampOutputFormat.isEmpty()) {
                    TmfTimestampFormat timestampFormat = new TmfTimestampFormat(fDefinition.timeStampOutputFormat);
                    fieldMap.put(outputColumn.name, new TmfEventField(outputColumn.name, timestampFormat.format(timestamp.getValue()), null));
                }
            } else if (outputColumn.tag.equals(Tag.OTHER) || outputColumn.tag.equals(Tag.MESSAGE)) {
                Object key = (outputColumn.tag.equals(Tag.OTHER) ? outputColumn.name : outputColumn.tag);
                fieldMap.put(outputColumn.name, new TmfEventField(outputColumn.name, nullToEmptyString(fData.remove(key)), null));
            }
        }
        // This event contains extra values, we process them now
        for (Entry<Object, String> entry : fData.entrySet()) {
            String fieldName = nullToEmptyString(entry.getKey().toString());
            // Ignore extra fields if a field of same name is already set
            if (!fieldMap.containsKey(fieldName)) {
                fieldMap.put(fieldName, new CustomExtraField(fieldName, nullToEmptyString(entry.getValue()), null));
            }
        }
        setContent(new CustomEventContent(customEventContent.getName(), customEventContent.getValue(), fieldMap.values().toArray(new ITmfEventField[fieldMap.size()])));
        fData = null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fDefinition == null) ? 0 : fDefinition.hashCode());
        result = prime * result + customEventTimestamp.hashCode();
        result = prime * result + ((customEventContent == null) ? 0 : customEventContent.hashCode());
        result = prime * result + ((customEventType == null) ? 0 : customEventType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof CustomEvent)) {
            return false;
        }
        CustomEvent other = (CustomEvent) obj;
        if (!Objects.equals(fDefinition, other.fDefinition)) {
            return false;
        }

        if (!customEventTimestamp.equals(other.customEventTimestamp)) {
            return false;
        }

        if (!Objects.equals(customEventContent, other.customEventContent)) {
            return false;
        }

        return (Objects.equals(customEventType, other.customEventType));
    }

}
