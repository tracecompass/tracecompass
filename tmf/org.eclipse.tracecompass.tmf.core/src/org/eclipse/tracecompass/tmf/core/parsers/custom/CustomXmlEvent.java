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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition.Tag;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Trace event for custom XML traces.
 *
 * @author Patrick Tassé
 */
public class CustomXmlEvent extends CustomEvent {

    private String fLastExtraFieldName = null;

    /**
     * Constructor defining only the trace definition
     *
     * @param definition
     *            Trace definition
     */
    public CustomXmlEvent(CustomXmlTraceDefinition definition) {
        super(definition);
    }

    /**
     * Build a custom trace event from an existing TmfEvent.
     *
     * @param definition
     *            Trace definition
     * @param other
     *            Other TmfEvent to copy
     */
    public CustomXmlEvent(CustomXmlTraceDefinition definition, @NonNull TmfEvent other) {
        super(definition, other);
    }

    /**
     * Full constructor
     *
     * @param definition
     *            Trace definition
     * @param parentTrace
     *            Parent trace object
     * @param timestamp
     *            Timestamp of the event
     * @param type
     *            Event type
     */
    public CustomXmlEvent(CustomXmlTraceDefinition definition,
            ITmfTrace parentTrace, ITmfTimestamp timestamp, TmfEventType type) {
        super(definition, parentTrace, timestamp, type);
    }

    @Override
    public void setContent(ITmfEventField content) {
        super.setContent(content);
    }

    /**
     * Parse an entry.
     *
     * @param value Value
     * @param name Name
     * @param inputAction Input action
     * @param inputFormat Input format
     * @deprecated Use {@link #parseInput(String, Tag, String, int, String)} instead.
     */
    @Deprecated
    public void parseInput(String value, String name, int inputAction, String inputFormat) {
    }

    /**
     * Parse an entry.
     *
     * @param value Value
     * @param inputTag Input tag
     * @param inputName Input name
     * @param inputAction Input action
     * @param inputFormat Input format
     * @since 2.1
     */
    public void parseInput(String value, Tag inputTag, String inputName, int inputAction, String inputFormat) {
        if (value.length() == 0) {
            return;
        }
        Object key = (inputTag.equals(Tag.OTHER) ? inputName : inputTag);
        if (key.equals(Tag.EXTRA_FIELD_NAME)) {
            // If tag extra field name, save the extra field name for
            // the next extra field value and add the field to the map
            fLastExtraFieldName = value;
            if (!fData.containsKey(value)) {
                fData.put(value, null);
            }
            return;
        } else if (key.equals(Tag.EXTRA_FIELD_VALUE)) {
            // If tag extra field value, use the extra field name as key
            if (fLastExtraFieldName == null) {
                return;
            }
            key = fLastExtraFieldName;
        }
        if (inputAction == CustomTraceDefinition.ACTION_SET) {
            fData.put(key, value);
            if (key.equals(Tag.TIMESTAMP)) {
                fData.put(Key.TIMESTAMP_INPUT_FORMAT, inputFormat);
            }
        } else if (inputAction == CustomTraceDefinition.ACTION_APPEND) {
            String s = fData.get(key);
            if (s != null) {
                fData.put(key, s + value);
            } else {
                fData.put(key, value);
            }
            if (key.equals(Tag.TIMESTAMP)) {
                String timeStampInputFormat = fData.get(Key.TIMESTAMP_INPUT_FORMAT);
                if (timeStampInputFormat != null) {
                    fData.put(Key.TIMESTAMP_INPUT_FORMAT, timeStampInputFormat + inputFormat);
                } else {
                    fData.put(Key.TIMESTAMP_INPUT_FORMAT, inputFormat);
                }
            }
        } else if (inputAction == CustomTraceDefinition.ACTION_APPEND_WITH_SEPARATOR) {
            String s = fData.get(key);
            if (s != null) {
                fData.put(key, s + CustomTraceDefinition.SEPARATOR + value);
            } else {
                fData.put(key, value);
            }
            if (key.equals(Tag.TIMESTAMP)) {
                String timeStampInputFormat = fData.get(Key.TIMESTAMP_INPUT_FORMAT);
                if (timeStampInputFormat != null) {
                    fData.put(Key.TIMESTAMP_INPUT_FORMAT, timeStampInputFormat + " | " + inputFormat); //$NON-NLS-1$
                } else {
                    fData.put(Key.TIMESTAMP_INPUT_FORMAT, inputFormat);
                }
            }
        }
    }

}
