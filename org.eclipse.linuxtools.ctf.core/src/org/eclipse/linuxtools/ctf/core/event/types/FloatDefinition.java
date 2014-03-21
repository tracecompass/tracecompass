/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.types;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;

/**
 * A CTF float definition.
 *
 * The definition of a floating point basic data type. It will take the data
 * from a trace and store it (and make it fit) as a double.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public final class FloatDefinition extends Definition {
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final double fValue;

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
     *            field value
     * @since 3.0
     */
    public FloatDefinition(@NonNull FloatDeclaration declaration,
            IDefinitionScope definitionScope, @NonNull String fieldName, double value) {
        super(declaration, definitionScope, fieldName);
        fValue = value;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * The value of a float stored, fit into a double. This should be extended
     * for exotic floats if this is necessary.
     *
     * @return the value of the float field fit into a double.
     */
    public double getValue() {
        return fValue;
    }

    @Override
    public FloatDeclaration getDeclaration() {
        return (FloatDeclaration) super.getDeclaration();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return String.valueOf(fValue);
    }
}
