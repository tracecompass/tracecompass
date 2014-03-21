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
public final class VariantDefinition extends Definition implements IDefinitionScope {

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

    /**
     * Lookup an array in a struct. if the name returns a non-array (like an
     * int) than the method returns null
     *
     * @param name
     *            the name of the array
     * @return the array or null.
     */
    public ArrayDefinition lookupArray(String name) {
        Definition def = lookupDefinition(name);
        return (ArrayDefinition) ((def instanceof ArrayDefinition) ? def : null);
    }

    /**
     * Lookup an enum in a struct. if the name returns a non-enum (like an int)
     * than the method returns null
     *
     * @param name
     *            the name of the enum
     * @return the enum or null.
     */
    public EnumDefinition lookupEnum(String name) {
        Definition def = lookupDefinition(name);
        return (EnumDefinition) ((def instanceof EnumDefinition) ? def : null);
    }

    /**
     * Lookup an integer in a struct. if the name returns a non-integer (like an
     * float) than the method returns null
     *
     * @param name
     *            the name of the integer
     * @return the integer or null.
     */
    public IntegerDefinition lookupInteger(String name) {
        Definition def = lookupDefinition(name);
        return (IntegerDefinition) ((def instanceof IntegerDefinition) ? def
                : null);
    }

    /**
     * Lookup a sequence in a struct. if the name returns a non-sequence (like
     * an int) than the method returns null
     *
     * @param name
     *            the name of the sequence
     * @return the sequence or null.
     */
    public SequenceDefinition lookupSequence(String name) {
        Definition def = lookupDefinition(name);
        return (SequenceDefinition) ((def instanceof SequenceDefinition) ? def
                : null);
    }

    /**
     * Lookup a string in a struct. if the name returns a non-string (like an
     * int) than the method returns null
     *
     * @param name
     *            the name of the string
     * @return the string or null.
     */
    public StringDefinition lookupString(String name) {
        Definition def = lookupDefinition(name);
        return (StringDefinition) ((def instanceof StringDefinition) ? def
                : null);
    }

    /**
     * Lookup a struct in a struct. if the name returns a non-struct (like an
     * int) than the method returns null
     *
     * @param name
     *            the name of the struct
     * @return the struct or null.
     */
    public StructDefinition lookupStruct(String name) {
        Definition def = lookupDefinition(name);
        return (StructDefinition) ((def instanceof StructDefinition) ? def
                : null);
    }

    /**
     * Lookup a variant in a struct. if the name returns a non-variant (like an
     * int) than the method returns null
     *
     * @param name
     *            the name of the variant
     * @return the variant or null.
     */
    public VariantDefinition lookupVariant(String name) {
        Definition def = lookupDefinition(name);
        return (VariantDefinition) ((def instanceof VariantDefinition) ? def
                : null);
    }

    @Override
    public String toString() {
        return "{ " + getCurrentFieldName() + //$NON-NLS-1$
                " = " + getCurrentField() + //$NON-NLS-1$
                " }"; //$NON-NLS-1$
    }
}
