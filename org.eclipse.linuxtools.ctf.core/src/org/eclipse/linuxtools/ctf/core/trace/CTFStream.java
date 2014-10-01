/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.ctf.core.event.IEventDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IEventHeaderDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.internal.ctf.core.event.EventDeclaration;
import org.eclipse.linuxtools.internal.ctf.core.event.metadata.exceptions.ParseException;

/**
 * <b><u>Stream</u></b>
 * <p>
 * Represents a stream in a trace.
 *
 * @since 3.0
 */
public class CTFStream {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The numerical ID of the stream
     */
    private Long fId = null;

    /**
     * Declarations of the stream-specific structures
     */
    private StructDeclaration fPacketContextDecl = null;
    private IDeclaration fEventHeaderDecl = null;
    private StructDeclaration fEventContextDecl = null;

    /**
     * The trace to which the stream belongs
     */
    private CTFTrace fTrace = null;

    /**
     * Maps event ids to events
     */
    private final ArrayList<IEventDeclaration> fEvents = new ArrayList<>();

    private boolean fEventUnsetId = false;

    /**
     * The inputs associated to this stream
     */
    private final Set<CTFStreamInput> fInputs = new HashSet<>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a Stream that belongs to a Trace
     *
     * @param trace
     *            The trace to which belongs this stream.
     */
    public CTFStream(CTFTrace trace) {
        fTrace = trace;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Sets the id of a stream
     *
     * @param id
     *            the id of a stream
     */
    public void setId(long id) {
        fId = id;
    }

    /**
     * Gets the id of a stream
     *
     * @return id the id of a stream
     */
    public Long getId() {
        return fId;
    }

    /**
     * Is the id of a stream set
     *
     * @return If the ID is set or not
     */
    public boolean isIdSet() {
        return fId != null;
    }

    /**
     *
     * @return is the event header set (timestamp and stuff) (see Ctf Spec)
     */
    public boolean isEventHeaderSet() {
        return fEventHeaderDecl != null;
    }

    /**
     *
     * @return is the event context set (pid and stuff) (see Ctf Spec)
     */
    public boolean isEventContextSet() {
        return fEventContextDecl != null;
    }

    /**
     *
     * @return Is the packet context set (see Ctf Spec)
     */
    public boolean isPacketContextSet() {
        return fPacketContextDecl != null;
    }

    /**
     * Sets the event header
     *
     * @param eventHeader
     *            the current event header for all events in this stream
     */
    public void setEventHeader(StructDeclaration eventHeader) {
        fEventHeaderDecl = eventHeader;
    }

    /**
     * Sets the event header, this typically has the id and the timestamp
     *
     * @param eventHeader
     *            the current event header for all events in this stream
     * @since 3.1
     */
    public void setEventHeader(IEventHeaderDeclaration eventHeader) {
        fEventHeaderDecl = eventHeader;
    }

    /**
     *
     * @param eventContext
     *            the context for all events in this stream
     */
    public void setEventContext(StructDeclaration eventContext) {
        fEventContextDecl = eventContext;
    }

    /**
     *
     * @param packetContext
     *            the packet context for all packets in this stream
     */
    public void setPacketContext(StructDeclaration packetContext) {
        fPacketContextDecl = packetContext;
    }

    /**
     *
     * @return the event header declaration in structdeclaration form
     * @deprecated use {@link CTFStream#getEventHeaderDeclaration()}
     */
    @Deprecated
    public StructDeclaration getEventHeaderDecl() {
        return (StructDeclaration) ((fEventHeaderDecl instanceof StructDeclaration) ? fEventHeaderDecl : null);
    }

    /**
     * Gets the event header declaration
     *
     * @return the event header declaration in declaration form
     * @since 3.1
     */
    public IDeclaration getEventHeaderDeclaration() {
        return fEventHeaderDecl;
    }

    /**
     *
     * @return the event context declaration in structdeclaration form
     */
    public StructDeclaration getEventContextDecl() {
        return fEventContextDecl;
    }

    /**
     *
     * @return the packet context declaration in structdeclaration form
     */
    public StructDeclaration getPacketContextDecl() {
        return fPacketContextDecl;
    }

    /**
     *
     * @return the set of all stream inputs for this stream
     */
    public Set<CTFStreamInput> getStreamInputs() {
        return fInputs;
    }

    /**
     *
     * @return the parent trace
     */
    public CTFTrace getTrace() {
        return fTrace;
    }

    /**
     *
     * @return all the event declarations for this stream, using the id as a key
     *         for the hashmap.
     * @deprecated use {@link CTFStream#getEventDeclarations()}
     */
    @Deprecated
    public Map<Long, IEventDeclaration> getEvents() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get all the event declarations in this stream.
     *
     * @return The event declarations for this stream
     * @since 3.2
     */
    public @NonNull Collection<IEventDeclaration> getEventDeclarations() {
        List<IEventDeclaration> retVal = new ArrayList<>(fEvents);
        retVal.removeAll(Collections.<IEventDeclaration> singletonList(null));
        return retVal;
    }

    /**
     * Get the event declaration for a given ID.
     *
     * @param eventId
     *            The ID, can be {@link EventDeclaration#UNSET_EVENT_ID}, or any
     *            positive value
     * @return The event declaration with the given ID for this stream, or
     *         'null' if there are no declaration with this ID
     * @throws IllegalArgumentException
     *             If the passed ID is invalid
     * @since 3.2
     */
    public @Nullable IEventDeclaration getEventDeclaration(int eventId) {
        int eventIndex = (eventId == EventDeclaration.UNSET_EVENT_ID) ? 0 : eventId;
        if (eventIndex < 0) {
            /* Any negative value other than UNSET_EVENT_ID is invalid */
            throw new IllegalArgumentException("Event ID cannot be negative."); //$NON-NLS-1$
        }
        if (eventIndex >= fEvents.size()) {
            /* This ID could be valid, but there are no declarations with it */
            return null;
        }
        return fEvents.get(eventIndex);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Adds an event to the event list.
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
        if (fEventUnsetId) {
            throw new ParseException("Event without id with multiple events in a stream"); //$NON-NLS-1$
        }
        int id = ((EventDeclaration) event).id();

        /*
         * If there is an event without id (the null key), it must be the only
         * one
         */
        if (id == EventDeclaration.UNSET_EVENT_ID) {
            if (!fEvents.isEmpty()) {
                throw new ParseException("Event without id with multiple events in a stream"); //$NON-NLS-1$
            }
            fEventUnsetId = true;
            fEvents.add(event);
        } else {
            /* Check if an event with the same ID already exists */
            if (fEvents.size() > id && fEvents.get(id) != null) {
                throw new ParseException("Event id already exists"); //$NON-NLS-1$
            }
            ensureSize(fEvents, id);
            /* Put the event in the list */
            fEvents.set(id, event);
        }
    }

    /**
     * Add a list of event declarations to this stream. There must be no overlap
     * between the two lists of event declarations. This will merge the two
     * lists and preserve the indexes of both lists.
     *
     * @param events
     *            list of the events to add
     * @throws CTFReaderException
     *             if the list already contains data
     * @since 3.2
     */
    public void addEvents(Collection<IEventDeclaration> events) throws CTFReaderException {
        if (fEventUnsetId) {
            throw new CTFReaderException("Cannot add to a stream with an unidentified event"); //$NON-NLS-1$
        }
        if (fEvents.isEmpty()) {
            fEvents.addAll(events);
            return;
        }
        for (IEventDeclaration event : events) {
            if (event != null) {
                int index = event.getId().intValue();
                ensureSize(fEvents, index);
                if (fEvents.get(index) != null) {
                    throw new CTFReaderException("Both lists have an event defined at position " + index); //$NON-NLS-1$
                }
                fEvents.set(index, event);
            }
        }
    }

    private static void ensureSize(ArrayList<? extends Object> list, int index) {
        list.ensureCapacity(index);
        while (list.size() <= index) {
            list.add(null);
        }
    }

    /**
     * Add an input to this Stream
     *
     * @param input
     *            The StreamInput to add.
     */
    public void addInput(CTFStreamInput input) {
        fInputs.add(input);
    }

    @Override
    public String toString() {
        return "Stream [id=" + fId + ", packetContextDecl=" + fPacketContextDecl //$NON-NLS-1$ //$NON-NLS-2$
                + ", eventHeaderDecl=" + fEventHeaderDecl //$NON-NLS-1$
                + ", eventContextDecl=" + fEventContextDecl + ", trace=" + fTrace //$NON-NLS-1$ //$NON-NLS-2$
                + ", events=" + fEvents + ", inputs=" + fInputs + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
