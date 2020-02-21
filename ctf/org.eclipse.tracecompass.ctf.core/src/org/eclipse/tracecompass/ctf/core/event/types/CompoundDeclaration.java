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
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.types;

/**
 * Parent of sequences and arrays
 *
 * @author Matthew Khouzam
 */
public abstract class CompoundDeclaration extends Declaration {

    private static final int BIT_MASK = 0x03;
    private static final int BITS_PER_BYTE = 8;

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
    public boolean isString() {
        IDeclaration elementType = getElementType();
        if (elementType instanceof IntegerDeclaration) {
            IntegerDeclaration elemInt = (IntegerDeclaration) elementType;
            return elemInt.isCharacter();
        }
        return false;
    }

    /**
     * If an array contains 8 bit aligned 8 bit ints, it can be bulk read.
     *
     * @return true if this array 1 byte aligned. false if it's a "normal" array
     *         of generic Definition's.
     * @since 1.0
     */
    public boolean isAlignedBytes() {
        IDeclaration elementType = getElementType();
        if (elementType instanceof IntegerDeclaration) {
            IntegerDeclaration elemInt = (IntegerDeclaration) elementType;
            return (elemInt.getLength() == BITS_PER_BYTE) && ((getAlignment() & BIT_MASK) == 0);
        }
        return false;
    }

}
