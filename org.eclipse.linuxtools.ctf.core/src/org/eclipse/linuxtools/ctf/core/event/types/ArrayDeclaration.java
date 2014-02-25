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

/**
 * A CTF array declaration
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
public class ArrayDeclaration implements IDeclaration {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final int length;
    private final IDeclaration elemType;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param length how many elements in the array
     * @param elemType what type of element is in the array
     */
    public ArrayDeclaration(int length, IDeclaration elemType) {
        this.length = length;
        this.elemType = elemType;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     *
     * @return the type of element in the array
     */
    public IDeclaration getElementType() {
        return elemType;
    }

    /**
     *
     * @return how many elements in the array
     */
    public int getLength() {
        return length;
    }

    @Override
    public long getAlignment() {
        return getElementType().getAlignment();
    }
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public ArrayDefinition createDefinition(IDefinitionScope definitionScope,
            String fieldName) {
        return new ArrayDefinition(this, definitionScope, fieldName);
    }

    @Override
    public String toString() {
        /* Only used for debugging */
        return "[declaration] array[" + Integer.toHexString(hashCode()) + ']'; //$NON-NLS-1$
    }

}
