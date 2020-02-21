/*******************************************************************************
 * Copyright (c) 2014 Ericsson
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A node of a lexical scope
 *
 * @author Matthew Khouzam
 */
public class LexicalScope implements ILexicalScope {
    private int hash = 0;
    private final @NonNull String fName;
    private final @NonNull String fPath;
    private final Map<String, ILexicalScope> fChildren = new ConcurrentHashMap<>();

    /**
     * Hidden constructor for the root node only
     *
     * @since 1.0
     */
    protected LexicalScope() {
        fPath = ""; //$NON-NLS-1$
        fName = ""; //$NON-NLS-1$
    }

    /**
     * The scope constructor
     *
     * @param parent
     *            The parent node, can be null, but shouldn't
     * @param name
     *            the name of the field
     * @since 1.0
     */
    public LexicalScope(ILexicalScope parent, @NonNull String name) {
        fName = name;
        fPath = parent.getPath().isEmpty() ? fName : parent.getPath() + '.' + fName;
        parent.addChild(name, this);
    }

    /**
     * @since 1.0
     */
    @Override
    public void addChild(String name, ILexicalScope child) {
        fChildren.put(name, child);
    }

    @Override
    public @NonNull String getName() {
        return fName;
    }

    /**
     * @since 1.0
     */
    @Override
    public @Nullable ILexicalScope getChild(String name) {
        return fChildren.get(name);
    }

    /**
     * @since 1.0
     */
    @Override
    public @NonNull String getPath() {
        return fPath;
    }

    // for debugging purposes
    @Override
    public String toString() {
        return getPath();
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
