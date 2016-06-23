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

import java.util.regex.Matcher;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;
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
            fData.put(CustomTraceDefinition.TAG_EVENT_TYPE, input.eventType);
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
                String name = column.name;
                if (column.action == CustomTraceDefinition.ACTION_SET) {
                    fData.put(name, value);
                    if (name.equals(CustomTraceDefinition.TAG_TIMESTAMP)) {
                        fData.put(TIMESTAMP_INPUT_FORMAT_KEY, column.format);
                    }
                } else if (column.action == CustomTraceDefinition.ACTION_APPEND) {
                    String s = fData.get(name);
                    if (s != null) {
                        fData.put(name, s + value);
                    } else {
                        fData.put(name, value);
                    }
                    if (name.equals(CustomTraceDefinition.TAG_TIMESTAMP)) {
                        String timeStampInputFormat = fData.get(TIMESTAMP_INPUT_FORMAT_KEY);
                        if (timeStampInputFormat != null) {
                            fData.put(TIMESTAMP_INPUT_FORMAT_KEY, timeStampInputFormat + column.format);
                        } else {
                            fData.put(TIMESTAMP_INPUT_FORMAT_KEY, column.format);
                        }
                    }
                } else if (column.action == CustomTraceDefinition.ACTION_APPEND_WITH_SEPARATOR) {
                    String s = fData.get(name);
                    if (s != null) {
                        fData.put(name, s + " | " + value); //$NON-NLS-1$
                    } else {
                        fData.put(name, value);
                    }
                    if (name.equals(CustomTraceDefinition.TAG_TIMESTAMP)) {
                        String timeStampInputFormat = fData.get(TIMESTAMP_INPUT_FORMAT_KEY);
                        if (timeStampInputFormat != null) {
                            fData.put(TIMESTAMP_INPUT_FORMAT_KEY, timeStampInputFormat + " | " + column.format); //$NON-NLS-1$
                        } else {
                            fData.put(TIMESTAMP_INPUT_FORMAT_KEY, column.format);
                        }
                    }
                }
            }
        }
    }

}
