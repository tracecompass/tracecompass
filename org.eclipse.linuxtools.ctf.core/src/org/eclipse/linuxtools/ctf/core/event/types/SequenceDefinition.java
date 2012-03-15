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

/**
 * <b><u>SequenceDefinition</u></b>
 */
public class SequenceDefinition extends Definition {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final SequenceDeclaration declaration;
    private IntegerDefinition lengthDefinition;
    private Definition definitions[];
    private int currentLength;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public SequenceDefinition(SequenceDeclaration declaration,
            IDefinitionScope definitionScope, String fieldName) {
        super(definitionScope, fieldName);

        this.declaration = declaration;
        // this.definitionScope = definitionScope;

        if (definitionScope != null) {
            Definition lenDef = definitionScope.lookupDefinition(declaration.getLengthName());
            lengthDefinition = (IntegerDefinition) lenDef;
        }
        /*
         * if (lenDef == null) { throw new
         * Exception("Sequence length field not found"); }
         *
         * if (!(lenDef instanceof IntegerDefinition)) { throw new
         * Exception("Sequence length field not integer"); }
         */
        /*
         * if (this.lengthDefinition.declaration.signed) { throw new
         * Exception("Sequence length must not be signed"); }
         */
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    public SequenceDeclaration getDeclaration() {
        return declaration;
    }

    public int getLength() {
        return currentLength;
    }

    public Definition getElem(int i) {
        if (i > definitions.length) {
            return null;
        }

        return definitions[i];
    }

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
                newDefinitions[i] = declaration.getElementType().createDefinition(
                        definitionScope, fieldName + "[" + i + "]"); //$NON-NLS-1$ //$NON-NLS-2$
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
