/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.types;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;

/**
 * A CTF integer definition.
 *
 * The definition of a integer basic data type. It will take the data from a
 * trace and store it (and make it fit) as a long.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public final class IntegerDefinition extends SimpleDatatypeDefinition {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static final int INT_BASE_10 = 10;
    private static final int INT_BASE_16 = 16;
    private static final int INT_BASE_8 = 8;
    private static final int INT_BASE_2 = 2;
    private final long fValue;

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
     * @param value
     *            integer value
     */
    public IntegerDefinition(@NonNull IntegerDeclaration declaration,
            IDefinitionScope definitionScope, @NonNull String fieldName, long value) {
        super(declaration, definitionScope, fieldName);
        fValue = value;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Gets the value of the integer
     *
     * @return the value of the integer (in long)
     */
    public long getValue() {
        return fValue;
    }

    @Override
    public IntegerDeclaration getDeclaration() {
        return (IntegerDeclaration) super.getDeclaration();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Long getIntegerValue() {
        return getValue();
    }

    @Override
    public long size() {
        return getDeclaration().getMaximumSize();
    }

    @Override
    public String getStringValue() {
        return this.toString();
    }

    @Override
    public String toString() {
        if (getDeclaration().isCharacter()) {
            char c = (char) fValue;
            return Character.toString(c);
        }
        return formatNumber(fValue, getDeclaration().getBase(), getDeclaration().isSigned());
    }

    /**
     * Print a numeric value as a string in a given base
     *
     * @param value
     *            The value to print as string
     * @param base
     *            The base for this value
     * @param signed
     *            Is the value signed or not
     * @return formatted number string
     */
    public static String formatNumber(long value, int base, boolean signed) {
        String s;
        /* Format the number correctly according to the integer's base */
        switch (base) {
        case INT_BASE_2:
            s = "0b" + Long.toBinaryString(value); //$NON-NLS-1$
            break;
        case INT_BASE_8:
            s = "0" + Long.toOctalString(value); //$NON-NLS-1$
            break;
        case INT_BASE_16:
            s = "0x" + Long.toHexString(value); //$NON-NLS-1$
            break;
        case INT_BASE_10:
        default:
            /* For non-standard base, we'll just print it as a decimal number */
            if (!signed && value < 0) {
                /*
                 * Since there are no 'unsigned long', handle this case with
                 * BigInteger
                 */
                BigInteger bigInteger = BigInteger.valueOf(value);
                /*
                 * we add 2^64 to the negative number to get the real unsigned
                 * value
                 */
                bigInteger = bigInteger.add(BigInteger.valueOf(1).shiftLeft(64));
                s = bigInteger.toString();
            } else {
                s = Long.toString(value);
            }
            break;
        }
        return s;
    }

    @Override
    public byte[] getBytes() {
        byte[] data = new byte[(int) Math.ceil(getDeclaration().getLength()/8.0)];
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(getDeclaration().getByteOrder());
        bb.putLong(fValue);
        return data;
    }
}
