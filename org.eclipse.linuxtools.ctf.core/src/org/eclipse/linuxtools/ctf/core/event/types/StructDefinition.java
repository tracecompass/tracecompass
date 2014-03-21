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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

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
public final class StructDefinition extends Definition implements IDefinitionScope {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final ImmutableList<String> fFieldNames;
    private final Definition[] fDefinitions;
    private Map<String, Definition> fDefinitionsMap = null;

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
     * @param fieldNames
     *            the list of fields
     * @param definitions
     *            the definitions
     * @since 3.0
     */
    public StructDefinition(@NonNull StructDeclaration declaration,
            IDefinitionScope definitionScope, @NonNull String structFieldName, List<String> fieldNames, Definition[] definitions) {
        super(declaration, definitionScope, structFieldName);
        fFieldNames = ImmutableList.copyOf(fieldNames);
        fDefinitions = definitions;
        if (fFieldNames == null) {
            fDefinitionsMap = Collections.EMPTY_MAP;
        }
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Gets the definition of the field
     *
     * @param fieldName
     *            the fieldname
     * @return The definitions of all the fields
     * @since 3.0
     */
    public Definition getDefinition(String fieldName) {
        if (fDefinitionsMap == null) {
            buildFieldsMap();
        }
        return fDefinitionsMap.get(fieldName);
    }

    private void buildFieldsMap() {
        Builder<String, Definition> mapBuilder = new ImmutableMap.Builder<>();
        for (int i = 0; i < fFieldNames.size(); i++) {
            if (fDefinitions[i] != null) {
                mapBuilder.put(fFieldNames.get(i), fDefinitions[i]);
            }
        }
        fDefinitionsMap = mapBuilder.build();
    }

    /**
     * Gets an array of the field names
     *
     * @return the field names array
     * @since 3.0
     */
    public List<String> getFieldNames() {
        return fFieldNames;
    }

    @Override
    public StructDeclaration getDeclaration() {
        return (StructDeclaration) super.getDeclaration();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Definition lookupDefinition(String lookupPath) {
        /*
         * The fields are created in order of appearance, so if a variant or
         * sequence refers to a field that is after it, the field's definition
         * will not be there yet in the hashmap.
         */
        int val = fFieldNames.indexOf(lookupPath);
        if (val != -1) {
            return fDefinitions[val];
        }
        String lookupUnderscored = "_" + lookupPath; //$NON-NLS-1$
        val = fFieldNames.indexOf(lookupUnderscored);
        if (val != -1) {
            return fDefinitions[val];
        }
        return null;
    }

    /**
     * Lookup an array in a struct. If the name returns a non-array (like an
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
     * Lookup an enum in a struct. If the name returns a non-enum (like an int)
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
     * Lookup an integer in a struct. If the name returns a non-integer (like an
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
     * Lookup a sequence in a struct. If the name returns a non-sequence (like
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
     * Lookup a string in a struct. If the name returns a non-string (like an
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
     * Lookup a struct in a struct. If the name returns a non-struct (like an
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
     * Lookup a variant in a struct. If the name returns a non-variant (like an
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
        StringBuilder builder = new StringBuilder();

        builder.append("{ "); //$NON-NLS-1$

        if (fFieldNames != null) {
            List<String> fields = new LinkedList<>();
            for (String field : fFieldNames) {
                String appendee = field + " = " + lookupDefinition(field).toString(); //$NON-NLS-1$
                fields.add(appendee);
            }
            Joiner joiner = Joiner.on(", ").skipNulls(); //$NON-NLS-1$
            builder.append(joiner.join(fields));
        }

        builder.append(" }"); //$NON-NLS-1$

        return builder.toString();
    }

}
