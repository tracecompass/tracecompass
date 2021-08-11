/*******************************************************************************
 * Copyright (c) 2011-2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.event.IEventDeclaration;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.scope.ILexicalScope;
import org.eclipse.tracecompass.ctf.core.event.scope.LexicalScope;
import org.eclipse.tracecompass.ctf.core.event.types.Definition;
import org.eclipse.tracecompass.ctf.core.event.types.ICompositeDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;
import org.eclipse.tracecompass.ctf.core.trace.ICTFPacketDescriptor;

/**
 * Representation of a particular instance of an event.
 */
public final class EventDefinition implements IDefinitionScope, IEventDefinition {

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
     * The current cpu, could be @link {@link IPacketHeader#UNKNOWN_CPU}
     */
    private final int fCpu;

    private final @NonNull Map<String, Object> fPacketAttributes;

    private final ICompositeDefinition fPacketHeader;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs an event definition.
     *
     * @param declaration
     *            The corresponding event declaration
     * @param cpu
     *            The cpu source of the event. You can use UNKNOWN_CPU if it is
     *            not known.
     * @param timestamp
     *            event timestamp
     * @param eventHeaderDefinition
     *            The event header definition, can be null if there is no header
     *            definition
     * @param eventContext
     *            The event context
     * @param packetHeader
     *            the packet header (the one with magic number)
     * @param streamContext
     *            the stream context
     * @param fields
     *            The event fields
     * @param packetDescriptor
     *            descriptor of the packet containing this event (containing
     *            the packet context)
     * @since 2.0
     */
    public EventDefinition(IEventDeclaration declaration,
            int cpu,
            long timestamp,
            ICompositeDefinition eventHeaderDefinition,
            ICompositeDefinition streamContext,
            ICompositeDefinition eventContext,
            ICompositeDefinition packetHeader,
            ICompositeDefinition fields,
            @Nullable ICTFPacketDescriptor packetDescriptor) {
        fDeclaration = declaration;
        fEventHeaderDefinition = eventHeaderDefinition;
        fCpu = cpu;
        fTimestamp = timestamp;
        fFields = fields;
        fEventContext = eventContext;
        fPacketHeader = packetHeader;
        fStreamContext = streamContext;
        fPacketAttributes = packetDescriptor != null ? packetDescriptor.getAttributes() : Collections.emptyMap();
        fPacketContext = packetDescriptor != null ? packetDescriptor.getStreamPacketContextDef() : null;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

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

    @Override
    public IEventDeclaration getDeclaration() {
        return fDeclaration;
    }

    @Override
    public ICompositeDefinition getEventHeader() {
        return fEventHeaderDefinition;
    }

    @Override
    public ICompositeDefinition getFields() {
        return fFields;
    }

    @Override
    public ICompositeDefinition getEventContext() {
        return fEventContext;
    }

    @Override
    public ICompositeDefinition getStreamContext() {
        return fStreamContext;
    }

    @Override
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
        List<@NonNull String> fieldNames = fStreamContext.getFieldNames();
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

    @Override
    public ICompositeDefinition getPacketContext() {
        return fPacketContext;
    }

    @Override
    public int getCPU() {
        return fCpu;
    }

    @Override
    public long getTimestamp() {
        return fTimestamp;
    }

    @Override
    public Map<String, Object> getPacketAttributes() {
        return fPacketAttributes;
    }

    @Override
    public ICompositeDefinition getPacketHeader() {
        return fPacketHeader;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

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
