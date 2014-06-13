/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.types;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

/**
 * A CTF sequence definition (a fixed-size array).
 *
 * An array where the size is fixed but declared in the trace, unlike array
 * where it is declared with a literal
 *
 * @deprecated use {@link AbstractArrayDefinition}
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
@Deprecated
public final class SequenceDefinition extends Definition {

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
     *            Definitions
     * @since 3.0
     */
    public SequenceDefinition(@NonNull SequenceDeclaration declaration, IDefinitionScope definitionScope, @NonNull String fieldName, List<Definition> definitions) {
        super(declaration, definitionScope, fieldName);
        fDefinitions = ImmutableList.copyOf(definitions);
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    @Override
    public SequenceDeclaration getDeclaration() {
        return (SequenceDeclaration) super.getDeclaration();
    }

    /**
     * The length of the sequence in number of elements so a sequence of 5
     * GIANT_rediculous_long_ints is the same as a sequence of 5 bits. (5)
     *
     * @return the length of the sequence
     */
    public int getLength() {
        return fDefinitions.size();
    }

    /**
     * Get the element at i
     *
     * @param i
     *            the index (cannot be negative)
     * @return The element at I, if I &gt; length, null, if I &lt; 0, the method
     *         throws an out of bounds exception
     */
    public Definition getElem(int i) {
        if (i > fDefinitions.size()) {
            return null;
        }
        return fDefinitions.get(i);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();

        if (getDeclaration().isString()) {
            for (Definition def : fDefinitions) {
                IntegerDefinition character = (IntegerDefinition) def;

                if (character.getValue() == 0) {
                    break;
                }

                b.append(character.toString());
            }
        } else {
            b.append('[');
            Joiner joiner = Joiner.on(", ").skipNulls(); //$NON-NLS-1$
            b.append(joiner.join(fDefinitions));
            b.append(']');
        }

        return b.toString();
    }
}
