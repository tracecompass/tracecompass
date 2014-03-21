/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
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

package org.eclipse.linuxtools.tmf.ctf.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.CTFCallsite;
import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.IEventDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfCustomAttributes;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.lookup.ITmfModelLookup;
import org.eclipse.linuxtools.tmf.core.event.lookup.ITmfSourceLookup;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;

/**
 * A wrapper class around CTF's Event Definition/Declaration that maps all types
 * of Declaration to native Java types.
 *
 * @version 1.0
 * @author Alexandre Montplaisir
 * @since 2.0
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
    @NonNull
    private final EventDefinition fEvent;
    private ITmfEventField fContent;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor used by {@link CtfTmfEventFactory#createEvent}
     */
    CtfTmfEvent(CtfTmfTrace trace, long rank, CtfTmfTimestamp timestamp,
            String fileName, int cpu, IEventDeclaration declaration, @NonNull EventDefinition eventDefinition) {
        super(trace,
                rank,
                timestamp,
                String.valueOf(cpu), // Source
                null, // Event type. We don't use TmfEvent's field here, we
                      // re-implement getType()
                null, // Content handled with a lazy loaded re-implemented in
                      // getContent()
                fileName // Reference
        );

        fEventDeclaration = declaration;
        fSourceCPU = cpu;
        fTypeId = declaration.getId();
        fEventName = declaration.getName();
        fEvent = eventDefinition;

    }

    /**
     * Inner constructor to create "null" events. Don't use this directly in
     * normal usage, use {@link CtfTmfEventFactory#getNullEvent()} to get an
     * instance of an empty event.
     *
     * This needs to be public however because it's used in extension points,
     * and the framework will use this constructor to get the class type.
     */
    public CtfTmfEvent() {
        super(null,
                ITmfContext.UNKNOWN_RANK,
                new CtfTmfTimestamp(-1),
                null,
                null,
                new TmfEventField("", null, new CtfTmfEventField[0]), //$NON-NLS-1$
                null);
        fSourceCPU = -1;
        fTypeId = -1;
        fEventName = EMPTY_CTF_EVENT_NAME;
        fEventDeclaration = null;
        fEvent = EventDefinition.NULL_EVENT;
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
        CtfTmfEventType ctfTmfEventType = CtfTmfEventType.get(getTrace(), fEventName);
        if (ctfTmfEventType == null) {
            /* Should only return null the first time */
            ctfTmfEventType = new CtfTmfEventType(fEventName, getTrace(), getContent());
        }
        return ctfTmfEventType;
    }

    /**
     * @since 2.0
     */
    @Override
    public Set<String> listCustomAttributes() {
        if (fEventDeclaration == null) {
            return new HashSet<>();
        }
        return fEventDeclaration.getCustomAttributes();
    }

    /**
     * @since 2.0
     */
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
     * @since 2.0
     */
    @Override
    public CtfTmfCallsite getCallsite() {
        CTFCallsite callsite = null;
        CtfTmfTrace trace = getTrace();
        if (trace == null) {
            return null;
        }
        CTFTrace ctfTrace = trace.getCTFTrace();
        /* Should not happen, but it is a good check */
        if (ctfTrace == null) {
            return null;
        }
        if (getContent() != null) {
            ITmfEventField ipField = getContent().getField(CtfConstants.CONTEXT_FIELD_PREFIX + CtfConstants.IP_KEY);
            if (ipField != null && ipField.getValue() instanceof Long) {
                long ip = (Long) ipField.getValue();
                callsite = ctfTrace.getCallsite(fEventName, ip);
            }
        }
        if (callsite == null) {
            callsite = ctfTrace.getCallsite(fEventName);
        }
        if (callsite != null) {
            return new CtfTmfCallsite(callsite);
        }
        return null;
    }

    /**
     * @since 2.0
     */
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

        StructDefinition structFields = eventDef.getFields();
        if (structFields != null) {
            if (structFields.getFieldNames() != null) {
                for (String curFieldName : structFields.getFieldNames()) {
                    fields.add(CtfTmfEventField.parseField(structFields.getDefinition(curFieldName), curFieldName));
                }
            }
        }
        /* Add context information as CtfTmfEventField */
        StructDefinition structContext = eventDef.getContext();
        if (structContext != null) {
            for (String contextName : structContext.getFieldNames()) {
                /* Prefix field name */
                String curContextName = CtfConstants.CONTEXT_FIELD_PREFIX + contextName;
                fields.add(CtfTmfEventField.parseField(structContext.getDefinition(contextName), curContextName));
            }
        }

        return fields.toArray(new CtfTmfEventField[fields.size()]);
    }

}
