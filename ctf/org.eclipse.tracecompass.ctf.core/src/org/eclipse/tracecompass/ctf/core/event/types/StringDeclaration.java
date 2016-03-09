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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;

/**
 * A CTF string declaration.
 *
 * Strings are an array of bytes of variable size and are terminated by a '\0'
 * "NULL" character. Their encoding is described in the TSDL meta-data. In
 * absence of encoding attribute information, the default encoding is UTF-8.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
@NonNullByDefault
public class StringDeclaration extends Declaration {

    private static final StringDeclaration STRING_DEC_UTF8 = new StringDeclaration(Encoding.UTF8);
    private static final StringDeclaration STRING_DEC_ASCII = new StringDeclaration(Encoding.ASCII);
    private static final StringDeclaration STRING_DEC_NO_ENC = new StringDeclaration(Encoding.NONE);

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static final int BITS_PER_BYTE = Byte.SIZE;
    private final Encoding fEncoding;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Generate an encoded string declaration
     *
     * @param encoding
     *            the encoding, utf8 or ascii
     */
    private StringDeclaration(Encoding encoding) {
        fEncoding = encoding;
    }

    /**
     * Create a StringDeclaration with the default UTF-8 encoding
     *
     * @return a {@link StringDeclaration} with UTF-8 encoding
     */
    public static StringDeclaration getStringDeclaration() {
        return STRING_DEC_UTF8;
    }

    /**
     * Create a StringDeclaration
     *
     * @param encoding
     *            the {@link Encoding} can be Encoding.UTF8, Encoding.ASCII or
     *            other
     * @return a {@link StringDeclaration}
     * @throws IllegalArgumentException
     *             if the encoding is not recognized.
     */
    public static StringDeclaration getStringDeclaration(Encoding encoding) {
        switch (encoding) {
        case ASCII:
            return STRING_DEC_ASCII;
        case NONE:
            return STRING_DEC_NO_ENC;
        case UTF8:
            return STRING_DEC_UTF8;
        default:
            throw new IllegalArgumentException("Unrecognized encoding: " + encoding); //$NON-NLS-1$
        }
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     *
     * @return the character encoding.
     */
    public Encoding getEncoding() {
        return fEncoding;
    }

    @Override
    public long getAlignment() {
        // See ctf 4.2.5: Strings are always aligned on byte size.
        return BITS_PER_BYTE;
    }

    @Override
    public int getMaximumSize() {
        /*
         * Every definition can have a different size, so we do not scope this.
         * Minimum size is one byte (8 bits) though.
         */
        return 8;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public StringDefinition createDefinition(@Nullable IDefinitionScope definitionScope,
            String fieldName, BitBuffer input) throws CTFException {
        String value = read(input);
        return new StringDefinition(this, definitionScope, fieldName, value);
    }

    private String read(BitBuffer input) throws CTFException {
        /* Offset the buffer position wrt the current alignment */
        alignRead(input);

        StringBuilder sb = new StringBuilder();
        char c = (char) input.get(BITS_PER_BYTE, false);
        while (c != 0) {
            sb.append(c);
            c = (char) input.get(BITS_PER_BYTE, false);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        /* Only used for debugging */
        return "[declaration] string[" + Integer.toHexString(hashCode()) + ']'; //$NON-NLS-1$
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime;
        switch (fEncoding) {
        case ASCII:
            result += 1;
            break;
        case NONE:
            result += 2;
            break;
        case UTF8:
            result += 3;
            break;
        default:
            break;
        }
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StringDeclaration other = (StringDeclaration) obj;
        if (fEncoding != other.fEncoding) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isBinaryEquivalent(@Nullable IDeclaration other) {
        return equals(other);
    }

}
