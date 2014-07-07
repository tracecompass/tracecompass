/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ctf.core;

import org.eclipse.linuxtools.ctf.core.CTFStrings;
import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.IEventDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;

/**
 * Factory for CtfTmfEvent's.
 *
 * This code was moved out of CtfTmfEvent to provide better separation between
 * the parsing/instantiation of events, and the usual TMF API implementations.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public final class CtfTmfEventFactory {

    private static final String NO_STREAM = "No stream"; //$NON-NLS-1$

    /**
     * Don't let anyone instantiate this class.
     */
    private CtfTmfEventFactory() {}

    /**
     * Factory method to instantiate new {@link CtfTmfEvent}'s.
     *
     * @param eventDef
     *            CTF EventDefinition object corresponding to this trace event
     * @param fileName
     *            The path to the trace file
     * @param originTrace
     *            The trace from which this event originates
     * @return The newly-built CtfTmfEvent
     */
    public static CtfTmfEvent createEvent(EventDefinition eventDef,
            String fileName, CtfTmfTrace originTrace) {

        /* Prepare what to pass to CtfTmfEvent's constructor */
        final IEventDeclaration eventDecl = eventDef.getDeclaration();
        final long ts = eventDef.getTimestamp();
        final CtfTmfTimestamp timestamp = originTrace.createTimestamp(
                originTrace.getCTFTrace().timestampCyclesToNanos(ts));

        int sourceCPU = eventDef.getCPU();

        String reference = fileName == null ? NO_STREAM : fileName;

        /* Handle the special case of lost events */
        if (eventDecl.getName().equals(CTFStrings.LOST_EVENT_NAME)) {
            IDefinition nbLostEventsDef = eventDef.getFields().getDefinition(CTFStrings.LOST_EVENTS_FIELD);
            IDefinition durationDef = eventDef.getFields().getDefinition(CTFStrings.LOST_EVENTS_DURATION);
            if (!(nbLostEventsDef instanceof IntegerDefinition) || !(durationDef instanceof IntegerDefinition)) {
                /*
                 * One or both of these fields doesn't exist, or is not of the
                 * right type. The event claims to be a "lost event", but is
                 * malformed. Log it and return a null event instead.
                 */
                return getNullEvent();
            }
            long nbLostEvents = ((IntegerDefinition) nbLostEventsDef).getValue();
            long duration = ((IntegerDefinition) durationDef).getValue();
            CtfTmfTimestamp timestampEnd = new CtfTmfTimestamp(
                    originTrace.getCTFTrace().timestampCyclesToNanos(ts) + duration);

            CtfTmfLostEvent lostEvent = new CtfTmfLostEvent(originTrace,
                    ITmfContext.UNKNOWN_RANK,
                    reference, // filename
                    sourceCPU,
                    eventDecl,
                    new TmfTimeRange(timestamp, timestampEnd),
                    nbLostEvents,
                    eventDef);
            return lostEvent;
        }

        /* Handle standard event types */
        CtfTmfEvent event = new CtfTmfEvent(
                originTrace,
                ITmfContext.UNKNOWN_RANK,
                timestamp,
                reference, // filename
                sourceCPU,
                eventDecl,
                eventDef);
        return event;
    }

    /* Singleton instance of a null event */
    private static CtfTmfEvent nullEvent = null;

    /**
     * Get an instance of a null event.
     *
     * @return An empty event
     */
    public static CtfTmfEvent getNullEvent() {
        if (nullEvent == null) {
            nullEvent = new CtfTmfEvent();
        }
        return nullEvent;
    }


}
