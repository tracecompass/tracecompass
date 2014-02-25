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

package org.eclipse.linuxtools.ctf.core.event;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.trace.StreamInputReader;

/**
 * Representation of a particular instance of an event.
 */
public class EventDefinition implements IDefinitionScope {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The corresponding event declaration.
     */
    private final IEventDeclaration fDeclaration;

    /**
     * The timestamp of the current event.
     */
    private long fTimestamp;

    /**
     * The event context structure definition.
     */
    private StructDefinition fContext;

    /**
     * The event fields structure definition.
     */
    private StructDefinition fFields;

    /**
     * The StreamInputReader that reads this event definition.
     */
    private final StreamInputReader fStreamInputReader;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs an event definition.
     *
     * @param declaration
     *            The corresponding event declaration
     * @param streamInputReader
     *            The SIR from where this EventDef was read
     * @since 2.0
     */
    public EventDefinition(IEventDeclaration declaration,
            StreamInputReader streamInputReader) {
        fDeclaration = declaration;
        fStreamInputReader = streamInputReader;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    @Override
    public String getPath() {
        return "event"; //$NON-NLS-1$
    }

    /**
     * Gets the declaration (the form) of the data
     *
     * @return the event declaration
     * @since 2.0
     */
    public IEventDeclaration getDeclaration() {
        return fDeclaration;
    }

    /**
     * Gets the fields of a definition
     *
     * @return the fields of a definition in struct form. Can be null.
     */
    public StructDefinition getFields() {
        return fFields;
    }

    /**
     * Gets the context of this event without the context of the stream
     *
     * @return the context in struct form
     * @since 1.2
     */
    public StructDefinition getEventContext() {
        return fContext;
    }

    /**
     * Gets the context of this event within a stream
     *
     * @return the context in struct form
     */
    public StructDefinition getContext() {
        final StructDefinition streamContext =
                fStreamInputReader.getPacketReader().getStreamEventContextDef();

        /* Most common case so far */
        if (streamContext == null) {
            return fContext;
        }

        /* streamContext is not null, but the context of the event is null */
        if (fContext == null) {
            return streamContext;
        }

        /* The stream context and event context are assigned. */
        StructDeclaration mergedDeclaration = new StructDeclaration(1);

        /* Add fields from the stream */
        Map<String, Definition> defs = streamContext.getDefinitions();
        for (Entry<String, Definition> entry : defs.entrySet()) {
            mergedDeclaration.addField(entry.getKey(), entry.getValue().getDeclaration());
        }

        /* Add fields from the event context, overwrite the stream ones if needed. */
        for (Entry<String, Definition> entry : fContext.getDefinitions().entrySet()) {
            mergedDeclaration.addField(entry.getKey(), entry.getValue().getDeclaration());
        }

        StructDefinition mergedContext = mergedDeclaration.createDefinition(null, "context"); //$NON-NLS-1$
        for (String key : mergedContext.getDefinitions().keySet()) {
            final Definition lookupDefinition = fContext.lookupDefinition(key);
            /*
             * If the key is in the event context, add it from there, if it is
             * not, then it's in the stream. There is a priority with scoping so
             * if there is a field like "context" in both stream and context,
             * you display the context.
             */
            if (lookupDefinition != null) {
                mergedContext.getDefinitions().put(key, lookupDefinition);
            } else {
                mergedContext.getDefinitions().put(key, streamContext.lookupDefinition(key));
            }
        }
        return mergedContext;
    }

    /**
     * Gets the stream input reader that this event was made by
     *
     * @return the parent
     */
    public StreamInputReader getStreamInputReader() {
        return fStreamInputReader;
    }

    /**
     * Gets the context of packet the event is in.
     *
     * @return the packet context
     */
    public StructDefinition getPacketContext() {
        return fStreamInputReader.getCurrentPacketContext();
    }

    /**
     * gets the CPU the event was generated by. Slightly LTTng specific
     *
     * @return The CPU the event was generated by
     */
    public int getCPU() {
        return fStreamInputReader.getCPU();
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return fTimestamp;
    }

    /**
     * @param timestamp
     *            the timestamp to set
     */
    public void setTimestamp(long timestamp) {
        fTimestamp = timestamp;
    }

    /**
     * @param context
     *            the context to set
     */
    public void setContext(StructDefinition context) {
        fContext = context;
    }

    /**
     * @param fields
     *            the fields to set
     */
    public void setFields(StructDefinition fields) {
        fFields = fields;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Definition lookupDefinition(String lookupPath) {
        if (lookupPath.equals("context")) { //$NON-NLS-1$
            return fContext;
        } else if (lookupPath.equals("fields")) { //$NON-NLS-1$
            return fFields;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        Map<String, Definition> definitions;
        List<String> list;
        StringBuilder retString = new StringBuilder();
        final String cr = System.getProperty("line.separator");//$NON-NLS-1$

        retString.append("Event type: " + fDeclaration.getName() + cr); //$NON-NLS-1$
        retString.append("Timestamp: " + Long.toString(fTimestamp) + cr); //$NON-NLS-1$

        if (fContext != null) {
            definitions = fContext.getDefinitions();
            list = fContext.getDeclaration().getFieldsList();

            for (String field : list) {
                retString.append(field
                        + " : " + definitions.get(field).toString() + cr); //$NON-NLS-1$
            }
        }

        if (fFields != null) {
            definitions = fFields.getDefinitions();
            list = fFields.getDeclaration().getFieldsList();

            for (String field : list) {
                retString.append(field
                        + " : " + definitions.get(field).toString() + cr); //$NON-NLS-1$
            }
        }

        return retString.toString();
    }

}
