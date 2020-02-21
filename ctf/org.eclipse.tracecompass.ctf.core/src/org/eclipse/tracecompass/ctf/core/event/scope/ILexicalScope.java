/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.ctf.core.event.scope;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @since 1.0
 */
@NonNullByDefault
public interface ILexicalScope {
    /**
     * Empty string
     */
    ILexicalScope ROOT = new RootScope();

    /**
     * Trace string
     */
    ILexicalScope TRACE = new LexicalScope(ROOT, "trace"); //$NON-NLS-1$
    /**
     * Env string
     */
    ILexicalScope ENV = new LexicalScope(ROOT, "env"); //$NON-NLS-1$
    /**
     * Stream string
     */
    LexicalScope STREAM = new LexicalScope(ROOT, "stream"); //$NON-NLS-1$
    /**
     * Event string
     */
    LexicalScope EVENT = new LexicalScope(ROOT, "event"); //$NON-NLS-1$
    /**
     * Variant string
     */
    ILexicalScope VARIANT = new LexicalScope(ROOT, "variant"); //$NON-NLS-1$
    /**
     * packet string
     */
    LexicalScope PACKET = new LexicalScope(ROOT, "packet"); //$NON-NLS-1$
    /**
     * Packet header string
     */
    LexicalScope PACKET_HEADER = new PacketHeaderScope();

    /**
     * Packet header v id string
     */
    ILexicalScope EVENT_HEADER_V_ID = new LexicalScope(PACKET_HEADER, "id"); //$NON-NLS-1$
    /**
     * Packet header v timestamp string
     */
    ILexicalScope EVENT_HEADER_V_TIMESTAMP = new LexicalScope(PACKET_HEADER, "timestamp"); //$NON-NLS-1$

    /**
     * Stream packet scope
     */
    LexicalScope STREAM_PACKET = new LexicalScope(STREAM, "packet"); //$NON-NLS-1$
    /**
     * Stream Packet header string
     */
    ILexicalScope STREAM_PACKET_CONTEXT = new LexicalScope(STREAM_PACKET, "context"); //$NON-NLS-1$
    /**
     * Trace packet scope
     */
    LexicalScope TRACE_PACKET = new LexicalScope(TRACE, "packet"); //$NON-NLS-1$
    /**
     * Stream event scope
     */
    LexicalScope STREAM_EVENT = new LexicalScope(STREAM, "event"); //$NON-NLS-1$
    /**
     * Trace packet header string
     */
    ILexicalScope TRACE_PACKET_HEADER = new LexicalScope(TRACE_PACKET, "header"); //$NON-NLS-1$
    /**
     * Stream event context
     */
    ILexicalScope STREAM_EVENT_CONTEXT = new LexicalScope(STREAM_EVENT, "context"); //$NON-NLS-1$
    /**
     * Stream event header
     */
    ILexicalScope STREAM_EVENT_HEADER = new LexicalScope(STREAM_EVENT, "header"); //$NON-NLS-1$
    /**
     * Context of an event
     */
    LexicalScope CONTEXT = new LexicalScope(ROOT, "context"); //$NON-NLS-1$
    /**
     * Event Header scope
     */
    ILexicalScope EVENT_HEADER = new EventHeaderScope(EVENT, "header"); //$NON-NLS-1$

    /**
     * Event header id string
     */
    ILexicalScope EVENT_HEADER_ID = new LexicalScope(EVENT_HEADER, "id"); //$NON-NLS-1$

    /**
     * Event header v as in variant string
     */
    ILexicalScope EVENT_HEADER_V = new EventHeaderVScope(EVENT_HEADER, "v"); //$NON-NLS-1$

    /**
     * Fields in an event
     */
    ILexicalScope FIELDS = new FieldsScope(ROOT, "fields"); //$NON-NLS-1$

    /**
     * ret field
     */
    ILexicalScope FIELDS_RET = new LexicalScope(FIELDS, "_ret"); //$NON-NLS-1$

    /**
     * tid field
     */
    ILexicalScope FIELDS_TID = new LexicalScope(FIELDS, "_tid"); //$NON-NLS-1$

    /**
     * Get the name
     *
     * @return the name
     */
    String getName();

    /**
     * Gets a child of a given name
     *
     * @param name
     *            the child
     * @return the scope, can be null
     */
    @Nullable
    ILexicalScope getChild(String name);

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    /**
     * Adds a child lexical scope
     *
     * @param name
     *            the name of the child
     * @param child
     *            the child
     */
    void addChild(String name, ILexicalScope child);

    /**
     * Get the path of the scope
     *
     * @return the path of the scope
     */
    String getPath();


}