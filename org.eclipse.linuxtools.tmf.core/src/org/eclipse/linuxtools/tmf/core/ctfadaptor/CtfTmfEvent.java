/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
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

package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.linuxtools.ctf.core.event.CTFCallsite;
import org.eclipse.linuxtools.ctf.core.event.IEventDeclaration;
import org.eclipse.linuxtools.tmf.core.event.ITmfCustomAttributes;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.lookup.ITmfModelLookup;
import org.eclipse.linuxtools.tmf.core.event.lookup.ITmfSourceLookup;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;

/**
 * A wrapper class around CTF's Event Definition/Declaration that maps all
 * types of Declaration to native Java types.
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

    static final String NO_STREAM = "No stream"; //$NON-NLS-1$
    private static final String EMPTY_CTF_EVENT_NAME = "Empty CTF event"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final int sourceCPU;
    private final long typeId;
    private final String eventName;
    private final IEventDeclaration fDeclaration;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor used by {@link CtfTmfEventFactory#createEvent}
     */
    CtfTmfEvent(CtfTmfTrace trace, long rank, CtfTmfTimestamp timestamp,
            ITmfEventField content, String fileName, int cpu,
            IEventDeclaration declaration) {
        super(trace,
                rank,
                timestamp,
                String.valueOf(cpu), // Source
                null, // Event type. We don't use TmfEvent's field here, we re-implement getType()
                content,
                fileName // Reference
        );

        fDeclaration = declaration;
        sourceCPU = cpu;
        typeId = declaration.getId();
        eventName = declaration.getName();

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
                NO_STREAM);
        this.sourceCPU = -1;
        this.typeId = -1;
        this.eventName = EMPTY_CTF_EVENT_NAME;
        this.fDeclaration = null;
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
        return this.sourceCPU;
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
        return this.typeId;
    }

    /**
     * Gets the name of a current event.
     *
     * @return The event name
     */
    public String getEventName() {
        return eventName;
    }

    @Override
    public CtfTmfTrace getTrace() {
        /* Should be of the right type, since we take a CtfTmfTrace at the constructor */
        return (CtfTmfTrace) super.getTrace();
    }

    @Override
    public ITmfEventType getType() {
        CtfTmfEventType ctfTmfEventType = CtfTmfEventType.get(eventName);
        if (ctfTmfEventType == null) {
            /* Should only return null the first time */
            ctfTmfEventType = new CtfTmfEventType(this.getEventName(), this.getContent());
        }
        return ctfTmfEventType;
    }

    /**
     * @since 2.0
     */
    @Override
    public Set<String> listCustomAttributes() {
        if (fDeclaration == null) {
            return new HashSet<String>();
        }
        return fDeclaration.getCustomAttributes();
    }

    /**
     * @since 2.0
     */
    @Override
    public String getCustomAttribute(String name) {
        if (fDeclaration == null) {
            return null;
        }
        return fDeclaration.getCustomAttribute(name);
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
        if (getTrace() == null) {
            return null;
        }
        if (getContent() != null) {
            ITmfEventField ipField = getContent().getField(CtfConstants.CONTEXT_FIELD_PREFIX + CtfConstants.IP_KEY);
            if (ipField != null && ipField.getValue() instanceof Long) {
                long ip = (Long) ipField.getValue();
                callsite = getTrace().getCTFTrace().getCallsite(eventName, ip);
            }
        }
        if (callsite == null) {
            callsite = getTrace().getCTFTrace().getCallsite(eventName);
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

}
