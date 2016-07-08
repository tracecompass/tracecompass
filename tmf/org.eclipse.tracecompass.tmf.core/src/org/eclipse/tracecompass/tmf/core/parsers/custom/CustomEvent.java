/*******************************************************************************
 * Copyright (c) 2010, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.parsers.custom;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition.OutputColumn;
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

    /** Input format key */
    protected static final String TIMESTAMP_INPUT_FORMAT_KEY = "CE_TS_I_F"; //$NON-NLS-1$

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

    /** The payload data of this event, <field name, value> */
    protected Map<String, String> fData;

    private TmfEventField[] fColumnData;

    /**
     * Basic constructor.
     *
     * @param definition
     *            The trace definition to which this event belongs
     */
    public CustomEvent(CustomTraceDefinition definition) {
        super(null, ITmfContext.UNKNOWN_RANK, null, null, null);
        fDefinition = definition;
        fData = new HashMap<>();
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
        fData = new HashMap<>();

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
        fData = new HashMap<>();

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

    /**
     * Get the contents of an event table cell for this event's row.
     *
     * @param index
     *            The ID/index of the field to display. This corresponds to the
     *            index in the event content.
     * @return The String to display in the cell
     */
    public String getEventString(int index) {
        if (fData != null) {
            processData();
        }
        if (index < 0 || index >= fColumnData.length) {
            return ""; //$NON-NLS-1$
        }

        return fColumnData[index].getValue().toString();
    }

    private void processData() {
        String timestampString = fData.get(CustomTraceDefinition.TAG_TIMESTAMP);
        String timestampInputFormat = fData.get(TIMESTAMP_INPUT_FORMAT_KEY);
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
        String eventName = fData.get(CustomTraceDefinition.TAG_EVENT_TYPE);
        ITmfEventType type = getType();
        if (eventName != null && type instanceof CustomEventType) {
            ((CustomEventType) type).setName(eventName);
        }

        int i = 0;
        fColumnData = new TmfEventField[fDefinition.outputs.size()];
        for (OutputColumn outputColumn : fDefinition.outputs) {
            String value = fData.get(outputColumn.name);
            if (outputColumn.name.equals(CustomTraceDefinition.TAG_TIMESTAMP) && timestamp != null) {
                TmfTimestampFormat timestampFormat = new TmfTimestampFormat(fDefinition.timeStampOutputFormat);
                fColumnData[i++] = new TmfEventField(outputColumn.name, timestampFormat.format(timestamp.getValue()), null);
            } else {
                fColumnData[i++] = new TmfEventField(outputColumn.name, (value != null ? value : ""), null); //$NON-NLS-1$
            }
        }
        setContent(new CustomEventContent(customEventContent.getName(), customEventContent.getValue(), fColumnData));
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

        if (!Objects.equals(customEventType, other.customEventType)) {
            return false;
        }
        return true;
    }

}
