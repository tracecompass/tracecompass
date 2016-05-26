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
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Trace event for custom XML traces.
 *
 * @author Patrick Tassé
 */
public class CustomXmlEvent extends CustomEvent {

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
     */
    public void parseInput(String value, String name, int inputAction, String inputFormat) {
        if (value.length() == 0) {
            return;
        }
        if (inputAction == CustomTraceDefinition.ACTION_SET) {
            fData.put(name, value);
            if (name.equals(CustomTraceDefinition.TAG_TIMESTAMP)) {
                fData.put(TIMESTAMP_INPUT_FORMAT_KEY, inputFormat);
            }
        } else if (inputAction == CustomTraceDefinition.ACTION_APPEND) {
            String s = fData.get(name);
            if (s != null) {
                fData.put(name, s + value);
            } else {
                fData.put(name, value);
            }
            if (name.equals(CustomTraceDefinition.TAG_TIMESTAMP)) {
                String timeStampInputFormat = fData.get(TIMESTAMP_INPUT_FORMAT_KEY);
                if (timeStampInputFormat != null) {
                    fData.put(TIMESTAMP_INPUT_FORMAT_KEY, timeStampInputFormat + inputFormat);
                } else {
                    fData.put(TIMESTAMP_INPUT_FORMAT_KEY, inputFormat);
                }
            }
        } else if (inputAction == CustomTraceDefinition.ACTION_APPEND_WITH_SEPARATOR) {
            String s = fData.get(name);
            if (s != null) {
                fData.put(name, s + " | " + value); //$NON-NLS-1$
            } else {
                fData.put(name, value);
            }
            if (name.equals(CustomTraceDefinition.TAG_TIMESTAMP)) {
                String timeStampInputFormat = fData.get(TIMESTAMP_INPUT_FORMAT_KEY);
                if (timeStampInputFormat != null) {
                    fData.put(TIMESTAMP_INPUT_FORMAT_KEY, timeStampInputFormat + " | " + inputFormat); //$NON-NLS-1$
                } else {
                    fData.put(TIMESTAMP_INPUT_FORMAT_KEY, inputFormat);
                }
            }
        }
    }

}
