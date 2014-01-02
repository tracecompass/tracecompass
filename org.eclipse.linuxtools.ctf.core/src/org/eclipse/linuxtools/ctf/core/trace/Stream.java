/*******************************************************************************
 * Copyright (c) 2011-2013 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.trace;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.linuxtools.ctf.core.event.IEventDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.internal.ctf.core.event.EventDeclaration;
import org.eclipse.linuxtools.internal.ctf.core.event.metadata.exceptions.ParseException;

/**
 * <b><u>Stream</u></b>
 * <p>
 * Represents a stream in a trace.
 * @since 2.0
 */
public class Stream {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------


    /**
     * The numerical ID of the stream
     */
    private Long id = null;

    /**
     * Declarations of the stream-specific structures
     */
    private StructDeclaration packetContextDecl = null;
    private StructDeclaration eventHeaderDecl = null;
    private StructDeclaration eventContextDecl = null;

    /**
     * The trace to which the stream belongs
     */
    private CTFTrace trace = null;

    /**
     * Maps event ids to events
     */
    private Map<Long, IEventDeclaration> events = new HashMap<>();

    /**
     * The inputs associated to this stream
     */
    private final Set<StreamInput> inputs = new HashSet<>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a Stream that belongs to a Trace
     *
     * @param trace
     *            The trace to which belongs this stream.
     */
    public Stream(CTFTrace trace) {
        this.trace = trace;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Sets the id of a stream
     * @param id the id of a stream
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Gets the id of a stream
     * @return id the id of a stream
     */
    public Long getId() {
        return id;
    }

    /**
     * Is the id of a stream set
     *
     * @return If the ID is set or not
     */
    public boolean isIdSet() {
        return id != null;
    }

    /**
     *
     * @return is the event header set (timestamp and stuff) (see Ctf Spec)
     */
    public boolean isEventHeaderSet() {
        return eventHeaderDecl != null;
    }

    /**
    *
    * @return is the event context set (pid and stuff) (see Ctf Spec)
    */
    public boolean isEventContextSet() {
        return eventContextDecl != null;
    }

    /**
     *
     * @return Is the packet context set (see Ctf Spec)
     */
    public boolean isPacketContextSet() {
        return packetContextDecl != null;
    }

    /**
     *
     * @param eventHeader the current event header for all events in this stream
     */
    public void setEventHeader(StructDeclaration eventHeader) {
        this.eventHeaderDecl = eventHeader;
    }

    /**
     *
     * @param eventContext the context for all events in this stream
     */
    public void setEventContext(StructDeclaration eventContext) {
        this.eventContextDecl = eventContext;
    }

    /**
     *
     * @param packetContext the packet context for all packets in this stream
     */
    public void setPacketContext(StructDeclaration packetContext) {
        this.packetContextDecl = packetContext;
    }

    /**
     *
     * @return the event header declaration in structdeclaration form
     */
    public StructDeclaration getEventHeaderDecl() {
        return eventHeaderDecl;
    }

    /**
     *
     * @return the event context declaration in structdeclaration form
     */
    public StructDeclaration getEventContextDecl() {
        return eventContextDecl;
    }

    /**
     *
     * @return the packet context declaration in structdeclaration form
     */
    public StructDeclaration getPacketContextDecl() {
        return packetContextDecl;
    }

    /**
     *
     * @return the set of all stream inputs for this stream
     */
    public Set<StreamInput> getStreamInputs() {
        return inputs;
    }

    /**
     *
     * @return the parent trace
     */
    public CTFTrace getTrace() {
        return trace;
    }

    /**
     *
     * @return all the event declarations for this stream, using the id as a key for the hashmap.
     */
    public Map<Long, IEventDeclaration> getEvents() {
        return events;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Adds an event to the event map.
     *
     * An event in a stream can omit its id if it is the only event in this
     * stream. An event for which no id has been specified has a null id. It is
     * thus not possible to add an event with the null key if the map is not
     * empty. It is also not possible to add an event to the map if the null key
     * is present in the map.
     *
     * @param event
     *            The event to add
     * @throws ParseException
     *             If there was a problem reading the event or adding it to the
     *             stream
     */
    public void addEvent(IEventDeclaration event) throws ParseException {
        /*
         * If there is an event without id (the null key), it must be the only
         * one
         */
        if (events.get(null) != null) {
            throw new ParseException(
                    "Event without id with multiple events in a stream"); //$NON-NLS-1$
        }

        /*
         * If there is an event without id (the null key), it must be the only
         * one
         */
        if ((event.getId() == null) && (events.size() != 0)) {
            throw new ParseException(
                    "Event without id with multiple events in a stream"); //$NON-NLS-1$
        }

        /* Check if an event with the same ID already exists */
        if (events.get(event.getId()) != null) {
            throw new ParseException("Event id already exists"); //$NON-NLS-1$
        }
        if (event.getId() == null) {
            events.put(EventDeclaration.UNSET_EVENT_ID, event);
        } else {
            /* Put the event in the map */
            events.put(event.getId(), event);
        }
    }

    /**
     * Add an input to this Stream
     *
     * @param input
     *            The StreamInput to add.
     */
    public void addInput(StreamInput input) {
        inputs.add(input);
    }

    @Override
    public String toString() {
        return "Stream [id=" + id + ", packetContextDecl=" + packetContextDecl //$NON-NLS-1$ //$NON-NLS-2$
                + ", eventHeaderDecl=" + eventHeaderDecl //$NON-NLS-1$
                + ", eventContextDecl=" + eventContextDecl + ", trace=" + trace //$NON-NLS-1$ //$NON-NLS-2$
                + ", events=" + events + ", inputs=" + inputs + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
