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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

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
public class VariantDefinition extends Definition implements IDefinitionScope {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private VariantDeclaration declaration;

    private EnumDefinition tagDefinition;
    private Map<String, Definition> definitions = new HashMap<String, Definition>();
    private String currentField;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param declaration the parent declaration
     * @param definitionScope the parent scope
     * @param fieldName the field name
     */
    public VariantDefinition(VariantDeclaration declaration,
            IDefinitionScope definitionScope, String fieldName) {
        super(definitionScope, fieldName);

        this.declaration = declaration;

        Definition tagDef = definitionScope.lookupDefinition(declaration.getTag());
        this.tagDefinition = (EnumDefinition) tagDef;

        for (Map.Entry<String, IDeclaration> field : declaration.getFields().entrySet()) {
            Definition fieldDef = field.getValue().createDefinition(this,
                    field.getKey());
            definitions.put(field.getKey(), fieldDef);
        }
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    @Override
    public VariantDeclaration getDeclaration() {
        return declaration;
    }

    /**
     * Sets the variant declaration
     * @param declaration the variant declaration
     */
    public void setDeclaration(VariantDeclaration declaration) {
        this.declaration = declaration;
    }

    /**
     * Gets the tag
     * @return the tag definition
     */
    public EnumDefinition getTagDefinition() {
        return tagDefinition;
    }

    /**
     * Sets the tag
     * @param tagDefinition the tag
     */
    public void setTagDefinition(EnumDefinition tagDefinition) {
        this.tagDefinition = tagDefinition;
    }

    /**
     * Get the definitions in the variant
     * @return the definitions
     * @since 2.0
     */
    public Map<String, Definition> getDefinitions() {
        return definitions;
    }

    /**
     * Set the definitions in a variant
     * @param definitions the definitions
     * @since 2.0
     */
    public void setDefinitions(Map<String, Definition> definitions) {
        this.definitions = definitions;
    }

    /**
     * Set the current field
     * @param currentField the current field
     */
    public void setCurrentField(String currentField) {
        this.currentField = currentField;
    }

    /**
     * Get the current field name
     * @return the current field name
     */
    public String getCurrentFieldName() {
        return currentField;
    }

    /**
     * Get the current field
     * @return the current field
     */
    public Definition getCurrentField() {
        return definitions.get(currentField);
    }


    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void read(BitBuffer input) throws CTFReaderException {
        currentField = tagDefinition.getValue();

        Definition field = definitions.get(currentField);
        if (field == null) {
            throw new CTFReaderException("Variant was not defined for: "+ currentField); //$NON-NLS-1$
        }
        field.read(input);
    }

    @Override
    public Definition lookupDefinition(String lookupPath) {
        return definitions.get(lookupPath);
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
     * Lookup a string in a struct. if the name returns a non-string (like
     * an int) than the method returns null
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
     * Lookup a struct in a struct. if the name returns a non-struct (like
     * an int) than the method returns null
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
     * Lookup a variant in a struct. if the name returns a non-variant (like
     * an int) than the method returns null
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
