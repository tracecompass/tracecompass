/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi    - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.CTFStrings;
import org.eclipse.tracecompass.ctf.core.event.CTFCallsite;
import org.eclipse.tracecompass.ctf.core.event.IEventDeclaration;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.ILexicalScope;
import org.eclipse.tracecompass.ctf.core.event.types.Definition;
import org.eclipse.tracecompass.ctf.core.event.types.ICompositeDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;
import org.eclipse.tracecompass.ctf.core.trace.CTFIOException;
import org.eclipse.tracecompass.ctf.core.trace.CTFStreamInputReader;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.ICTFPacketDescriptor;
import org.eclipse.tracecompass.internal.ctf.core.event.types.composite.EventHeaderDefinition;
import org.eclipse.tracecompass.internal.ctf.core.trace.CTFStream;

import com.google.common.collect.ImmutableList;

/**
 * Representation of one type of event. A bit like "int" or "long" but for trace
 * events.
 */
public class EventDeclaration implements IEventDeclaration {

    private static final Comparator<CTFCallsite> CS_COMPARATOR = (o1, o2) -> Long.compareUnsigned(o1.getIp(), o2.getIp());

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Name of the event
     */
    private String fName;

    /**
     * Event context structure declaration
     */
    private StructDeclaration fContext = null;

    /**
     * Event fields structure declaration
     */
    private StructDeclaration fFields = null;

    /**
     * Stream to which belongs this event.
     */
    private @Nullable CTFStream fStream = null;

    /**
     * Loglevel of an event
     */
    private long fLogLevel;

    /** Map of this event type's custom CTF attributes */
    private final Map<String, String> fCustomAttributes = new HashMap<>();

    private final @NonNull List<@NonNull CTFCallsite> fCallsites = new ArrayList<>();

    private int fId = (int) UNSET_EVENT_ID;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor. Use the setters afterwards to set the fields
     * accordingly.
     */
    public EventDeclaration() {
    }

    /**
     * Creates an instance of EventDefinition corresponding to this declaration.
     *
     * @param streamEventContextDecl
     *            event context
     * @param packetDescriptor
     *            current packet
     * @param packetContext
     *            packet context
     * @param eventHeaderDef
     *            The event header definition
     * @param input
     *            the bitbuffer input source
     * @param prevTimestamp
     *            The timestamp when the event was taken
     * @return A new EventDefinition.
     * @throws CTFException
     *             As a bitbuffer is used to read, it could have wrapped
     *             IOExceptions.
     */
    public EventDefinition createDefinition(StructDeclaration streamEventContextDecl, ICTFPacketDescriptor packetDescriptor, ICompositeDefinition packetContext, ICompositeDefinition eventHeaderDef, @NonNull BitBuffer input, long prevTimestamp)
            throws CTFException {
        final CTFStream stream = fStream;
        final CTFTrace trace = stream == null ? null : stream.getTrace();
        StructDefinition streamEventContext = streamEventContextDecl != null ? streamEventContextDecl.createDefinition(trace, ILexicalScope.STREAM_EVENT_CONTEXT, input) : null;
        StructDefinition eventContext = fContext != null ? fContext.createFieldDefinition(eventHeaderDef, trace, ILexicalScope.CONTEXT, input) : null;
        StructDefinition eventPayload = fFields != null ? fFields.createFieldDefinition(eventHeaderDef, trace, ILexicalScope.FIELDS, input) : null;
        long timestamp = calculateTimestamp(eventHeaderDef, prevTimestamp, eventPayload, eventContext);

        int cpu = (int) packetDescriptor.getTargetId();
        return new EventDefinition(
                this,
                cpu,
                timestamp,
                eventHeaderDef,
                streamEventContext,
                eventContext,
                packetContext,
                eventPayload,
                packetDescriptor);
    }

    private static long calculateTimestamp(@Nullable ICompositeDefinition eventHeaderDef, long prevTimestamp, StructDefinition eventPayload, StructDefinition eventContext) throws CTFIOException {
        long timestamp = 0;
        Definition def = null;
        if (eventHeaderDef instanceof EventHeaderDefinition) {
            EventHeaderDefinition eventHeaderDefinition = (EventHeaderDefinition) eventHeaderDef;
            timestamp = calculateTimestamp(eventHeaderDefinition.getTimestamp(), eventHeaderDefinition.getTimestampLength(), prevTimestamp);
            def = eventHeaderDefinition;
        } else if (eventHeaderDef instanceof StructDefinition) {
            StructDefinition structDefinition = (StructDefinition) eventHeaderDef;
            def = structDefinition.lookupDefinition(CTFStrings.TIMESTAMP);
        } else if (eventHeaderDef != null) {
            throw new CTFIOException("Event header def is not a Struct or an Event Header"); //$NON-NLS-1$
        }
        if (def == null && eventPayload != null) {
            def = eventPayload.lookupDefinition(CTFStrings.TIMESTAMP);
        }
        if (def == null && eventContext != null) {
            def = eventContext.lookupDefinition(CTFStrings.TIMESTAMP);
        }
        if (def instanceof IntegerDefinition) {
            IntegerDefinition timestampDef = (IntegerDefinition) def;
            timestamp = calculateTimestamp(timestampDef, prevTimestamp);
        }
        return timestamp;
    }

    @Override
    public EventDefinition createDefinition(CTFStreamInputReader streamInputReader, @NonNull BitBuffer input, long timestamp) throws CTFException {
        StructDeclaration streamEventContextDecl = streamInputReader.getStreamEventContextDecl();
        final @Nullable CTFStream stream = fStream;
        final CTFTrace trace = stream == null ? null : stream.getTrace();
        StructDefinition streamEventContext = streamEventContextDecl != null ? streamEventContextDecl.createDefinition(trace, ILexicalScope.STREAM_EVENT_CONTEXT, input) : null;
        ICompositeDefinition packetContext = streamInputReader.getCurrentPacketReader().getCurrentPacketEventHeader();
        StructDefinition eventContext = fContext != null ? fContext.createDefinition(trace, ILexicalScope.CONTEXT, input) : null;
        StructDefinition eventPayload = fFields != null ? fFields.createDefinition(trace, ILexicalScope.FIELDS, input) : null;

        // a bit lttng specific
        // CTF doesn't require a timestamp,
        // but it's passed to us
        return new EventDefinition(
                this,
                streamInputReader.getCPU(),
                timestamp,
                null,
                streamEventContext,
                eventContext,
                packetContext,
                eventPayload,
                streamInputReader.getCurrentPacketReader().getCurrentPacket());
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Sets a name for an event Declaration
     *
     * @param name
     *            the name
     */
    public void setName(String name) {
        fName = name;
    }

    @Override
    public String getName() {
        return fName;
    }

    /**
     * Sets the context for an event declaration (see CTF specification)
     *
     * @param context
     *            the context in structdeclaration format
     */
    public void setContext(StructDeclaration context) {
        fContext = context;
    }

    /**
     * Sets the fields of an event declaration
     *
     * @param fields
     *            the fields in structdeclaration format
     */
    public void setFields(StructDeclaration fields) {
        fFields = fields;
    }

    @Override
    public StructDeclaration getFields() {
        return fFields;
    }

    @Override
    public StructDeclaration getContext() {
        return fContext;
    }

    /**
     * Sets the id of an event declaration
     *
     * @param id
     *            the id
     */
    public void setId(long id) {
        if (id < 0 || id > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("id out of range"); //$NON-NLS-1$
        }
        fId = (int) id;
    }

    @Override
    public Long getId() {
        return Long.valueOf(fId);
    }

    /**
     * Faster get id assuming you have less than a billion event types
     *
     * @return the event id
     */
    public int id() {
        return fId;
    }

    /**
     * Sets the stream of an event declaration
     *
     * @param stream
     *            the stream
     */
    public void setStream(CTFStream stream) {
        fStream = stream;
    }

    @Override
    public CTFStream getStream() {
        return fStream;
    }

    /**
     * Is the name of the event declaration set
     *
     * @return is the name set?
     */
    public boolean nameIsSet() {
        return fName != null;
    }

    /**
     * Is the context set
     *
     * @return is the context set
     */
    public boolean contextIsSet() {
        return fContext != null;
    }

    /**
     * Is a field set?
     *
     * @return Is the field set?
     */
    public boolean fieldsIsSet() {
        return fFields != null;
    }

    /**
     * Is the id set?
     *
     * @return is the id set?
     */
    public boolean idIsSet() {
        return (fId != UNSET_EVENT_ID);
    }

    /**
     * Is the stream set?
     *
     * @return is the stream set?
     */
    public boolean streamIsSet() {
        return fStream != null;
    }

    @Override
    public long getLogLevel() {
        return fLogLevel;
    }

    /**
     * Sets the log level
     *
     * @param level
     *            the log level
     */
    public void setLogLevel(long level) {
        fLogLevel = level;
    }

    @Override
    public Set<String> getCustomAttributes() {
        return fCustomAttributes.keySet();
    }

    @Override
    public String getCustomAttribute(String key) {
        return fCustomAttributes.get(key);
    }

    /**
     * Sets a custom attribute value.
     *
     * @param key
     *            the key of the attribute
     * @param value
     *            the value of the attribute
     */
    public void setCustomAttribute(String key, String value) {
        fCustomAttributes.put(key, value);
    }

    /**
     * Calculates the timestamp value of the event, possibly using the timestamp
     * from the last event.
     *
     * @param timestampDef
     *            Integer definition of the timestamp.
     * @return The calculated timestamp value.
     */
    private static long calculateTimestamp(IntegerDefinition timestampDef, long lastTimestamp) {
        int len = timestampDef.getDeclaration().getLength();
        final long value = timestampDef.getValue();

        return calculateTimestamp(value, len, lastTimestamp);
    }

    private static long calculateTimestamp(final long value, int len, long prevTimestamp) {
        long newval;
        long majorasbitmask;
        long lastTimestamp = prevTimestamp;
        /*
         * If the timestamp length is 64 bits, it is a full timestamp.
         */
        if (len == Long.SIZE) {
            lastTimestamp = value;
            return lastTimestamp;
        }

        /*
         * Bit mask to keep / remove all old / new bits.
         */
        majorasbitmask = (1L << len) - 1;

        /*
         * If the new value is smaller than the corresponding bits of the last
         * timestamp, we assume an overflow of the compact representation.
         */
        newval = value;
        if (newval < (lastTimestamp & majorasbitmask)) {
            newval = newval + (1L << len);
        }

        /* Keep only the high bits of the old value */
        lastTimestamp = lastTimestamp & ~majorasbitmask;

        /* Then add the low bits of the new value */
        lastTimestamp = lastTimestamp + newval;

        return lastTimestamp;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof EventDeclaration)) {
            return false;
        }
        EventDeclaration other = (EventDeclaration) obj;
        if (fId != (other.fId)) {
            return false;
        }
        if (!Objects.equals(fContext, other.fContext)) {
            return false;
        }
        if (!Objects.equals(fFields, other.fFields)) {
            return false;
        }
        if (!Objects.equals(fName, other.fName)) {
            return false;
        }
        if (!Objects.equals(fStream, other.fStream)) {
            return false;
        }
        if (!fCustomAttributes.equals(other.fCustomAttributes)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result)
                + ((fContext == null) ? 0 : fContext.hashCode());
        result = (prime * result) + ((fFields == null) ? 0 : fFields.hashCode());
        result = (prime * result) + fId;
        result = (prime * result) + ((fName == null) ? 0 : fName.hashCode());
        final CTFStream stream = fStream;
        result = (prime * result) + ((stream == null) ? 0 : stream.hashCode());
        result = (prime * result) + fCustomAttributes.hashCode();
        return result;
    }

    /**
     * Add static callsites to an event
     *
     * @param callsites
     *            the callsites to add
     */
    public void addCallsites(List<@NonNull CTFCallsite> callsites) {
        fCallsites.addAll(callsites);
        Collections.sort(fCallsites, CS_COMPARATOR);
    }

    @Override
    public List<@NonNull CTFCallsite> getCallsites() {
        return ImmutableList.copyOf(fCallsites);
    }

}
