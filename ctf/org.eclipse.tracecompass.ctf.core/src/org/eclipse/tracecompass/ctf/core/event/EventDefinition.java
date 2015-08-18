/*******************************************************************************
 * Copyright (c) 2011-2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.scope.ILexicalScope;
import org.eclipse.tracecompass.ctf.core.event.scope.LexicalScope;
import org.eclipse.tracecompass.ctf.core.event.types.Definition;
import org.eclipse.tracecompass.ctf.core.event.types.ICompositeDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;
import org.eclipse.tracecompass.ctf.core.trace.CTFStreamInputReader;
import org.eclipse.tracecompass.internal.ctf.core.event.EventDeclaration;

/**
 * Representation of a particular instance of an event.
 */
public final class EventDefinition implements IDefinitionScope {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * A null event, can be used for testing or poison pilling
     */
    @NonNull
    public static final EventDefinition NULL_EVENT = new EventDefinition(new EventDeclaration(), null, -1L, null, null, null, null);

    /**
     * The corresponding event declaration.
     */
    private final IEventDeclaration fDeclaration;

    /**
     * The timestamp of the current event.
     */
    private final long fTimestamp;

    private final ICompositeDefinition fEventHeaderDefinition;

    /**
     * The event context structure definition.
     */
    private final ICompositeDefinition fEventContext;

    private final ICompositeDefinition fStreamContext;

    private final ICompositeDefinition fPacketContext;

    /**
     * The event fields structure definition.
     */
    private final ICompositeDefinition fFields;

    /**
     * The StreamInputReader that reads this event definition.
     */
    private final CTFStreamInputReader fStreamInputReader;

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
     * @param timestamp
     *            event timestamp
     * @param eventContext
     *            The event context
     * @param packetContext
     *            the packet context
     * @param streamContext
     *            the stream context
     * @param fields
     *            The event fields
     * @since 1.0
     */
    public EventDefinition(IEventDeclaration declaration,
            CTFStreamInputReader streamInputReader,
            long timestamp,
            ICompositeDefinition streamContext,
            ICompositeDefinition eventContext,
            ICompositeDefinition packetContext,
            ICompositeDefinition fields) {
        this(declaration, streamInputReader, timestamp, null, streamContext,
                eventContext, packetContext, fields);
    }

    /**
     * Constructs an event definition.
     *
     * @param declaration
     *            The corresponding event declaration
     * @param streamInputReader
     *            The SIR from where this EventDef was read
     * @param timestamp
     *            event timestamp
     * @param eventHeaderDefinition
     *            the event header definition, can be null
     * @param eventContext
     *            The event context
     * @param packetContext
     *            the packet context
     * @param streamContext
     *            the stream context
     * @param fields
     *            The event fields
     * @since 2.0
     */
    public EventDefinition(IEventDeclaration declaration,
            CTFStreamInputReader streamInputReader,
            long timestamp,
            ICompositeDefinition eventHeaderDefinition,
            ICompositeDefinition streamContext,
            ICompositeDefinition eventContext,
            ICompositeDefinition packetContext,
            ICompositeDefinition fields) {
        fDeclaration = declaration;
        fEventHeaderDefinition = eventHeaderDefinition;
        fStreamInputReader = streamInputReader;
        fTimestamp = timestamp;
        fFields = fields;
        fEventContext = eventContext;
        fPacketContext = packetContext;
        fStreamContext = streamContext;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * @since 1.0
     */
    @Override
    public ILexicalScope getScopePath() {
        String eventName = fDeclaration.getName();
        if (eventName == null) {
            return null;
        }
        ILexicalScope myScope = ILexicalScope.EVENT.getChild(eventName);
        if (myScope == null) {
            myScope = new LexicalScope(ILexicalScope.EVENT, eventName);
        }
        return myScope;
    }

    /**
     * Gets the declaration (the form) of the data
     *
     * @return the event declaration
     */
    public IEventDeclaration getDeclaration() {
        return fDeclaration;
    }

    /**
     * Get the event header
     *
     * @return the event header
     * @since 2.0
     */
    public ICompositeDefinition getEventHeader() {
        return fEventHeaderDefinition;
    }

    /**
     * Gets the fields of a definition
     *
     * @return the fields of a definition in struct form. Can be null.
     * @since 1.0
     */
    public ICompositeDefinition getFields() {
        return fFields;
    }

    /**
     * Gets the context of this event without the context of the stream
     *
     * @return the context in struct form
     * @since 1.0
     */
    public ICompositeDefinition getEventContext() {
        return fEventContext;
    }

    /**
     * Gets the context of this event within a stream
     *
     * @return the context in struct form
     * @since 1.0
     */
    public ICompositeDefinition getContext() {

        /* Most common case so far */
        if (fStreamContext == null) {
            return fEventContext;
        }

        /* streamContext is not null, but the context of the event is null */
        if (fEventContext == null) {
            return fStreamContext;
        }

        // TODO: cache if this is a performance issue

        /* The stream context and event context are assigned. */
        StructDeclaration mergedDeclaration = new StructDeclaration(1);

        List<Definition> fieldValues = new ArrayList<>();

        /* Add fields from the stream */
        List<String> fieldNames = fStreamContext.getFieldNames();
        for (String fieldName : fieldNames) {
            Definition definition = fStreamContext.getDefinition(fieldName);
            mergedDeclaration.addField(fieldName, definition.getDeclaration());
            fieldValues.add(definition);
        }

        /*
         * Add fields from the event context, overwrite the stream ones if
         * needed.
         */
        for (String fieldName : fEventContext.getFieldNames()) {
            Definition definition = fEventContext.getDefinition(fieldName);
            mergedDeclaration.addField(fieldName, definition.getDeclaration());
            if (fieldNames.contains(fieldName)) {
                fieldValues.set((fieldNames.indexOf(fieldName)), definition);
            } else {
                fieldValues.add(definition);
            }
        }
        return new StructDefinition(mergedDeclaration, this, "context", //$NON-NLS-1$
                fieldValues.toArray(new Definition[fieldValues.size()]));
    }

    /**
     * Gets the stream input reader that this event was made by
     *
     * @return the parent
     */
    public CTFStreamInputReader getStreamInputReader() {
        return fStreamInputReader;
    }

    /**
     * Gets the context of packet the event is in.
     *
     * @return the packet context
     * @since 1.0
     */
    public ICompositeDefinition getPacketContext() {
        return fPacketContext;
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

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * @since 1.0
     */
    @Override
    public IDefinition lookupDefinition(String lookupPath) {
        if (lookupPath.equals("context")) { //$NON-NLS-1$
            return fEventContext;
        } else if (lookupPath.equals("fields")) { //$NON-NLS-1$
            return fFields;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        Iterable<String> list;
        StringBuilder retString = new StringBuilder();
        final String cr = System.getProperty("line.separator");//$NON-NLS-1$

        retString.append("Event type: ").append(fDeclaration.getName()).append(cr); //$NON-NLS-1$
        retString.append("Timestamp: ").append(Long.toString(fTimestamp)).append(cr); //$NON-NLS-1$

        if (fEventContext != null) {
            list = fEventContext.getFieldNames();

            for (String field : list) {
                retString.append(field).append(" : ").append(fEventContext.getDefinition(field).toString()).append(cr); //$NON-NLS-1$
            }
        }

        if (fFields != null) {
            list = fFields.getFieldNames();

            for (String field : list) {
                retString.append(field).append(" : ").append(fFields.getDefinition(field).toString()).append(cr); //$NON-NLS-1$
            }
        }

        return retString.toString();
    }

}
