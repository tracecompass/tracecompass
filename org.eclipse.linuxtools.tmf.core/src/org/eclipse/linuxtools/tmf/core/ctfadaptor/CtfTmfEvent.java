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
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.trace.StreamInputReader;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;

/**
 * <b><u>CTFEvent</u></b>
 * <p>
 * This is a wrapper class around CTF's Event Definition/Declaration so that we
 * can map all types of Declaration to native Java types.
 */
public final class CtfTmfEvent implements ITmfEvent {

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

    private final TmfEventField fContent;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Usual CTFEvent constructor, where we read an event from the trace (via
     * the StreamInputReader).
     *
     * @param eventDef
     * @param top
     */
    public CtfTmfEvent(EventDefinition eventDef, StreamInputReader top,
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
        // FIXME restore once the CTF parser with clocks gets merged
        //Long offset = originTrace.getCTFTrace().getOffset();
        Long offset = 0L;
        this.timestamp = eventDef.timestamp + offset;
        this.sourceCPU = eventDef.getCPU();
        this.typeId = eventDef.getDeclaration().getId();
        this.eventName = eventDef.getDeclaration().getName();
        this.fileName = top.getStreamInput().getFilename();

        /* Read the fields */
        this.fContent = new TmfEventField(ITmfEventField.ROOT_FIELD_ID,
                parseFields(eventDef));
    }

    /**
     * Extract the field information from the structDefinition haze-inducing
     * mess, and put them into something ITmfEventField can cope with.
     *
     * @param eventDef
     * @return
     */
    private static CtfTmfEventField[] parseFields(EventDefinition eventDef) {
        List<CtfTmfEventField> fields = new ArrayList<CtfTmfEventField>();

        StructDefinition structFields = eventDef.getFields();
        HashMap<String, Definition> definitions = structFields.getDefinitions();
        String curFieldName;
        Definition curFieldDef;
        CtfTmfEventField curField;

        for (Entry<String, Definition> entry : definitions.entrySet()) {
            curFieldName = entry.getKey();
            curFieldDef = entry.getValue();
            curField = CtfTmfEventField.parseField(curFieldDef, curFieldName);
            if (curField == null) {
//                TmfCorePlugin.getDefault().log(
//                        "We've parsed an unimplemented field type for event \"" + this.eventName //$NON-NLS-1$
//                                + "\", field \"" + curFieldName + "\" of type " + curFieldDef.getClass().toString()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            fields.add(curField);
        }

        return fields.toArray(new CtfTmfEventField[fields.size()]);
    }

    /**
     * Copy constructor
     *
     * @param other
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
        this.fContent = other.fContent.clone();
    }

    /**
     * Inner constructor to create "null" events. Don't use this directly, use
     * CTFEvent.getNullEvent();
     */
    private CtfTmfEvent() {
        this.fTrace = null;
        this.timestamp = -1;
        this.sourceCPU = -1;
        this.typeId = -1;
        this.fileName = NO_STREAM;
        this.eventName = EMPTY_CTF_EVENT_NAME;
        this.fContent = null;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    private static CtfTmfEvent nullEvent = null;

    /**
     * Get a null event
     *
     * @return an empty event.
     */
    public static CtfTmfEvent getNullEvent() {
        if (nullEvent == null) {
            nullEvent = new CtfTmfEvent();
        }
        return nullEvent;
    }

    /**
     * Gets the current timestamp of the event
     *
     * @return the current timestamp (long)
     */
    public long getTimestampValue() {
        return this.timestamp;
    }

    /**
     * Gets the cpu core the event was recorded on.
     *
     * @return the cpu id for a given source. In lttng it's from CPUINFO
     */
    public int getCPU() {
        return this.sourceCPU;
    }

    /**
     * Return this event's ID, according to the trace's metadata. Watch out,
     * this ID is not constant from one trace to another for the same event
     * types! Use "getEventName()" for a constant reference.
     *
     * @return the event ID
     */
    public long getID() {
        return this.typeId;
    }

    /**
     * Gets the name of a current event.
     *
     * @return the event name
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Gets the channel name of a field.
     *
     * @return the channel name.
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

    private ITmfTimestamp fTimestamp = null;

    // TODO Benchmark if the singleton approach is faster than just
    // instantiating a final fTimestramp right away at creation time
    @Override
    public ITmfTimestamp getTimestamp() {
        if (fTimestamp == null) {
            fTimestamp = new TmfTimestamp(timestamp);
        }
        return fTimestamp;
    }

    @Override
    public String getSource() {
        // TODO Returns eventName for now
        return eventName;
    }

    @Override
    public ITmfEventType getType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ITmfEventField getContent() {
        return fContent;
    }

    @Override
    public String getReference() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CtfTmfEvent clone() {
        return new CtfTmfEvent(this);
    }
}
