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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;

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
public final class StringDefinition extends Definition {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final String fString;

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
     *            The String value
     * @since 3.0
     */
    public StringDefinition(@NonNull StringDeclaration declaration,
            IDefinitionScope definitionScope, @NonNull String fieldName, String value) {
        super(declaration, definitionScope, fieldName);
        fString = value;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    @Override
    public StringDeclaration getDeclaration() {
        return (StringDeclaration) super.getDeclaration();
    }

    /**
     * Gets the string (value)
     *
     * @return the string
     */
    public String getValue() {
        return fString;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return '\"' + getValue() + '\"';
    }

}
