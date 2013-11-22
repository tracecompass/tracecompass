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

package org.eclipse.linuxtools.ctf.core.event.types;

import java.util.Arrays;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;

/**
 * A CTF array definition
 *
 * Arrays are fixed-length. Their length is declared in the type
 * declaration within the meta-data. They contain an array of "inner type"
 * elements, which can refer to any type not containing the type of the
 * array being declared (no circular dependency). The length is the number
 * of elements in an array.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public class ArrayDefinition extends Definition {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final ArrayDeclaration declaration;
    private Definition definitions[];

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param declaration the parent declaration
     * @param definitionScope the parent scope
     * @param fieldName the field name
     */
    public ArrayDefinition(ArrayDeclaration declaration,
            IDefinitionScope definitionScope, String fieldName) {
        super(definitionScope, fieldName);

        this.declaration = declaration;

        definitions = new Definition[declaration.getLength()];

        for (int i = 0; i < declaration.getLength(); i++) {
            definitions[i] = declaration.getElementType().createDefinition(
                    definitionScope, fieldName + "[" + i + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * @return the definitions
     */
    public Definition[] getDefinitions() {
        return Arrays.copyOf(definitions, definitions.length);
    }

    /**
     * @param definitions
     *            the definitions to set
     */
    public void setDefinitions(Definition[] definitions) {
        this.definitions = Arrays.copyOf(definitions, definitions.length);
    }

    /**
     * Get the element at i
     * @param i the index (cannot be negative)
     * @return The element at I, if I > length, null, if I < 0, the method throws an out of bounds exception
     */
    public Definition getElem(int i) {
        if (i > definitions.length) {
            return null;
        }

        return definitions[i];
    }

    @Override
    public ArrayDeclaration getDeclaration() {
        return declaration;
    }

    /**
     * Sometimes, strings are encoded as an array of 1-byte integers (each one
     * being an UTF-8 byte).
     *
     * @return true if this array is in fact an UTF-8 string. false if it's a
     *         "normal" array of generic Definition's.
     */
    public boolean isString() {
        IntegerDeclaration elemInt;

        if (declaration.getElementType() instanceof IntegerDeclaration) {
            /*
             * If the first byte is a "character", we'll consider the whole
             * array a character string.
             */
            elemInt = (IntegerDeclaration) declaration.getElementType();
            if (elemInt.isCharacter()) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void read(BitBuffer input) {
        for (Definition definition : definitions) {
            definition.read(input);
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();

        if (this.isString()) {
            for (Definition def : definitions) {
                IntegerDefinition character = (IntegerDefinition) def;

                if (character.getValue() == 0) {
                    break;
                }

                b.append(character.toString());
            }
        } else if (definitions == null) {
            b.append("[ ]"); //$NON-NLS-1$
        } else {
            b.append('[');
            for (int i = 0; i < (definitions.length - 1); i++) {
                b.append(' ');
                b.append(definitions[i].toString());
                b.append(',');
            }
            b.append(' ');
            b.append(definitions[definitions.length - 1].toString());
            b.append(" ]"); //$NON-NLS-1$
        }

        return b.toString();
    }
}
