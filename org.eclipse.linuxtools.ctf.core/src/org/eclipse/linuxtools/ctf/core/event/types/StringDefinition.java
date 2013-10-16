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

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;

/**
 * A CTF string definition (similar to a C null-terminated byte array).
 *
 * Strings are an array of bytes of variable size and are terminated by a '\0'
 * "NULL" character. Their encoding is described in the TSDL meta-data. In
 * absence of encoding attribute information, the default encoding is UTF-8.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public class StringDefinition extends Definition {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private StringDeclaration declaration;

    private StringBuilder string;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param declaration the parent declaration
     * @param definitionScope the parent scope
     * @param fieldName the field name
     */
    public StringDefinition(StringDeclaration declaration,
            IDefinitionScope definitionScope, String fieldName) {
        super(definitionScope, fieldName);

        this.declaration = declaration;

        string = new StringBuilder();
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    @Override
    public StringDeclaration getDeclaration() {
        return declaration;
    }

    /**
     * Sets the string declaration
     * @param declaration the declaration
     */
    public void setDeclaration(StringDeclaration declaration) {
        this.declaration = declaration;
    }

    /**
     * Gets the string
     * @return the stringbuilder
     */
    public StringBuilder getString() {
        return string;
    }

    /**
     * Sets a stringbuilder for the definition
     * @param string the stringbuilder
     */
    public void setString(StringBuilder string) {
        this.string = string;
    }

    /**
     * Gets the string (value)
     * @return the string
     */
    public String getValue() {
        return string.toString();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void read(BitBuffer input) {
        /* Offset the buffer position wrt the current alignment */
        int align = (int) declaration.getAlignment();
        int pos = input.position() + ((align - (input.position() % align)) % align);
        input.position(pos);

        string.setLength(0);

        char c = (char) input.getInt(8, false);
        while (c != 0) {
            string.append(c);
            c = (char) input.getInt(8, false);
        }
    }

    @Override
    public String toString() {
        return '\"' + getValue() + '\"';
    }

}
