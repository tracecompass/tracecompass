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
 * <b><u>EnumDefinition</u></b>
 */
public class EnumDefinition extends Definition {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final EnumDeclaration declaration;

    private final IntegerDefinition integerValue;

    private String value;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public EnumDefinition(EnumDeclaration declaration,
            IDefinitionScope definitionScope, String fieldName) {
        super(definitionScope, fieldName);

        this.declaration = declaration;

        integerValue = declaration.getContainerType().createDefinition(
                definitionScope, fieldName);
        value = ((Long) integerValue.getValue()).toString();
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    public String getValue() {
        return value;
    }

    public long getIntegerValue() {
        return integerValue.getValue();
    }

    public void setIntegerValue(long Value) {
        integerValue.setValue(Value);
        value = ((Long) integerValue.getValue()).toString();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void read(BitBuffer input) {
        integerValue.read(input);
        long val = integerValue.getValue();

        // TODO: what to do if the integer value maps to no string for this
        // integer ?
        value = declaration.query(val);
    }

}
