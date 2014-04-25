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

import java.util.regex.Matcher;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtTraceDefinition.InputData;
import org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtTraceDefinition.InputLine;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Trace event for custom text parsers.
 *
 * @author Patrick Tassé
 * @since 3.0
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
        setType(new CustomTxtEventType(definition));
    }

    /**
     * Construct a custom text event from an existing TmfEvent.
     *
     * @param definition
     *            Trace definition
     * @param other
     *            The TmfEvent object to copy
     */
    public CustomTxtEvent(CustomTxtTraceDefinition definition, TmfEvent other) {
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
     * @param source
     *            Source of this event
     * @param type
     *            Event type
     * @param reference
     *            Reference if this event
     */
    public CustomTxtEvent(CustomTxtTraceDefinition definition,
            ITmfTrace parentTrace, ITmfTimestamp timestamp, String source,
            TmfEventType type, String reference) {
        super(definition, parentTrace, timestamp, source, type, reference);
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
