/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Updated for TMF 2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.gdbtrace.core.event;

import org.eclipse.tracecompass.internal.gdbtrace.core.trace.GdbTrace;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;

/**
 * GDB Trace implementation of TmfEvent
 * @author Francois Chouinard
 */
public class GdbTraceEvent extends TmfEvent {

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    private final String fSource;
    private final String fReference;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public GdbTraceEvent() {
        super(null, ITmfContext.UNKNOWN_RANK, null, null, null);
        fSource = null;
        fReference = null;
    }

    /**
     * Full constructor
     *
     * @param trace
     *            the parent trace
     * @param timestamp
     *            the event timestamp
     * @param source
     *            the event source
     * @param type
     *            the event type
     * @param content
     *            the event content
     * @param reference
     *            the event reference
     */
    public GdbTraceEvent(GdbTrace trace, ITmfTimestamp timestamp, String source,
            ITmfEventType type, GdbTraceEventContent content, String reference) {
        super(trace, ITmfContext.UNKNOWN_RANK, timestamp, type, content);
        fSource = source;
        fReference = reference;
    }

    /**
     * Return the event's source
     *
     * @return The event source
     */
    public String getSource() {
        return fSource;
    }

    /**
     * Return the event's reference
     *
     * @return The event reference
     */
    public String getReference() {
        return fReference;
    }

    @Override
    public GdbTraceEventContent getContent() {
        /* We only allow GdbTraceEventContent at the constructor */
        return (GdbTraceEventContent) super.getContent();
    }

}
