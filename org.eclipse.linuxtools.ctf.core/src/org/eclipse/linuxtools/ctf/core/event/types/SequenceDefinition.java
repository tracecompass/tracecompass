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

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

/**
 * A CTF sequence definition (a fixed-size array).
 *
 * An array where the size is fixed but declared in the trace, unlike array
 * where it is declared with a literal
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public class SequenceDefinition extends Definition {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final SequenceDeclaration declaration;
    private final IntegerDefinition lengthDefinition;
    private Definition definitions[];
    private int currentLength;

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
     * @throws CTFReaderException
     *             If the sequence field was malformatted
     */
    public SequenceDefinition(SequenceDeclaration declaration,
            IDefinitionScope definitionScope, String fieldName)
            throws CTFReaderException {
        super(definitionScope, fieldName);
        Definition lenDef = null;

        this.declaration = declaration;

        if (definitionScope != null) {
            lenDef = definitionScope.lookupDefinition(declaration
                    .getLengthName());
        }

        if (lenDef == null) {
            throw new CTFReaderException("Sequence length field not found"); //$NON-NLS-1$
        }

        if (!(lenDef instanceof IntegerDefinition)) {
            throw new CTFReaderException("Sequence length field not integer"); //$NON-NLS-1$
        }

        lengthDefinition = (IntegerDefinition) lenDef;

        if (this.lengthDefinition.getDeclaration().isSigned()) {
            throw new CTFReaderException("Sequence length must not be signed"); //$NON-NLS-1$
        }
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    @Override
    public SequenceDeclaration getDeclaration() {
        return declaration;
    }

    /**
     * The length of the sequence in number of elements so a sequence of 5
     * GIANT_rediculous_long_ints is the same as a sequence of 5 bits. (5)
     *
     * @return the length of the sequence
     */
    public int getLength() {
        return currentLength;
    }

    /**
     * Get the element at i
     *
     * @param i
     *            the index (cannot be negative)
     * @return The element at I, if I > length, null, if I < 0, the method
     *         throws an out of bounds exception
     */
    public Definition getElem(int i) {
        if (i > definitions.length) {
            return null;
        }

        return definitions[i];
    }

    /**
     * Is the sequence a null terminated string?
     * @return true == is a string, false == is not a string
     */
    public boolean isString() {
        IntegerDeclaration elemInt;

        if (declaration.getElementType() instanceof IntegerDeclaration) {
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
        currentLength = (int) lengthDefinition.getValue();

        if ((definitions == null) || (definitions.length < currentLength)) {
            Definition newDefinitions[] = new Definition[currentLength];

            int i = 0;

            if (definitions != null) {
                for (; i < definitions.length; i++) {
                    newDefinitions[i] = definitions[i];
                }
            }

            for (; i < currentLength; i++) {
                newDefinitions[i] = declaration.getElementType()
                        .createDefinition(definitionScope,
                                fieldName + "[" + i + "]"); //$NON-NLS-1$ //$NON-NLS-2$
            }

            definitions = newDefinitions;
        }

        for (int i = 0; i < currentLength; i++) {
            definitions[i].read(input);
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();

        if (this.isString()) {
            for (int i = 0; i < currentLength; i++) {
                IntegerDefinition character = (IntegerDefinition) definitions[i];

                if (character.getValue() == 0) {
                    break;
                }

                b.append(character.toString());
            }
        } else {
            b.append('[');
            if (currentLength > 0) {
                for (int i = 0; i < (currentLength - 1); i++) {
                    b.append(' ');
                    b.append(definitions[i].toString());
                    b.append(',');
                }
                b.append(' ');
                b.append(definitions[currentLength - 1].toString());
            }
            b.append(" ]"); //$NON-NLS-1$

        }

        return b.toString();
    }
}
