/*******************************************************************************
 * Copyright (c) 2011-2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.linuxtools.ctf.core.event.CTFCallsite;
import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.IEventDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventPropertySource;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * A wrapper class around CTF's Event Definition/Declaration that maps all
 * types of Declaration to native Java types.
 *
 * @version 1.0
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public final class CtfTmfEvent implements ITmfEvent, IAdaptable, Cloneable {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String NO_STREAM = "No stream"; //$NON-NLS-1$
    private static final String EMPTY_CTF_EVENT_NAME = "Empty CTF event"; //$NON-NLS-1$

    /** Prefix for Context information stored as CtfTmfEventfield */
    private static final String CONTEXT_FIELD_PREFIX = "context."; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final CtfTmfTrace fTrace;
    private final ITmfTimestamp fTimestamp;
    private final int sourceCPU;
    private final long typeId;
    private final String eventName;
    private final String fileName;

    private final TmfEventField fContent;
    private final IEventDeclaration fDeclaration;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Usual CTFEvent constructor, where we read an event from the trace (via
     * the StreamInputReader).
     *
     * @param eventDef
     *            CTF EventDefinition object corresponding to this trace event
     * @param fileName
     *            The path to the trace file
     * @param originTrace
     *            The trace from which this event originates
     */
    public CtfTmfEvent(EventDefinition eventDef, String fileName,
            CtfTmfTrace originTrace) {
        this.fTrace = originTrace;

        if (eventDef == null) {
            this.fTimestamp = new CtfTmfTimestamp(-1);
            this.sourceCPU = -1;
            this.typeId = -1;
            this.fileName = NO_STREAM;
            this.eventName = EMPTY_CTF_EVENT_NAME;
            this.fContent = null;
            this.fDeclaration = null;
            return;
        }

        /* Read the base event info */
        long ts = this.getTrace().getCTFTrace().timestampCyclesToNanos(eventDef.getTimestamp());
        this.fTimestamp = new CtfTmfTimestamp(ts);
        this.sourceCPU = eventDef.getCPU();
        this.typeId = eventDef.getDeclaration().getId();
        this.eventName = eventDef.getDeclaration().getName();
        this.fileName =  fileName;

        /* Read the fields */
        this.fContent = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, parseFields(eventDef));

        /* Keep a reference to this event's CTF declaration */
        this.fDeclaration = eventDef.getDeclaration();
    }

    /**
     * Extract the field information from the structDefinition haze-inducing
     * mess, and put them into something ITmfEventField can cope with.
     */
    private CtfTmfEventField[] parseFields(EventDefinition eventDef) {
        List<CtfTmfEventField> fields = new ArrayList<CtfTmfEventField>();

        StructDefinition structFields = eventDef.getFields();
        HashMap<String, Definition> definitions = structFields.getDefinitions();
        String curFieldName = null;
        Definition curFieldDef;
        CtfTmfEventField curField;
        Iterator<Entry<String, Definition>> it = definitions.entrySet().iterator();
        while(it.hasNext()) {
            Entry<String, Definition> entry = it.next();
            curFieldName = entry.getKey();
            curFieldDef = entry.getValue();
            curField = CtfTmfEventField.parseField(curFieldDef, curFieldName);
            fields.add(curField);
        }

        /* Add context information as CtfTmfEventField */
        long ip = -1;
        StructDefinition structContext = eventDef.getContext();
        if (structContext != null) {
            definitions = structContext.getDefinitions();
            String curContextName;
            Definition curContextDef;
            CtfTmfEventField curContext;
            it = definitions.entrySet().iterator();
            while(it.hasNext()) {
                Entry<String, Definition> entry = it.next();
                /* This is to get the instruction pointer if available */
                if (entry.getKey().equals("_ip") && //$NON-NLS-1$
                        (entry.getValue() instanceof IntegerDefinition)) {
                    ip = ((IntegerDefinition) entry.getValue()).getValue();
                }
                /* Prefix field name to */
                curContextName = CONTEXT_FIELD_PREFIX + entry.getKey();
                curContextDef = entry.getValue();
                curContext = CtfTmfEventField.parseField(curContextDef, curContextName);
                fields.add(curContext);
            }
        }
        /* Add callsite */
        final String name = eventDef.getDeclaration().getName();
        List<CTFCallsite> eventList = fTrace.getCTFTrace().getCallsiteCandidates(name);
        if (!eventList.isEmpty()) {
            final String callsite = "callsite"; //$NON-NLS-1$
            if (eventList.size() == 1 || ip == -1) {
                CTFCallsite cs = eventList.get(0);
                fields.add(new CTFStringField(cs.toString(), callsite));
            } else {
                fields.add(new CTFStringField(
                        fTrace.getCTFTrace().getCallsite(name, ip).toString(),
                        callsite));
            }
        }

        return fields.toArray(new CtfTmfEventField[fields.size()]);
    }

    /**
     * Copy constructor
     *
     * @param other
     *            CtfTmfEvent to copy
     */
    public CtfTmfEvent(CtfTmfEvent other) {
        /* There is only one reference to the trace, so we can shallow-copy it */
        this.fTrace = other.getTrace();

        /*
         * Copy the timestamp
         * FIXME This can be switched to a shallow-copy once timestamps are
         * made immutable.
         */
        this.fTimestamp = new CtfTmfTimestamp(other.fTimestamp.getValue());

        /* Primitives, those will be copied by value */
        this.sourceCPU = other.sourceCPU;
        this.typeId = other.typeId;

        /* Strings are immutable, it's safe to shallow-copy them */
        this.eventName = other.eventName;
        this.fileName = other.fileName;

        /* Copy the fields over */
        this.fContent = other.fContent.clone();

        /*
         * Copy the reference to the custom attributes (should be the same
         * object for all events of this type)
         */
        this.fDeclaration = other.fDeclaration;
    }

    /**
     * Inner constructor to create "null" events. Don't use this directly in
     * normal usage, use CtfTmfEvent.getNullEvent() to get an instance of an
     * empty event.
     *
     * This needs to be public however because it's used in extension points,
     * and the framework will use this constructor to get the class type.
     */
    public CtfTmfEvent() {
        this.fTrace = null;
        this.fTimestamp = new CtfTmfTimestamp(-1);
        this.sourceCPU = -1;
        this.typeId = -1;
        this.fileName = NO_STREAM;
        this.eventName = EMPTY_CTF_EVENT_NAME;
        this.fContent = new TmfEventField("", new CtfTmfEventField[0]); //$NON-NLS-1$
        this.fDeclaration = null;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    private static CtfTmfEvent nullEvent = new CtfTmfEvent();

    /**
     * Get a null event
     *
     * @return An empty event.
     */
    public static CtfTmfEvent getNullEvent() {
        return nullEvent;
    }

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

    /**
     * Gets the channel name of a field.
     *
     * @return The channel name.
     */
    public String getChannelName() {
        return this.fileName;
    }

    @Override
    public CtfTmfTrace getTrace() {
        return fTrace;
    }

    @Override
    public long getRank() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ITmfTimestamp getTimestamp() {
        return fTimestamp;
    }

    @Override
    public String getSource() {
        // TODO Returns CPU for now
        return Integer.toString(getCPU());
    }

    @Override
    public ITmfEventType getType() {
        CtfTmfEventType ctfTmfEventType = CtfTmfEventType.get(eventName);
        if( ctfTmfEventType == null ){
            ctfTmfEventType = new CtfTmfEventType( this.getEventName(), this.getContent());
        }
        return ctfTmfEventType;
    }

    @Override
    public ITmfEventField getContent() {
        return fContent;
    }

    @Override
    public String getReference() {
        return getChannelName();
    }

    /**
     * List the custom CTF attributes for events of this type.
     *
     * @return The list of custom attribute names. Should not be null, but could
     *         be empty.
     * @since 2.0
     */
    public Set<String> listCustomAttributes() {
        if (fDeclaration == null) {
            return new HashSet<String>();
        }
        return fDeclaration.getCustomAttributes();
    }

    /**
     * Get the value of a custom CTF attributes for this event's type.
     *
     * @param name
     *            Name of the the custom attribute
     * @return Value of this attribute, or null if there is no attribute with
     *         that name
     * @since 2.0
     */
    public String getCustomAttribute(String name) {
        if (fDeclaration == null) {
            return null;
        }
        return fDeclaration.getCustomAttribute(name);
    }

    @Override
    public CtfTmfEvent clone() {
        return new CtfTmfEvent(this);
    }

    /**
     * @since 2.0
     */
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySource.class) {
            return new TmfEventPropertySource(this);
        }
        return null;
    }
}
