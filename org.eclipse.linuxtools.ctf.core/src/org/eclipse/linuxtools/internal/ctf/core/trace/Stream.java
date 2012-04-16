/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core.trace;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.linuxtools.ctf.core.event.EventDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.internal.ctf.core.event.metadata.exceptions.ParseException;

/**
 * <b><u>Stream</u></b>
 * <p>
 * Represents a stream in a trace.
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
    private final HashMap<Long, EventDeclaration> events = new HashMap<Long, EventDeclaration>();

    /**
     * The inputs associated to this stream
     */
    private final Set<StreamInput> inputs = new HashSet<StreamInput>();

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

    public void setId(long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public boolean idIsSet() {
        return id != null;
    }

    public boolean eventHeaderIsSet() {
        return eventHeaderDecl != null;
    }

    public boolean eventContextIsSet() {
        return eventContextDecl != null;
    }

    public boolean packetContextIsSet() {
        return packetContextDecl != null;
    }

    public void setEventHeader(StructDeclaration eventHeader) {
        this.eventHeaderDecl = eventHeader;
    }

    public void setEventContext(StructDeclaration eventContext) {
        this.eventContextDecl = eventContext;
    }

    public void setPacketContext(StructDeclaration packetContext) {
        this.packetContextDecl = packetContext;
    }

    public StructDeclaration getEventHeaderDecl() {
        return eventHeaderDecl;
    }

    public StructDeclaration getEventContextDecl() {
        return eventContextDecl;
    }

    public StructDeclaration getPacketContextDecl() {
        return packetContextDecl;
    }

    public Set<StreamInput> getStreamInputs() {
        return inputs;
    }

    public CTFTrace getTrace() {
        return trace;
    }

    public HashMap<Long, EventDeclaration> getEvents() {
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
     *            The event to add.
     * @throws ParseException
     */
    public void addEvent(EventDeclaration event) throws ParseException {
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

        /* Put the event in the map */
        events.put(event.getId(), event);
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


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Stream [id=" + id + ", packetContextDecl=" + packetContextDecl //$NON-NLS-1$ //$NON-NLS-2$
                + ", eventHeaderDecl=" + eventHeaderDecl //$NON-NLS-1$
                + ", eventContextDecl=" + eventContextDecl + ", trace=" + trace //$NON-NLS-1$ //$NON-NLS-2$
                + ", events=" + events + ", inputs=" + inputs + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
