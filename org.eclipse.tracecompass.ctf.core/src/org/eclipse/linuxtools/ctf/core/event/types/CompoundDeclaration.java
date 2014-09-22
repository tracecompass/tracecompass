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

package org.eclipse.linuxtools.ctf.core.event.types;


/**
 * Parent of sequences and arrays
 *
 * @author Matthew Khouzam
 * @since 3.1
 */
public abstract class CompoundDeclaration extends Declaration {

    /**
     * Get the element type
     *
     * @return the type of element in the array
     */
    public abstract IDeclaration getElementType();

    @Override
    public long getAlignment() {
        return getElementType().getAlignment();
    }

    /**
     * Sometimes, strings are encoded as an array of 1-byte integers (each one
     * being an UTF-8 byte).
     *
     * @return true if this array is in fact an UTF-8 string. false if it's a
     *         "normal" array of generic Definition's.
     */
    public boolean isString(){
        IDeclaration elementType = getElementType();
        if (elementType instanceof IntegerDeclaration) {
            IntegerDeclaration elemInt = (IntegerDeclaration) elementType;
            return elemInt.isCharacter();
        }
        return false;
    }

}
