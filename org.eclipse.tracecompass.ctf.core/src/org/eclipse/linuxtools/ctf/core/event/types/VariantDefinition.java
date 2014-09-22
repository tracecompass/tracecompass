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

package org.eclipse.linuxtools.ctf.core.event.types;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;

/**
 * A CTF variant definition (similar to a C union).
 *
 * A variant is similar to a C union, only taking the minimum size of the types,
 * it is a compound data type that contains other datatypes in fields. they are
 * stored in an hashmap and indexed by names which are strings.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public final class VariantDefinition extends ScopedDefinition {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final Definition fDefinition;
    private final String fCurrentField;
    private final String fFieldName;

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
     * @param selectedField
     *            the selected field
     * @param fieldName
     *            the field name
     * @param fieldValue
     *            the field value
     * @since 3.0
     */
    public VariantDefinition(@NonNull VariantDeclaration declaration,
            IDefinitionScope definitionScope, String selectedField, @NonNull String fieldName, Definition fieldValue) {
        super(declaration, definitionScope, fieldName);

        fFieldName = fieldName;
        fCurrentField = selectedField;
        fDefinition = fieldValue;

    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    @Override
    public VariantDeclaration getDeclaration() {
        return (VariantDeclaration) super.getDeclaration();
    }

    /**
     * Get the current field name
     *
     * @return the current field name
     */
    public String getCurrentFieldName() {
        return fCurrentField;
    }

    /**
     * Get the current field
     *
     * @return the current field
     */
    public Definition getCurrentField() {
        return fDefinition;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Definition lookupDefinition(String lookupPath) {
        if (lookupPath == null) {
            return null;
        }
        if (lookupPath.equals(fFieldName)) {
            return fDefinition;
        }
        return getDefinitionScope().lookupDefinition(lookupPath);
    }

    @Override
    public String toString() {
        return "{ " + getCurrentFieldName() + //$NON-NLS-1$
                " = " + getCurrentField() + //$NON-NLS-1$
                " }"; //$NON-NLS-1$
    }
}
