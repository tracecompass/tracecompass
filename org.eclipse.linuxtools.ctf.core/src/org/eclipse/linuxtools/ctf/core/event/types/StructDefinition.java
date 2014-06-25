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
public final class StructDefinition extends ScopedDefinition {

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
     * *DEPRECATED* TODO: To remove once we break the API...
     *
     * Not marked with the annotation to not annoy callers using a List, which
     * is still as valid with the new constructor. But the compiler gives an
     * error even though a Iterable is a List too...
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
     * @since 3.1
     */
    public StructDefinition(@NonNull StructDeclaration declaration,
            IDefinitionScope definitionScope,
            @NonNull String structFieldName,
            List<String> fieldNames,
            Definition[] definitions) {
        this(declaration, definitionScope, structFieldName, (Iterable<String>) fieldNames, definitions);
    }

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
     * @since 3.1
     */
    public StructDefinition(@NonNull StructDeclaration declaration,
            IDefinitionScope definitionScope,
            @NonNull String structFieldName,
            Iterable<String> fieldNames,
            Definition[] definitions) {
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
            /* Build the definitions map */
            Builder<String, Definition> mapBuilder = new ImmutableMap.Builder<>();
            for (int i = 0; i < fFieldNames.size(); i++) {
                if (fDefinitions[i] != null) {
                    mapBuilder.put(fFieldNames.get(i), fDefinitions[i]);
                }
            }
            fDefinitionsMap = mapBuilder.build();
        }

        return fDefinitionsMap.get(fieldName);
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
