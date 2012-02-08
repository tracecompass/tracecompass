/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.parsers.custom;

import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventReference;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

public class CustomXmlEvent extends CustomEvent {

    public CustomXmlEvent(CustomXmlTraceDefinition definition) {
        super(definition);
        fType = new CustomXmlEventType(definition);
    }

    public CustomXmlEvent(CustomXmlTraceDefinition definition, TmfEvent other) {
        super(definition, other);
    }

    public CustomXmlEvent(CustomXmlTraceDefinition definition, ITmfTrace<?> parentTrace, TmfTimestamp timestamp, String source, TmfEventType type, TmfEventReference reference) {
        super(definition, parentTrace, timestamp, source, type, reference);
    }

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
