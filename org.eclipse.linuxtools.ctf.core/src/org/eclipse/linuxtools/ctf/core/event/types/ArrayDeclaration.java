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

/**
 * <b><u>ArrayDeclaration</u></b>
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

    public ArrayDeclaration(int length, IDeclaration elemType) {
        this.length = length;
        this.elemType = elemType;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    public IDeclaration getElementType() {
        return elemType;
    }

    public int getLength() {
        return length;
    }

    @Override
    public long getAlignment() {
        long retVal = this.getElementType().getAlignment();
        return retVal;
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
