/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.types;

import java.nio.ByteOrder;

/**
 * A CTF float declaration.
 *
 * The declaration of a floating point basic data type.
 *
 * @version 1.0
 * @author Matthew Khouzam
 */
public class FloatDeclaration implements IDeclaration {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final int mant;
    private final int exp;
    private final ByteOrder byteOrder;
    private final long alignment;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param exponent
     *            The exponent size in bits
     * @param mantissa
     *            The mantissa size in bits (+1 for sign) (see CTF spec)
     * @param byteOrder
     *            The byte order
     * @param alignment
     *            The alignment. Should be >= 1
     */
    public FloatDeclaration(int exponent, int mantissa, ByteOrder byteOrder,
            long alignment) {
        mant = mantissa;
        exp = exponent;
        this.byteOrder = byteOrder;
        this.alignment = Math.max(alignment, 1);

    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * @return the mant
     */
    public int getMantissa() {
        return mant;
    }

    /**
     * @return the exp
     */
    public int getExponent() {
        return exp;
    }

    /**
     * @return the byteOrder
     */
    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    @Override
    public long getAlignment() {
        return alignment;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public FloatDefinition createDefinition(IDefinitionScope definitionScope,
            String fieldName) {
        return new FloatDefinition(this, definitionScope, fieldName);
    }

    @Override
    public String toString() {
        /* Only used for debugging */
        return "[declaration] float[" + Integer.toHexString(hashCode()) + ']'; //$NON-NLS-1$
    }
}
