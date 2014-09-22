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
 * A CTF enum definition.
 *
 * The definition of a enum point basic data type. It will take the data from a
 * trace and store it (and make it fit) as an integer and a string.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public final class EnumDefinition extends SimpleDatatypeDefinition {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final long fIntegerValue;

    private final String fValue;

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
     * @param intValue
     *            the value of the enum
     * @since 3.0
     */
    public EnumDefinition(@NonNull EnumDeclaration declaration,
            IDefinitionScope definitionScope, @NonNull String fieldName, IntegerDefinition intValue) {
        super(declaration, definitionScope, fieldName);

        fIntegerValue = intValue.getValue();
        fValue = declaration.query(fIntegerValue);
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Gets the value of the enum in string format so
     * "Enum a{DAY="0", NIGHT="1"}; will return "DAY"
     *
     * @return the value of the enum.
     */
    public String getValue() {
        return fValue;
    }

    @Override
    public String getStringValue() {
        return getValue();
    }

    /**
     * Gets the value of the enum in string format so
     * "Enum a{DAY="0", NIGHT="1"}; will return 0
     *
     * @return the value of the enum.
     */
    @Override
    public Long getIntegerValue() {
        return fIntegerValue;
    }

    @Override
    public EnumDeclaration getDeclaration() {
        return (EnumDeclaration) super.getDeclaration();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return "{ value = " + getValue() + //$NON-NLS-1$
                ", container = " + fIntegerValue + //$NON-NLS-1$
                " }"; //$NON-NLS-1$
    }
}
