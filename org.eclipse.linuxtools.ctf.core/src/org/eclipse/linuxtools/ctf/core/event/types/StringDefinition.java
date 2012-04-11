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

import org.eclipse.linuxtools.internal.ctf.core.event.io.BitBuffer;

/**
 * <b><u>StringDefinition</u></b>
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

    public StringDefinition(StringDeclaration declaration,
            IDefinitionScope definitionScope, String fieldName) {
        super(definitionScope, fieldName);

        this.declaration = declaration;

        string = new StringBuilder();
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    public StringDeclaration getDeclaration() {
        return declaration;
    }

    public void setDeclaration(StringDeclaration declaration) {
        this.declaration = declaration;
    }

    public StringBuilder getString() {
        return string;
    }

    public void setString(StringBuilder string) {
        this.string = string;
    }

    public String getValue() {
        return string.toString();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void read(BitBuffer input) {
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
