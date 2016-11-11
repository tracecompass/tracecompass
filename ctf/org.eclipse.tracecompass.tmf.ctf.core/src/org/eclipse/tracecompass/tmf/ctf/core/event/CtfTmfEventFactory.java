/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.event;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFStrings;
import org.eclipse.tracecompass.ctf.core.event.IEventDeclaration;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;

/**
 * Factory for CtfTmfEvent's.
 *
 * This code was moved out of CtfTmfEvent to provide better separation between
 * the parsing/instantiation of events, and the usual TMF API implementations.
 *
 * @author Alexandre Montplaisir
 */
@NonNullByDefault
public class CtfTmfEventFactory {

    private static final CtfTmfEventFactory INSTANCE = new CtfTmfEventFactory();

    /**
     * The file name to use when none is specified.
     *
     * FIXME Externalize?
     *
     * @since 2.0
     */
    protected static final String NO_STREAM = "No stream"; //$NON-NLS-1$

    /**
     * Protected constructor, only for use by sub-classes. Users should call
     * the {@link #instance()} method instead.
     *
     * @since 2.0
     */
    protected CtfTmfEventFactory() {}

    /**
     * Get the singleton factory instance
     *
     * @return The instance
     * @since 2.0
     */
    public static CtfTmfEventFactory instance() {
        return INSTANCE;
    }

    /**
     * Factory method to instantiate new CTF events.
     *
     * @param trace
     *            The trace to which the new event will belong
     * @param eventDef
     *            CTF EventDefinition object corresponding to this trace event
     * @param fileName
     *            The path to the trace file
     * @return The newly-built CtfTmfEvent
     * @since 2.0
     */
    public CtfTmfEvent createEvent(CtfTmfTrace trace, IEventDefinition eventDef, @Nullable String fileName) {

        /* Prepare what to pass to CtfTmfEvent's constructor */
        final IEventDeclaration eventDecl = eventDef.getDeclaration();
        final long ts = eventDef.getTimestamp();
        final ITmfTimestamp timestamp = trace.createTimestamp(trace.timestampCyclesToNanos(ts));

        int sourceCPU = eventDef.getCPU();

        String reference = (fileName == null ? NO_STREAM : fileName);

        /* Handle the special case of lost events */
        if (eventDecl.getName().equals(CTFStrings.LOST_EVENT_NAME)) {
            return createLostEvent(trace, eventDef, eventDecl, ts, timestamp, sourceCPU, reference);
        }

        /* Handle standard event types */
        return new CtfTmfEvent(trace,
                ITmfContext.UNKNOWN_RANK,
                timestamp,
                reference, // filename
                sourceCPU,
                eventDecl,
                eventDef);
    }

    /**
     * Create a new CTF lost event.
     *
     * @param trace
     *            The trace to which the new event will belong
     * @param eventDef
     *            The CTF event definition
     * @param eventDecl
     *            The CTF event declaration
     * @param ts
     *            The event's timestamp
     * @param timestamp
     *            The event's timestamp (FIXME again??)
     * @param sourceCPU
     *            The source CPU
     * @param fileName
     *            The file name
     * @return The new lost event
     * @since 2.0
     */
    protected static CtfTmfEvent createLostEvent(CtfTmfTrace trace,
            IEventDefinition eventDef,
            final IEventDeclaration eventDecl,
            final long ts,
            final ITmfTimestamp timestamp,
            int sourceCPU,
            String fileName) {

        IDefinition nbLostEventsDef = eventDef.getFields().getDefinition(CTFStrings.LOST_EVENTS_FIELD);
        IDefinition durationDef = eventDef.getFields().getDefinition(CTFStrings.LOST_EVENTS_DURATION);
        if (!(nbLostEventsDef instanceof IntegerDefinition) || !(durationDef instanceof IntegerDefinition)) {
            /*
             * One or both of these fields doesn't exist, or is not of the right
             * type. The event claims to be a "lost event", but is malformed.
             * Log it and return a null event instead.
             */
            return getNullEvent(trace);
        }
        long nbLostEvents = ((IntegerDefinition) nbLostEventsDef).getValue();
        long duration = ((IntegerDefinition) durationDef).getValue();
        ITmfTimestamp timestampEnd = trace.createTimestamp(
                trace.timestampCyclesToNanos(ts) + trace.timestampCyclesToNanos(duration - trace.getOffset()));

        CtfTmfLostEvent lostEvent = new CtfTmfLostEvent(trace,
                ITmfContext.UNKNOWN_RANK,
                fileName,
                sourceCPU,
                eventDecl,
                new TmfTimeRange(timestamp, timestampEnd),
                nbLostEvents,
                eventDef);
        return lostEvent;
    }

    /**
     * Get an instance of a null event.
     *
     * @param trace
     *            The trace to which the new null event will belong
     * @return An empty event
     * @since 2.0
     */
    public static CtfTmfEvent getNullEvent(CtfTmfTrace trace) {
        return new CtfTmfEvent(trace);
    }


}
