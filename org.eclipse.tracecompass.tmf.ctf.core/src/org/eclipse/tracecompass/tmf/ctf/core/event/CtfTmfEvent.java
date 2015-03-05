/*******************************************************************************
 * Copyright (c) 2011, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexandre Montplaisir - Initial API and implementation
 *     Bernd Hufmann - Updated for source and model lookup interfaces
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.event.EventDefinition;
import org.eclipse.tracecompass.ctf.core.event.IEventDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.ICompositeDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IDefinition;
import org.eclipse.tracecompass.tmf.core.event.ITmfCustomAttributes;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfModelLookup;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfSourceLookup;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.ctf.core.CtfConstants;
import org.eclipse.tracecompass.tmf.ctf.core.event.lookup.CtfTmfCallsite;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;

/**
 * A wrapper class around CTF's Event Definition/Declaration that maps all types
 * of Declaration to native Java types.
 *
 * @author Alexandre Montplaisir
 */
public class CtfTmfEvent extends TmfEvent
        implements ITmfSourceLookup, ITmfModelLookup, ITmfCustomAttributes {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String EMPTY_CTF_EVENT_NAME = "Empty CTF event"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final int fSourceCPU;
    private final long fTypeId;
    private final String fEventName;
    private final IEventDeclaration fEventDeclaration;
    private final @NonNull EventDefinition fEvent;
    private final String fReference;

    /** Lazy-loaded field containing the event's payload */
    private ITmfEventField fContent;

    private CtfTmfEventType fCtfTmfEventType;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor used by {@link CtfTmfEventFactory#createEvent}
     */
    CtfTmfEvent(CtfTmfTrace trace, long rank, TmfNanoTimestamp timestamp,
            String fileName, int cpu, IEventDeclaration declaration, @NonNull EventDefinition eventDefinition) {
        super(trace,
                rank,
                timestamp,
                /*
                 * Event type. We don't use TmfEvent's field here, we
                 * re-implement getType().
                 */
                null,
                /*
                 * Content handled with a lazy-loaded field re-implemented in
                 * getContent().
                 */
                null);

        fEventDeclaration = declaration;
        fSourceCPU = cpu;
        fTypeId = declaration.getId().longValue();
        fEventName = declaration.getName();
        fEvent = eventDefinition;
        fReference = fileName;
    }

    /**
     * Inner constructor to create "null" events. Don't use this directly in
     * normal usage, use {@link CtfTmfEventFactory#getNullEvent(CtfTmfTrace)} to
     * get an instance of an empty event.
     *
     * There is no need to give higher visibility to this method than package
     * visible.
     *
     * @param trace
     *            The trace associated with this event
     */
    CtfTmfEvent(CtfTmfTrace trace) {
        super(trace,
                ITmfContext.UNKNOWN_RANK,
                new TmfNanoTimestamp(-1),
                null,
                new TmfEventField("", null, new CtfTmfEventField[0])); //$NON-NLS-1$
        fSourceCPU = -1;
        fTypeId = -1;
        fEventName = EMPTY_CTF_EVENT_NAME;
        fEventDeclaration = null;
        fEvent = EventDefinition.NULL_EVENT;
        fReference = null;
    }

    /**
     * Default constructor. Do not use directly, but it needs to be present
     * because it's used in extension points, and the framework will use this
     * constructor to get the class type.
     */
    public CtfTmfEvent() {
        this(null);
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Gets the cpu core the event was recorded on.
     *
     * @return The cpu id for a given source. In lttng it's from CPUINFO
     */
    public int getCPU() {
        return fSourceCPU;
    }

    /**
     * Return this event's ID, according to the trace's metadata.
     *
     * Watch out, this ID is not constant from one trace to another for the same
     * event types! Use "getEventName()" for a constant reference.
     *
     * @return The event ID
     */
    public long getID() {
        return fTypeId;
    }

    /**
     * Return this event's reference
     *
     * @return The event's reference
     */
    public String getReference() {
        return fReference;
    }

    @Override
    public CtfTmfTrace getTrace() {
        /*
         * Should be of the right type, since we take a CtfTmfTrace at the
         * constructor
         */
        return (CtfTmfTrace) super.getTrace();
    }

    @Override
    public ITmfEventType getType() {
        if (fCtfTmfEventType == null) {
            fCtfTmfEventType = new CtfTmfEventType(fEventName, getContent());

            /*
             * Register the event type in the owning trace, but only if there is
             * one
             */
            getTrace().registerEventType(fCtfTmfEventType);
        }
        return fCtfTmfEventType;
    }

    @Override
    public Set<String> listCustomAttributes() {
        if (fEventDeclaration == null) {
            return new HashSet<>();
        }
        return fEventDeclaration.getCustomAttributes();
    }

    @Override
    public String getCustomAttribute(String name) {
        if (fEventDeclaration == null) {
            return null;
        }
        return fEventDeclaration.getCustomAttribute(name);
    }

    /**
     * Get the call site for this event.
     *
     * @return the call site information, or null if there is none
     */
    @Override
    public CtfTmfCallsite getCallsite() {
        CtfTmfCallsite callsite = null;
        CtfTmfTrace trace = getTrace();

        if (getContent() != null) {
            ITmfEventField ipField = getContent().getField(CtfConstants.CONTEXT_FIELD_PREFIX + CtfConstants.IP_KEY);
            if (ipField != null && ipField.getValue() instanceof Long) {
                long ip = (Long) ipField.getValue();
                callsite = trace.getCallsite(fEventName, ip);
            }
        }
        if (callsite == null) {
            callsite = trace.getCallsite(fEventName);
        }
        return callsite;
    }

    @Override
    public String getModelUri() {
        return getCustomAttribute(CtfConstants.MODEL_URI_KEY);
    }

    @Override
    public synchronized ITmfEventField getContent() {
        if (fContent == null) {
            fContent = new TmfEventField(
                    ITmfEventField.ROOT_FIELD_ID, null, parseFields(fEvent));
        }
        return fContent;
    }

    /**
     * Extract the field information from the structDefinition haze-inducing
     * mess, and put them into something ITmfEventField can cope with.
     */
    private static CtfTmfEventField[] parseFields(@NonNull EventDefinition eventDef) {
        List<CtfTmfEventField> fields = new ArrayList<>();

        ICompositeDefinition structFields = eventDef.getFields();
        if (structFields != null) {
            if (structFields.getFieldNames() != null) {
                for (String curFieldName : structFields.getFieldNames()) {
                    fields.add(CtfTmfEventField.parseField((IDefinition) structFields.getDefinition(curFieldName), curFieldName));
                }
            }
        }
        /* Add context information as CtfTmfEventField */
        ICompositeDefinition structContext = eventDef.getContext();
        if (structContext != null) {
            for (String contextName : structContext.getFieldNames()) {
                /* Prefix field name */
                String curContextName = CtfConstants.CONTEXT_FIELD_PREFIX + contextName;
                fields.add(CtfTmfEventField.parseField((IDefinition) structContext.getDefinition(contextName), curContextName));
            }
        }

        return fields.toArray(new CtfTmfEventField[fields.size()]);
    }

}
