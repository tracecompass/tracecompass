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
public class StringDeclaration implements IDeclaration {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private Encoding encoding = Encoding.UTF8;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Generate a UTF8 string declaration
     */
    public StringDeclaration() {
    }

    /**
     * Generate an encoded string declaration
     * @param encoding the encoding, utf8 or ascii
     */
    public StringDeclaration(Encoding encoding) {
        this.encoding = encoding;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     *
     * @return the character encoding.
     */
    public Encoding getEncoding() {
        return encoding;
    }

    /**
     *
     * @param encoding the character encoding to set
     */
    public void setEncoding(Encoding encoding) {
        this.encoding = encoding;
    }

    @Override
    public long getAlignment() {
        // See ctf 4.2.5: Strings are always aligned on byte size.
        return 8;
    }
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public StringDefinition createDefinition(IDefinitionScope definitionScope,
            String fieldName) {
        return new StringDefinition(this, definitionScope, fieldName);
    }

    @Override
    public String toString() {
        /* Only used for debugging */
        return "[declaration] string[" + Integer.toHexString(hashCode()) + ']'; //$NON-NLS-1$
    }

}
