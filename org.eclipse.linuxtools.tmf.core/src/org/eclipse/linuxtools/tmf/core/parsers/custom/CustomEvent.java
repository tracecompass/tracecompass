/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.parsers.custom;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTraceDefinition.OutputColumn;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Base event for custom text parsers.
 *
 * @author Patrick Tassé
 * @since 3.0
 */
public class CustomEvent extends TmfEvent {

    /** Input format key */
    protected static final String TIMESTAMP_INPUT_FORMAT_KEY = "CE_TS_I_F"; //$NON-NLS-1$

    /** Empty message */
    protected static final String NO_MESSAGE = ""; //$NON-NLS-1$

    /** Replacement for the super-class' timestamp field */
    private ITmfTimestamp customEventTimestamp;

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
        fDefinition = definition;
        fData = new HashMap<>();
    }

    /**
     * Build a new CustomEvent from an existing TmfEvent.
     *
     * @param definition
     *            The trace definition to which this event belongs
     * @param other
     *            The TmfEvent to copy
     */
    public CustomEvent(CustomTraceDefinition definition, TmfEvent other) {
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
     * @param source
     *            Source of the event
     * @param type
     *            Event type
     * @param reference
     *            Event reference
     */
    public CustomEvent(CustomTraceDefinition definition, ITmfTrace parentTrace,
            ITmfTimestamp timestamp, String source, TmfEventType type,
            String reference) {
        /* Do not use upstream's fields for stuff we override */
        super(parentTrace, null, source, null, null, reference);
        fDefinition = definition;
        fData = new HashMap<>();

        /* Set our overridden fields */
        customEventTimestamp = timestamp;
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
        return customEventContent;
    }

    @Override
    public ITmfEventType getType() {
        return customEventType;
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
    protected void setTimestamp(ITmfTimestamp timestamp) {
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
     * Get the contents of the row in the events table corresponding to this
     * event. The order of the elements corresponds to the order of the columns.
     *
     * @return The event row entries
     */
    public String[] getEventStrings() {
        if (fData != null) {
            processData();
        }
        String[] entries = new String[fColumnData.length];
        for (int i = 0; i < fColumnData.length; i++) {
            entries[i] = fColumnData[i].getValue().toString();
        }
        return entries;
    }

    private void processData() {
        String timestampString = fData.get(CustomTraceDefinition.TAG_TIMESTAMP);
        String timestampInputFormat = fData.get(TIMESTAMP_INPUT_FORMAT_KEY);
        TmfTimestamp timestamp = null;
        if (timestampInputFormat != null && timestampString != null) {
            TmfTimestampFormat timestampFormat = new TmfTimestampFormat(timestampInputFormat);
            try {
                long time = timestampFormat.parseValue(timestampString);
                timestamp = new TmfTimestamp(time, ITmfTimestamp.NANOSECOND_SCALE);
                setTimestamp(timestamp);
            } catch (ParseException e) {
                setTimestamp(TmfTimestamp.ZERO);
            }
        } else {
            setTimestamp(TmfTimestamp.ZERO);
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
        CustomEventContent curContent = (CustomEventContent) getContent();
        setContent(new CustomEventContent(curContent.getName(), curContent.getValue(), fColumnData));
        fData = null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fDefinition == null) ? 0 : fDefinition.hashCode());
        result = prime * result + ((customEventTimestamp == null) ? 0 : customEventTimestamp.hashCode());
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
        if (fDefinition == null) {
            if (other.fDefinition != null) {
                return false;
            }
        } else if (!fDefinition.equals(other.fDefinition)) {
            return false;
        }

        if (customEventTimestamp == null) {
            if (other.customEventTimestamp != null) {
                return false;
            }
        } else if (!customEventTimestamp.equals(other.customEventTimestamp)) {
            return false;
        }

        if (customEventContent == null) {
            if (other.customEventContent != null) {
                return false;
            }
        } else if (!customEventContent.equals(other.customEventContent)) {
            return false;
        }

        if (customEventType == null) {
            if (other.customEventType != null) {
                return false;
            }
        } else if (!customEventType.equals(other.customEventType)) {
            return false;
        }

        return true;
    }

}
