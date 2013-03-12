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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;

/**
 * A CTF structure definition (similar to a C structure).
 *
 * A structure is similar to a C structure, it is a compound data type that
 * contains other datatypes in fields. they are stored in an hashmap and indexed
 * by names which are strings.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public class StructDefinition extends Definition implements IDefinitionScope {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final StructDeclaration declaration;
    private final Map<String, Definition> definitions = new LinkedHashMap<String, Definition>();

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
     * @param structFieldName
     *            the field name
     */
    public StructDefinition(StructDeclaration declaration,
            IDefinitionScope definitionScope, String structFieldName) {
        super(definitionScope, structFieldName);

        this.declaration = declaration;

        for (String fName : declaration.getFieldsList()) {
            IDeclaration fieldDecl = declaration.getFields().get(fName);
            assert (fieldDecl != null);

            Definition def = fieldDecl.createDefinition(this, fName);
            definitions.put(fName, def);
        }
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    @Override
    public String getPath() {
        return path;
    }

    /**
     * @return The definitions of all the fields
     * @since 2.0
     */
    public Map<String, Definition> getDefinitions() {
        return definitions;
    }

    @Override
    public StructDeclaration getDeclaration() {
        return declaration;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void read(BitBuffer input) {
        final int align = (int) declaration.getAlignment();
        int pos = input.position()
                + ((align - (input.position() % align)) % align);
        input.position(pos);
        final List<String> fieldList = declaration.getFieldsList();
        for (String fName : fieldList) {
            Definition def = definitions.get(fName);
            assert (def != null);
            def.read(input);
        }
    }

    @Override
    public Definition lookupDefinition(String lookupPath) {
        /*
         * The fields are created in order of appearance, so if a variant or
         * sequence refers to a field that is after it, the field's definition
         * will not be there yet in the hashmap.
         */
        Definition retVal = definitions.get(lookupPath);
        if (retVal == null) {
            retVal = definitions.get("_" + lookupPath); //$NON-NLS-1$
        }
        return retVal;
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
        StringBuilder builder = new StringBuilder();

        builder.append("{ "); //$NON-NLS-1$

        ListIterator<String> listIterator = this.declaration.getFieldsList()
                .listIterator();

        while (listIterator.hasNext()) {
            String field = listIterator.next();

            builder.append(field);
            builder.append(" = "); //$NON-NLS-1$
            builder.append(lookupDefinition(field).toString());

            if (listIterator.hasNext()) {
                builder.append(", "); //$NON-NLS-1$
            }
        }

        builder.append(" }"); //$NON-NLS-1$

        return builder.toString();
    }
}
