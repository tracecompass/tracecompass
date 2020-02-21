/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace.text;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;

/**
 * Class to store the common functionality of text trace events.
 *
 * @author Alexandre Montplaisir
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
     * @param type
     *            The event type
     * @param content
     *            The event content (payload)
     */
    public TextTraceEvent(TextTrace<? extends TextTraceEvent> parentTrace,
            final ITmfTimestamp timestamp,
            final ITmfEventType type,
            final TextTraceEventContent content) {
        super(parentTrace, ITmfContext.UNKNOWN_RANK, timestamp, type, content);
    }

    /**
     * Copy constructor
     *
     * @param other
     *            The event to copy
     */
    public TextTraceEvent(final @NonNull TextTraceEvent other) {
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
