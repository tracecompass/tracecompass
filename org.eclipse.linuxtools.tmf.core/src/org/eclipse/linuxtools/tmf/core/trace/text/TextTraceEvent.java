/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace.text;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;

/**
 * Class to store the common functionality of text trace events.
 *
 * @author Alexandre Montplaisir
 * @since 3.0
 */
public abstract class TextTraceEvent extends TmfEvent {

    /**
     * Full Constructor.
     *
     * Compared to {@link TmfEvent}'s constructor, 'content' is restricted to a
     * {@link TextTraceEventContent}.
     *
     * @param parentTrace
     *            The parent trace
     * @param timestamp
     *            The event timestamp
     * @param source
     *            The event source
     * @param type
     *            The event type
     * @param content
     *            The event content (payload)
     * @param reference
     *            The event reference
     */
    public TextTraceEvent(TextTrace<? extends TextTraceEvent> parentTrace,
            final ITmfTimestamp timestamp,
            final String source,
            final ITmfEventType type,
            final TextTraceEventContent content,
            final String reference) {
        super(parentTrace, timestamp, source, type, content, reference);
    }

    /**
     * Copy constructor
     *
     * @param other
     *            The event to copy
     */
    public TextTraceEvent(final TextTraceEvent other) {
        super(other);
    }

    @Override
    public TextTrace<? extends TextTraceEvent> getTrace() {
        /* Cast should be safe, type is restricted by the constructor */
        return (TextTrace<?>) super.getTrace();
    }

    @Override
    public TextTraceEventContent getContent() {
        /* Cast should be safe, type is restricted by the constructor */
        return (TextTraceEventContent) super.getContent();
    }
}
