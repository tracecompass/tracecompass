/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.types;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;

/**
 * A CTF string definition (similar to a C null-terminated byte array).
 *
 * Strings are an array of bytes of variable size and are terminated by a '\0'
 * "NULL" character. Their encoding is described in the TSDL meta-data. In
 * absence of encoding attribute information, the default encoding is UTF-8.
 *
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

    @Override
    public long size() {
        return fString.length();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return '\"' + getValue() + '\"';
    }

}
