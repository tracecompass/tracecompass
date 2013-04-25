/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *     Simon Marchi - Initial API and implementation
 *     Marc-Andre Laperle - Add min/maximum for validation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.types;

import java.math.BigInteger;
import java.nio.ByteOrder;

/**
 * A CTF integer declaration.
 *
 * The declaration of a integer basic data type.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public class IntegerDeclaration implements IDeclaration {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    final private int length;
    final private boolean signed;
    final private int base;
    final private ByteOrder byteOrder;
    final private Encoding encoding;
    final private long alignment;
    final private String clock;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Contructor
     * @param len the length in bits
     * @param signed is the integer signed? false == unsigned
     * @param base the base (10-16 are most common)
     * @param byteOrder Big endian little endian or other
     * @param encoding ascii, utf8 or none.
     * @param clock the clock path, can be null
     * @param alignment the minimum alignment
     */
    public IntegerDeclaration(int len, boolean signed, int base,
            ByteOrder byteOrder, Encoding encoding, String clock, long alignment) {
        if (len <= 0 || len == 1 && signed) {
            throw new IllegalArgumentException();
        }
        this.length = len;
        this.signed = signed;
        this.base = base;
        this.byteOrder = byteOrder;
        this.encoding = encoding;
        this.clock = clock;
        this.alignment = alignment;
    }

    // ------------------------------------------------------------------------
    // Gettters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Is the integer signed?
     * @return the is the integer signed
     */
    public boolean isSigned() {
        return signed;
    }

    /**
     * get the integer base commonly decimal or hex
     * @return the integer base
     */
    public int getBase() {
        return base;
    }

    /**
     * gets the byte order
     * @return the byte order
     */
    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    /**
     * get encoding, chars are 8 bit ints
     * @return the encoding
     */
    public Encoding getEncoding() {
        return encoding;
    }

    /**
     * is the integer a character (8 bits and encoded?)
     * @return is the integer a char
     */
   public boolean isCharacter() {
        return (length == 8) && (encoding != Encoding.NONE);
    }

   /**
    * How many bits is this int
    * @return the length of the int
    */
    public int getLength() {
        return length;
    }

    @Override
    public long getAlignment(){
        return alignment;
    }

    /**
     * The integer's clock, since timestamps are stored in ints
     * @return the integer's clock, can be null. (most often it is)
     */
    public String getClock(){
        return clock;
    }
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public IntegerDefinition createDefinition(IDefinitionScope definitionScope,
            String fieldName) {
        return new IntegerDefinition(this, definitionScope, fieldName);
    }

    @Override
    public String toString() {
        /* Only used for debugging */
        return "[declaration] integer[" + Integer.toHexString(hashCode()) + ']'; //$NON-NLS-1$
    }

    /**
     * Get the maximum value for this integer declaration
     *
     * @return The maximum value for this integer declaration
     * @since 2.0
     */
    public BigInteger getMaxValue() {
        BigInteger capacity = BigInteger.ONE.shiftLeft(length);
        BigInteger max = signed ? capacity.divide(BigInteger.valueOf(2)) : capacity;
        return max.subtract(BigInteger.ONE);
    }

    /**
     * Get the minimum value for this integer declaration
     *
     * @return The minimum value for this integer declaration
     * @since 2.0
     */
    public BigInteger getMinValue() {
        if (!signed) {
            return BigInteger.ZERO;
        }

        BigInteger capacity = BigInteger.ONE.shiftLeft(length);
        return capacity.divide(BigInteger.valueOf(2)).negate();
    }

}
