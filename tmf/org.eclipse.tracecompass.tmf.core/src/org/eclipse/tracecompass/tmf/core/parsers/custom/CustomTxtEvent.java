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

import java.util.regex.Matcher;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition.Tag;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTraceDefinition.InputData;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTraceDefinition.InputLine;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Trace event for custom text parsers.
 *
 * @author Patrick Tassé
 */
public class CustomTxtEvent extends CustomEvent {

    private String fLastExtraFieldName = null;

    /**
     * Constructor
     *
     * @param definition
     *            Trace definition
     */
    public CustomTxtEvent(CustomTxtTraceDefinition definition) {
        super(definition);
    }

    /**
     * Construct a custom text event from an existing TmfEvent.
     *
     * @param definition
     *            Trace definition
     * @param other
     *            The TmfEvent object to copy
     */
    public CustomTxtEvent(CustomTxtTraceDefinition definition, @NonNull TmfEvent other) {
        super(definition, other);
    }

    /**
     * Full constructor.
     *
     * @param definition
     *            Trace definition
     * @param parentTrace
     *            Parent trace object
     * @param timestamp
     *            Timestamp of this event
     * @param type
     *            Event type
     */
    public CustomTxtEvent(CustomTxtTraceDefinition definition,
            ITmfTrace parentTrace, ITmfTimestamp timestamp, TmfEventType type) {
        super(definition, parentTrace, timestamp, type);
    }

    @Override
    public void setContent(ITmfEventField content) {
        super.setContent(content);
    }

    /**
     * Process an entry in the trace file
     *
     * @param input
     *            The input line to read
     * @param matcher
     *            The regex matcher to use
     */
    public void processGroups(InputLine input, Matcher matcher) {
        if (input.eventType != null) {
            fData.put(Tag.EVENT_TYPE, input.eventType);
        }
        if (input.columns == null) {
            return;
        }
        for (int i = 0; i < input.columns.size(); i++) {
            InputData column = input.columns.get(i);
            if (i < matcher.groupCount() && matcher.group(i + 1) != null) {
                String value = matcher.group(i + 1).trim();
                if (value.length() == 0) {
                    continue;
                }
                Object key = (column.tag.equals(Tag.OTHER) ? column.name : column.tag);
                if (key.equals(Tag.EXTRA_FIELD_NAME)) {
                    // If tag extra field name, save the extra field name for
                    // the next extra field value and add the field to the map
                    fLastExtraFieldName = value;
                    if (!fData.containsKey(value)) {
                        fData.put(value, null);
                    }
                    continue;
                } else if (key.equals(Tag.EXTRA_FIELD_VALUE)) {
                    // If tag extra field value, use the extra field name as key
                    if (fLastExtraFieldName == null) {
                        continue;
                    }
                    key = fLastExtraFieldName;
                }
                if (column.action == CustomTraceDefinition.ACTION_SET) {
                    fData.put(key, value);
                    if (key.equals(Tag.TIMESTAMP)) {
                        fData.put(Key.TIMESTAMP_INPUT_FORMAT, column.format);
                    }
                } else if (column.action == CustomTraceDefinition.ACTION_APPEND) {
                    String s = fData.get(key);
                    if (s != null) {
                        fData.put(key, s + value);
                    } else {
                        fData.put(key, value);
                    }
                    if (key.equals(Tag.TIMESTAMP)) {
                        String timeStampInputFormat = fData.get(Key.TIMESTAMP_INPUT_FORMAT);
                        if (timeStampInputFormat != null) {
                            fData.put(Key.TIMESTAMP_INPUT_FORMAT, timeStampInputFormat + column.format);
                        } else {
                            fData.put(Key.TIMESTAMP_INPUT_FORMAT, column.format);
                        }
                    }
                } else if (column.action == CustomTraceDefinition.ACTION_APPEND_WITH_SEPARATOR) {
                    String s = fData.get(key);
                    if (s != null) {
                        fData.put(key, s + CustomTraceDefinition.SEPARATOR + value);
                    } else {
                        fData.put(key, value);
                    }
                    if (key.equals(Tag.TIMESTAMP)) {
                        String timeStampInputFormat = fData.get(Key.TIMESTAMP_INPUT_FORMAT);
                        if (timeStampInputFormat != null) {
                            fData.put(Key.TIMESTAMP_INPUT_FORMAT, timeStampInputFormat + " | " + column.format); //$NON-NLS-1$
                        } else {
                            fData.put(Key.TIMESTAMP_INPUT_FORMAT, column.format);
                        }
                    }
                }
            }
        }
    }

}
