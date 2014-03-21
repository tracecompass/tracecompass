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

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

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
public class StringDeclaration extends Declaration {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final Encoding fEncoding;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Generate a UTF8 string declaration
     */
    public StringDeclaration() {
        fEncoding = Encoding.UTF8;
    }

    /**
     * Generate an encoded string declaration
     * @param encoding the encoding, utf8 or ascii
     */
    public StringDeclaration(Encoding encoding) {
        fEncoding = encoding;
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
        return 8;
    }

    /**
     * @since 3.0
     */
    @Override
    public int getMaximumSize() {
        return Integer.MAX_VALUE;
    }
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * @since 3.0
     */
    @Override
    public StringDefinition createDefinition(IDefinitionScope definitionScope,
            String fieldName, BitBuffer input) throws CTFReaderException {
        String value = read(input);
        return new StringDefinition(this, definitionScope, fieldName, value);
    }

    private String read(BitBuffer input) throws CTFReaderException {
        /* Offset the buffer position wrt the current alignment */
        alignRead(input);

        StringBuilder sb = new StringBuilder();
        char c = (char) input.get(8, false);
        while (c != 0) {
            sb.append(c);
            c = (char) input.get(8, false);
        }
        return sb.toString();
    }
    @Override
    public String toString() {
        /* Only used for debugging */
        return "[declaration] string[" + Integer.toHexString(hashCode()) + ']'; //$NON-NLS-1$
    }

}
