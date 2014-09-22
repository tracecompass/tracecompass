/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.scope;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.base.Joiner;

/**
 * A node of a lexical scope
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
@NonNullByDefault
public class LexicalScope implements Comparable<LexicalScope> {
    /**
     * Empty string
     *
     * @since 3.0
     */
    public static final LexicalScope ROOT = new RootScope();

    /**
     * Trace string
     *
     * @since 3.0
     */
    public static final LexicalScope TRACE = new LexicalScope(ROOT, "trace"); //$NON-NLS-1$

    /**
     * Env string
     *
     * @since 3.0
     */
    public static final LexicalScope ENV = new LexicalScope(ROOT, "env"); //$NON-NLS-1$

    /**
     * Stream string
     *
     * @since 3.0
     */
    public static final LexicalScope STREAM = new LexicalScope(ROOT, "stream"); //$NON-NLS-1$

    /**
     * Event string
     *
     * @since 3.0
     */
    public static final LexicalScope EVENT = new LexicalScope(ROOT, "event"); //$NON-NLS-1$

    /**
     * Variant string
     *
     * @since 3.0
     */
    public static final LexicalScope VARIANT = new LexicalScope(ROOT, "variant"); //$NON-NLS-1$

    /**
     * packet string
     *
     * @since 3.0
     */
    public static final LexicalScope PACKET = new LexicalScope(ROOT, "packet"); //$NON-NLS-1$

    /**
     * Packet header string
     *
     * @since 3.0
     *
     */
    public static final LexicalScope PACKET_HEADER = new PacketHeaderScope();

    /**
     * Stream packet scope
     *
     * @since 3.0
     */
    public static final LexicalScope STREAM_PACKET = new LexicalScope(STREAM, "packet"); //$NON-NLS-1$

    /**
     * Stream Packet header string
     *
     * @since 3.0
     */
    public static final LexicalScope STREAM_PACKET_CONTEXT = new LexicalScope(STREAM_PACKET, "context"); //$NON-NLS-1$

    /**
     * Trace packet scope
     *
     * @since 3.0
     */
    public static final LexicalScope TRACE_PACKET = new LexicalScope(TRACE, "packet"); //$NON-NLS-1$

    /**
     * Stream event scope
     *
     * @since 3.0
     */
    public static final LexicalScope STREAM_EVENT = new LexicalScope(STREAM, "event"); //$NON-NLS-1$

    /**
     * Trace packet header string
     *
     * @since 3.0
     */
    public static final LexicalScope TRACE_PACKET_HEADER = new LexicalScope(TRACE_PACKET, "header"); //$NON-NLS-1$

    /**
     * Stream event context
     *
     * @since 3.0
     */
    public static final LexicalScope STREAM_EVENT_CONTEXT = new LexicalScope(STREAM_EVENT, "context"); //$NON-NLS-1$

    /**
     * Stream event header
     *
     * @since 3.0
     */
    public static final LexicalScope STREAM_EVENT_HEADER = new LexicalScope(STREAM_EVENT, "header"); //$NON-NLS-1$

    /**
     * Event header
     *
     * @since 3.1
     */
    public static final LexicalScope EVENT_HEADER = new EventHeaderScope(EVENT, "header"); //$NON-NLS-1$

    /**
     * Fields in an event
     *
     * @since 3.0
     */
    public static final LexicalScope FIELDS = new FieldsScope(ROOT, "fields"); //$NON-NLS-1$

    /**
     * Context of an event
     *
     * @since 3.0
     */
    public static final LexicalScope CONTEXT = new LexicalScope(ROOT, "context"); //$NON-NLS-1$

    /**
     * Sorted list of parent paths
     *
     * @since 3.0
     */
    public static final LexicalScope[] PARENT_PATHS = {
            ROOT,
            CONTEXT,
            FIELDS,
            PACKET_HEADER,
            STREAM_EVENT_CONTEXT,
            STREAM_EVENT_HEADER,
            STREAM_PACKET_CONTEXT,
            TRACE_PACKET_HEADER
    };

    private int hash = 0;
    private final String fName;
    private final String fPath;
    private final Map<String, LexicalScope> fChildren;

    /**
     * The scope constructor
     *
     * @param parent
     *            The parent node, can be null, but shouldn't
     * @param name
     *            the name of the field
     */
    @SuppressWarnings("null")
    public LexicalScope(@Nullable LexicalScope parent, String name) {
        fName = name;
        if (parent != null) {
            String pathString = Joiner.on('.').skipNulls().join(parent.fPath, parent.getName());
            /*
             * if joiner return null, we get an NPE... so we won't assign fPath
             * to null
             */
            if (pathString.startsWith(".")) { //$NON-NLS-1$
                /*
                 * substring throws an exception or returns a string, it won't
                 * return null
                 */
                pathString = pathString.substring(1);
            }
            fPath = pathString;
            parent.addChild(fName, this);
        } else {
            fPath = ""; //$NON-NLS-1$
        }
        @NonNull
        Map<String, LexicalScope> children =
                Collections.synchronizedMap(new HashMap<String, LexicalScope>());
        fChildren = children;
    }

    /**
     * Adds a child lexical scope
     *
     * @param name
     *            the name of the child
     * @param child
     *            the child
     */
    private void addChild(String name, LexicalScope child) {
        fChildren.put(name, child);
    }

    /**
     * Get the name
     *
     * @return the name
     */
    public String getName() {
        return fName;
    }

    /**
     * Gets a child of a given name
     *
     * @param name
     *            the child
     * @return the scope, can be null
     */
    @Nullable
    public LexicalScope getChild(String name) {
        return fChildren.get(name);
    }

    @Override
    public String toString() {
        return (fPath.isEmpty() ? fName : fPath + '.' + fName);
    }

    @Override
    public int compareTo(@Nullable LexicalScope other) {
        if (other == null) {
            throw new IllegalArgumentException();
        }
        int comp = fPath.compareTo(other.fPath);
        if (comp == 0) {
            return fName.compareTo(other.fName);
        }
        return comp;
    }

    @Override
    public synchronized int hashCode() {
        if (hash == 0) {
            final int prime = 31;
            hash = prime * (prime + fName.hashCode()) + fPath.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LexicalScope other = (LexicalScope) obj;
        if (!fName.equals(other.fName)) {
            return false;
        }
        return fPath.equals(other.fPath);
    }
}
