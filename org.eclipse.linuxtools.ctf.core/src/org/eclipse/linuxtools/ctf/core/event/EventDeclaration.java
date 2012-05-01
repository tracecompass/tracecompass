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
import org.eclipse.linuxtools.ctf.core.trace.StreamInputReader;
import org.eclipse.linuxtools.internal.ctf.core.trace.Stream;

/**
 * <b><u>EventDeclaration</u></b>
 * <p>
 * Represents one type of event.
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
     * Creates an instance of EventDefinition corresponding to this declaration.
     *
     * @param streamInputReader
     *            The StreamInputReader for which this definition is created.
     * @return A new EventDefinition.
     */
    public EventDefinition createDefinition(StreamInputReader streamInputReader) {
        EventDefinition event = new EventDefinition(this, streamInputReader);

        if (context != null) {
            event.context = context.createDefinition(event, "context"); //$NON-NLS-1$
        }

        if (this.fields != null) {
            event.fields = this.fields.createDefinition(event, "fields"); //$NON-NLS-1$
        }

        return event;
    }

    /**
     * Creates a "lost" event. This is a synthetic event that is there to show
     * that there should be something there.
     * @return
     */
    public synchronized static EventDeclaration getLostEventDeclaration(){
        EventDeclaration lostEvent = new EventDeclaration();
        lostEvent.fields = new StructDeclaration(1);
        lostEvent.id = (long) -1;
        lostEvent.name = "Lost event"; //$NON-NLS-1$
        return lostEvent;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setContext(StructDeclaration context) {
        this.context = context;
    }

    public void setFields(StructDeclaration fields) {
        this.fields = fields;
    }

    public StructDeclaration getFields() {
        return fields;
    }

    public StructDeclaration getContext() {
        return context;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setStream(Stream stream) {
        this.stream = stream;
    }

    public Stream getStream() {
        return stream;
    }

    public boolean nameIsSet() {
        return name != null;
    }

    public boolean contextIsSet() {
        return context != null;
    }

    public boolean fieldsIsSet() {
        return fields != null;
    }

    public boolean idIsSet() {
        return id != null;
    }

    public boolean streamIsSet() {
        return stream != null;
    }

    public long getLogLevel() {
        return logLevel;
    }

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
