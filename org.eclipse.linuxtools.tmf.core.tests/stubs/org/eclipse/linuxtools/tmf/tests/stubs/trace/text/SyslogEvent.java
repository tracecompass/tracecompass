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

import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.text.TextTraceEvent;
import org.eclipse.linuxtools.tmf.core.trace.text.TextTraceEventContent;

/**
 * System log trace implementation of TmfEvent.
 */
public class SyslogEvent extends TextTraceEvent {

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

}
