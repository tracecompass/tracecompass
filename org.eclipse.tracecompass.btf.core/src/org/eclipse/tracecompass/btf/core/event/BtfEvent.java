/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.btf.core.event;

import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * A Btf event, basically a wrapper for the TmfEvent with the additional field
 * of "description"
 *
 * @author Matthew Khouzam
 */
public class BtfEvent extends TmfEvent {

    private final String fDescription;
    private final String fSource;
    private final String fReference;

    /**
     * Standard constructor.
     *
     * @param trace
     *            the parent trace
     * @param rank
     *            the event rank
     * @param timestamp
     *            the event timestamp
     * @param source
     *            the event source
     * @param type
     *            the event type
     * @param description
     *            a description of the type
     * @param content
     *            the event content (payload)
     * @param reference
     *            the event reference
     */
    public BtfEvent(final ITmfTrace trace,
            final long rank,
            final ITmfTimestamp timestamp,
            final String source,
            final ITmfEventType type,
            final String description,
            final ITmfEventField content,
            final String reference) {
        super(trace, rank, timestamp, type, content);
        fDescription = description;
        fSource = source;
        fReference = reference;
    }

    /**
     * Gets a description
     *
     * @return the description
     */
    public String getEventDescription() {
        return fDescription;
    }

    /**
     * Returns the source of this event.
     *
     * @return This event's source
     */
    public String getSource() {
        return fSource;
    }

    /**
     * Returns the reference of this event.
     *
     * @return This event's reference
     */
    public String getReference() {
        return fReference;
    }
}
