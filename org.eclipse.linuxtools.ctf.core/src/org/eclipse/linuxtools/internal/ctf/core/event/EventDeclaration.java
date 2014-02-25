/*******************************************************************************
 * Copyright (c) 2011-2013 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi    - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core.event;

import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.linuxtools.ctf.core.CTFStrings;
import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.IEventDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.trace.Stream;
import org.eclipse.linuxtools.ctf.core.trace.StreamInputReader;

/**
 * Representation of one type of event. A bit like "int" or "long" but for trace
 * events.
 */
public class EventDeclaration implements IEventDeclaration {

    /** Id of lost events */
    public static final long LOST_EVENT_ID = -1L;

    /** Id of events when not set */
    public static final long UNSET_EVENT_ID = -2L;

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
     * Event id (can be null if only event in the stream).
     */
    private Long fId = UNSET_EVENT_ID;

    /**
     * Stream to which belongs this event.
     */
    private Stream fStream = null;

    /**
     * Loglevel of an event
     */
    private long fLogLevel;

    /** Map of this event type's custom CTF attributes */
    private final Map<String, String> fCustomAttributes = new HashMap<>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor. Use the setters afterwards to set the fields
     * accordingly.
     */
    public EventDeclaration() {
    }

    @Override
    public EventDefinition createDefinition(StreamInputReader streamInputReader) {
        EventDefinition event = new EventDefinition(this, streamInputReader);

        if (fContext != null) {
            event.setContext(fContext.createDefinition(event, "context")); //$NON-NLS-1$
        }

        if (fFields != null) {
            event.setFields(fFields.createDefinition(event, "fields")); //$NON-NLS-1$
        }

        return event;
    }

    /**
     * Creates a "lost" event. This is a synthetic event that is there to show
     * that there should be something there.
     *
     * @return the lost event
     */
    public static synchronized EventDeclaration getLostEventDeclaration() {
        EventDeclaration lostEvent = new EventDeclaration();
        IntegerDeclaration lostEventsDeclaration = new IntegerDeclaration(32, false, 10, ByteOrder.BIG_ENDIAN, Encoding.ASCII, null, 8);
        IntegerDeclaration timestampDeclaration = new IntegerDeclaration(64, false, 10, ByteOrder.BIG_ENDIAN, Encoding.ASCII, null, 8);

        lostEvent.fFields = new StructDeclaration(1);
        lostEvent.fFields.addField(CTFStrings.LOST_EVENTS_FIELD, lostEventsDeclaration);
        lostEvent.fFields.addField(CTFStrings.LOST_EVENTS_DURATION, timestampDeclaration);
        lostEvent.fId = LOST_EVENT_ID;
        lostEvent.fName = CTFStrings.LOST_EVENT_NAME;

        return lostEvent;
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
        fId = id;
    }

    @Override
    public Long getId() {
        return fId;
    }

    /**
     * Sets the stream of an event declaration
     *
     * @param stream
     *            the stream
     * @since 2.0
     */
    public void setStream(Stream stream) {
        fStream = stream;
    }

    @Override
    public Stream getStream() {
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
        return (fId != null && fId != UNSET_EVENT_ID);
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
     * @since 2.0
     */
    public void setCustomAttribute(String key, String value) {
        fCustomAttributes.put(key, value);
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
        if (fContext == null) {
            if (other.fContext != null) {
                return false;
            }
        } else if (!fContext.equals(other.fContext)) {
            return false;
        }
        if (fFields == null) {
            if (other.fFields != null) {
                return false;
            }
        } else if (!fFields.equals(other.fFields)) {
            return false;
        }
        if (fId == null) {
            if (other.fId != null) {
                return false;
            }
        } else if (!fId.equals(other.fId)) {
            return false;
        }
        if (fName == null) {
            if (other.fName != null) {
                return false;
            }
        } else if (!fName.equals(other.fName)) {
            return false;
        }
        if (fStream == null) {
            if (other.fStream != null) {
                return false;
            }
        } else if (!fStream.equals(other.fStream)) {
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
        result = (prime * result) + ((fId == null) ? 0 : fId.hashCode());
        result = (prime * result) + ((fName == null) ? 0 : fName.hashCode());
        result = (prime * result) + ((fStream == null) ? 0 : fStream.hashCode());
        result = (prime * result) + fCustomAttributes.hashCode();
        return result;
    }

}
