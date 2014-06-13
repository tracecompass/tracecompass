/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core.event.types;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.AbstractArrayDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.CompoundDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

/**
 * A CTF array definition
 *
 * Arrays are fixed-length. Their length is declared in the type declaration
 * within the meta-data. They contain an array of "inner type" elements, which
 * can refer to any type not containing the type of the array being declared (no
 * circular dependency). The length is the number of elements in an array.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @since 3.1
 */
@NonNullByDefault
public final class ArrayDefinition extends AbstractArrayDefinition {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final ImmutableList<Definition> fDefinitions;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param declaration
     *            the parent declaration
     * @param definitionScope
     *            the parent scope
     * @param fieldName
     *            the field name
     * @param definitions
     *            the content of the array
     */
    public ArrayDefinition(CompoundDeclaration declaration,
            @Nullable IDefinitionScope definitionScope,
            String fieldName,
            List<Definition> definitions) {
        super(declaration, definitionScope, fieldName);
        @SuppressWarnings("null")
        @NonNull ImmutableList<Definition> list = ImmutableList.copyOf(definitions);
        fDefinitions = list;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    @Override
    public List<Definition> getDefinitions() {
        return fDefinitions;
    }

    /**
     * Get the the number of elements in the array
     *
     * @return how many elements in the array
     */
    public int getLength() {
        return fDefinitions.size();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append('[');
        Joiner joiner = Joiner.on(", ").skipNulls(); //$NON-NLS-1$
        b.append(joiner.join(fDefinitions));
        b.append(']');
        @SuppressWarnings("null")
        @NonNull String ret = b.toString();
        return ret;
    }
}