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

import java.math.BigInteger;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;

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
     * @since 3.0
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
     * @since 3.0
     */
    public static final String formatNumber(long value, int base, boolean signed) {
        String s;
        /* Format the number correctly according to the integer's base */
        switch (base) {
        case 2:
            s = "0b" + Long.toBinaryString(value); //$NON-NLS-1$
            break;
        case 8:
            s = "0" + Long.toOctalString(value); //$NON-NLS-1$
            break;
        case 16:
            s = "0x" + Long.toHexString(value); //$NON-NLS-1$
            break;
        case 10:
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
}
