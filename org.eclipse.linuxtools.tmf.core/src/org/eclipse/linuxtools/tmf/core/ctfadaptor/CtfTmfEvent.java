/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;

/**
 * A wrapper class around CTF's Event Definition/Declaration that maps all
 * types of Declaration to native Java types.
 * 
 * @version 1.0
 * @author Alexandre Montplaisir
 */
public final class CtfTmfEvent implements ITmfEvent, Cloneable {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String NO_STREAM = "No stream"; //$NON-NLS-1$
    private static final String EMPTY_CTF_EVENT_NAME = "Empty CTF event"; //$NON-NLS-1$


    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final CtfTmfTrace fTrace;
    private final long timestamp;
    private final int sourceCPU;
    private final long typeId;
    private final String eventName;
    private final String fileName;

    private final CtfTmfContent fContent;

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
            this.timestamp = -1;
            this.sourceCPU = -1;
            this.typeId = -1;
            this.fileName = NO_STREAM;
            this.eventName = EMPTY_CTF_EVENT_NAME;
            this.fContent = null;
            return;
        }

        /* Read the base event info */
        Long offset = originTrace.getCTFTrace().getOffset();
        this.timestamp = eventDef.getTimestamp() + offset;
        this.sourceCPU = eventDef.getCPU();
        this.typeId = eventDef.getDeclaration().getId();
        this.eventName = eventDef.getDeclaration().getName();
        this.fileName =  fileName;

        /* Read the fields */
        this.fContent = new CtfTmfContent(ITmfEventField.ROOT_FIELD_ID,
                parseFields(eventDef));
    }

    /**
     * Extract the field information from the structDefinition haze-inducing
     * mess, and put them into something ITmfEventField can cope with.
     *
     * @param eventDef
     *            CTF EventDefinition to read
     * @return CtfTmfEventField[] The array of fields that were read
     */
    public static CtfTmfEventField[] parseFields(EventDefinition eventDef) {
        List<CtfTmfEventField> fields = new ArrayList<CtfTmfEventField>();

        StructDefinition structFields = eventDef.getFields();
        HashMap<String, Definition> definitions = structFields.getDefinitions();
        String curFieldName;
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

        return fields.toArray(new CtfTmfEventField[fields.size()]);
    }

    /**
     * Copy constructor
     *
     * @param other
     *            CtfTmfEvent to copy
     */
    public CtfTmfEvent(CtfTmfEvent other) {
        this.fTrace = other.getTrace();
        /* Primitives, those will be copied by value */
        this.timestamp = other.timestamp;
        this.sourceCPU = other.sourceCPU;
        this.typeId = other.typeId;

        /* Strings are immutable, it's safe to shallow-copy them */
        this.eventName = other.eventName;
        this.fileName = other.fileName;

        /* Copy the fields over */
        this.fContent = (CtfTmfContent) other.fContent.clone();
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
        this.timestamp = -1;
        this.sourceCPU = -1;
        this.typeId = -1;
        this.fileName = NO_STREAM;
        this.eventName = EMPTY_CTF_EVENT_NAME;
        this.fContent = new CtfTmfContent("", new CtfTmfEventField[0]); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    private static CtfTmfEvent nullEvent = null;

    /**
     * Get a null event
     *
     * @return an empty event. */
    public static CtfTmfEvent getNullEvent() {
        if (nullEvent == null) {
            nullEvent = new CtfTmfEvent();
        }
        return nullEvent;
    }

    /**
     * Gets the current timestamp of the event
     *
     * @return the current timestamp (long) */
    public long getTimestampValue() {
        return this.timestamp;
    }

    /**
     * Gets the cpu core the event was recorded on.
     *
     * @return the cpu id for a given source. In lttng it's from CPUINFO */
    public int getCPU() {
        return this.sourceCPU;
    }

    /**
     * Return this event's ID, according to the trace's metadata. Watch out,
     * this ID is not constant from one trace to another for the same event
     * types! Use "getEventName()" for a constant reference.
     *

     * @return the event ID */
    public long getID() {
        return this.typeId;
    }

    /**
     * Gets the name of a current event.
     *
     * @return the event name */
    public String getEventName() {
        return eventName;
    }

    /**
     * Gets the channel name of a field.
     *
     * @return the channel name. */
    public String getChannelName() {
        return this.fileName;
    }

    /**
     * Method getTrace.
     * @return CtfTmfTrace
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEvent#getTrace()
     */
    @Override
    public CtfTmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Method getRank.
     * @return long
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEvent#getRank()
     */
    @Override
    public long getRank() {
        // TODO Auto-generated method stub
        return 0;
    }

    private ITmfTimestamp fTimestamp = null;

    // TODO Benchmark if the singleton approach is faster than just
    // instantiating a final fTimestramp right away at creation time
    /**
     * Method getTimestamp.
     * @return ITmfTimestamp
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEvent#getTimestamp()
     */
    @Override
    public ITmfTimestamp getTimestamp() {
        if (fTimestamp == null) {
            fTimestamp = new CtfTmfTimestamp(timestamp);
        }
        return fTimestamp;
    }

    String fSource = null;
    /**
     * Method getSource.
     * @return String
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEvent#getSource()
     */
    @Override
    public String getSource() {
        // TODO Returns CPU for now
        if(fSource == null) {
            fSource= Integer.toString(getCPU());
        }
        return fSource;
    }

    /**
     * Method getType.
     * @return ITmfEventType
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEvent#getType()
     */
    @Override
    public ITmfEventType getType() {
        CtfTmfEventType ctfTmfEventType = CtfTmfEventType.get(eventName);
        if( ctfTmfEventType == null ){
            ctfTmfEventType = new CtfTmfEventType( this.getEventName(), this.getContent());
        }
        return ctfTmfEventType;
    }

    /**
     * Method getContent.
     * @return ITmfEventField
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEvent#getContent()
     */
    @Override
    public ITmfEventField getContent() {
        return fContent;
    }

    String fReference = null;
    /**
     * Method getReference.
     * @return String
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEvent#getReference()
     */
    @Override
    public String getReference() {
        if( fReference == null){
            fReference = getChannelName();
        }
        return fReference;
    }

    /**
     * Method clone.
     * @return CtfTmfEvent
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEvent#clone()
     */
    @Override
    public CtfTmfEvent clone() {
        return new CtfTmfEvent(this);
    }
}
