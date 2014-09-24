/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.stubs.trace.text;

import java.util.List;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.collapse.ITmfCollapsibleEvent;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.text.TextTraceEvent;
import org.eclipse.linuxtools.tmf.core.trace.text.TextTraceEventContent;

/**
 * System log trace implementation of TmfEvent.
 */
public class SyslogEvent extends TextTraceEvent implements ITmfCollapsibleEvent {

    /**
     * Default constructor
     */
    public SyslogEvent() {
        super(null, null, null, new SyslogEventType(), null, null);
    }

    /**
     * Copy constructor
     *
     * @param other
     *             the event to copy
     *
     */
    public SyslogEvent(final SyslogEvent other) {
        super(other);
    }

    /**
     * Full Constructor
     *
     * @param parentTrace
     *            the parent trace
     * @param timestamp
     *            the event timestamp
     * @param source
     *            the event source
     * @param type
     *            the event type
     * @param content
     *            the event content (payload)
     * @param reference
     *            the event reference
     */
    public SyslogEvent(SyslogTrace parentTrace, final ITmfTimestamp timestamp, final String source,
            final ITmfEventType type, final TextTraceEventContent content, final String reference) {
        super(parentTrace, timestamp, source, type, content, reference);
    }

    @Override
    public boolean isCollapsibleWith(ITmfEvent otherEvent) {
        if (this == otherEvent) {
            return true;
        }

        if (!(otherEvent instanceof SyslogEvent)) {
            return false;
        }

        final SyslogEvent other = (SyslogEvent) otherEvent;

        if (getTrace() == null) {
            if (other.getTrace() != null) {
                return false;
            }
        } else if (!getTrace().equals(other.getTrace())) {
            return false;
        }

        if (getType() == null) {
            if (other.getType() != null) {
                return false;
            }
        } else if (!getType().equals(other.getType())) {
            return false;
        }

        TextTraceEventContent content = this.getContent();
        TextTraceEventContent otherContent = other.getContent();

        if (content == null) {
            if (otherContent != null) {
                return false;
            }
            return true;
        }

        if (otherContent == null) {
            return false;
        }

        List<TextTraceEventContent> fields = content.getFields();
        List<TextTraceEventContent> otherFields = otherContent.getFields();
        int size = fields.size();

        if (size != otherFields.size()) {
            return false;
        }

        // At i = 0 the timestamp is stored and needs to be bypassed
        for (int i = 1; i < size; i++) {
            if (!fields.get(i).equals(otherFields.get(i))) {
                return false;
            }
        }
        return true;
    }

}
