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

package org.eclipse.linuxtools.ctf.core.event;

import java.util.HashMap;
import java.util.List;

import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.trace.StreamInputReader;

/**
 * <b><u>EventDefinition</u></b>
 * <p>
 * Represents an instance of an event.
 */
public class EventDefinition implements IDefinitionScope {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The corresponding event declaration.
     */
    public final EventDeclaration declaration;

    /**
     * The timestamp of the current event.
     */
    public long timestamp;

    /**
     * The event context structure definition.
     */
    public StructDefinition context;

    /**
     * The event fields structure definition.
     */
    public StructDefinition fields;

    /**
     * The StreamInputReader that reads this event definition.
     */
    public final StreamInputReader streamInputReader;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs an event definition.
     *
     * @param declaration
     *            The corresponding event declaration.
     */
    public EventDefinition(EventDeclaration declaration,
            StreamInputReader streamInputReader) {
        this.declaration = declaration;
        this.streamInputReader = streamInputReader;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    @Override
    public String getPath() {
        return "event"; //$NON-NLS-1$
    }

    public EventDeclaration getDeclaration() {
        return declaration;
    }

    public StructDefinition getFields() {
        return fields;
    }

    public StructDefinition getContext() {
        return context;
    }

    public StreamInputReader getStreamInputReader() {
        return streamInputReader;
    }

    public StructDefinition getPacketContext() {
        return streamInputReader.getCurrentPacketContext();
    }

    public int getCPU() {
        return streamInputReader.getCPU();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Definition lookupDefinition(String lookupPath) {
        if (lookupPath.equals("context")) { //$NON-NLS-1$
            return context;
        } else if (lookupPath.equals("fields")) { //$NON-NLS-1$
            return fields;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        HashMap<String, Definition> f;
        List<String> list;
        StringBuilder b = new StringBuilder();

        b.append("Event type: " + declaration.getName() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
        b.append("Timestamp: " + Long.toString(timestamp) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$

        if (context != null) {
            f = context.getDefinitions();
            list = context.getDeclaration().getFieldsList();

            for (String field : list) {
                b.append(field + " : " + f.get(field).toString() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        if (fields != null) {
            f = fields.getDefinitions();
            list = fields.getDeclaration().getFieldsList();

            for (String field : list) {
                b.append(field + " : " + f.get(field).toString() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        return b.toString();
    }

}
