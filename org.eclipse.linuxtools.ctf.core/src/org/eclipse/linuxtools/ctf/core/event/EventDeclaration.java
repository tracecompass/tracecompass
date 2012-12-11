/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi    - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event;

import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.trace.Stream;
import org.eclipse.linuxtools.ctf.core.trace.StreamInputReader;

/**
 * Representation of one type of event. A bit like "int" or "long" but for trace
 * events.
 */
public class EventDeclaration {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Name of the event
     */
    private String name;

    /**
     * Event context structure declaration
     */
    private StructDeclaration context = null;

    /**
     * Event fields structure declaration
     */
    private StructDeclaration fields = null;

    /**
     * Event id (can be null if only event in the stream).
     */
    private Long id = null;

    /**
     * Stream to which belongs this event.
     */
    private Stream stream = null;

    /**
     * Loglevel of an event
     */
    private long logLevel;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor. Use the setters afterwards to set the fields
     * accordingly.
     */
    public EventDeclaration() {}

    /**
     * Creates an instance of EventDefinition corresponding to this declaration.
     *
     * @param streamInputReader
     *            The StreamInputReader for which this definition is created.
     * @return A new EventDefinition.
     */
    public EventDefinition createDefinition(StreamInputReader streamInputReader) {
        EventDefinition event = new EventDefinition(this, streamInputReader);

        if (context != null) {
            event.setContext( context.createDefinition(event, "context")); //$NON-NLS-1$
        }

        if (this.fields != null) {
            event.setFields(this.fields.createDefinition(event, "fields")); //$NON-NLS-1$
        }

        return event;
    }

    /**
     * Creates a "lost" event. This is a synthetic event that is there to show
     * that there should be something there.
     * @return the lost event
     */
    public synchronized static EventDeclaration getLostEventDeclaration(){
        EventDeclaration lostEvent = new EventDeclaration();
        lostEvent.fields = new StructDeclaration(1);
        lostEvent.id = -1L;
        lostEvent.name = "Lost event"; //$NON-NLS-1$
        return lostEvent;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Sets a name for an event Declaration
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name of en event declaration
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the context for an event declaration (see CTF specification)
     * @param context the context in structdeclaration format
     */
    public void setContext(StructDeclaration context) {
        this.context = context;
    }

    /**
     * Sets the fields of an event declaration
     * @param fields the fields in structdeclaration format
     */
    public void setFields(StructDeclaration fields) {
        this.fields = fields;
    }

    /**
     * Gets the fields of an event declaration
     * @return fields the fields in structdeclaration format
     */
    public StructDeclaration getFields() {
        return fields;
    }

    /**
     * Gets the context of an event declaration
     * @return context the fields in structdeclaration format
     */
    public StructDeclaration getContext() {
        return context;
    }

    /**
     * Sets the id of am event declaration
     * @param id the id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Gets the id of am event declaration return id the id
     *
     * @return The EventDeclaration ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the stream of am event declaration
     * @param stream the stream
     * @since 2.0
     */
    public void setStream(Stream stream) {
        this.stream = stream;
    }

    /**
     * Gets the stream of am event declaration
     * @return stream the stream
     * @since 2.0
     */
    public Stream getStream() {
        return stream;
    }

    /**
     * Is the name of the event declaration set
     * @return is the name set?
     */
    public boolean nameIsSet() {
        return name != null;
    }

    /**
     * Is the context set
     * @return is the context set
     */
    public boolean contextIsSet() {
        return context != null;
    }

    /**
     * Is a field set?
     * @return Is the field set?
     */
    public boolean fieldsIsSet() {
        return fields != null;
    }

    /**
     * Is the id set?
     * @return is the id set?
     */
    public boolean idIsSet() {
        return id != null;
    }

    /**
     * Is the stream set?
     * @return is the stream set?
     */
    public boolean streamIsSet() {
        return stream != null;
    }

    /**
     * What is the log level of this event
     * @return the log level.
     */
    public long getLogLevel() {
        return logLevel;
    }

    /**
     * Sets the log level
     * @param level the log level
     */
    public void setLogLevel( long level){
        logLevel = level;
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
        if (context == null) {
            if (other.context != null) {
                return false;
            }
        } else if (!context.equals(other.context)) {
            return false;
        }
        if (fields == null) {
            if (other.fields != null) {
                return false;
            }
        } else if (!fields.equals(other.fields)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (stream == null) {
            if (other.stream != null) {
                return false;
            }
        } else if (!stream.equals(other.stream)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result)
                + ((context == null) ? 0 : context.hashCode());
        result = (prime * result) + ((fields == null) ? 0 : fields.hashCode());
        result = (prime * result) + ((id == null) ? 0 : id.hashCode());
        result = (prime * result) + ((name == null) ? 0 : name.hashCode());
        result = (prime * result) + ((stream == null) ? 0 : stream.hashCode());
        return result;
    }

}
