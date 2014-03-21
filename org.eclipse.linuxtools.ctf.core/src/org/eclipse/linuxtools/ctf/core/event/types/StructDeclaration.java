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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

import com.google.common.collect.ImmutableList;

/**
 * A CTF structure declaration.
 *
 * A structure is similar to a C structure, it is a compound data type that
 * contains other datatypes in fields. they are stored in an hashmap and indexed
 * by names which are strings.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public class StructDeclaration extends Declaration {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** linked list of field names. So fieldName->fieldValue */
    private final Map<String, IDeclaration> fFieldMap = new LinkedHashMap<>();

    /** List of strings for acceleration */
    @NonNull
    private ImmutableList<String> fFieldNames;
    /** array declaration for acceleration */
    private List<IDeclaration> fFieldDeclarations;

    /** maximum bit alignment */
    private long fMaxAlign;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The struct declaration, add fields later
     *
     * @param align
     *            the minimum alignment of the struct. (if a struct is 8bit
     *            aligned and has a 32 bit aligned field, the struct becomes 32
     *            bit aligned.
     */
    @SuppressWarnings("null")
    // ImmutableList.of()
    public StructDeclaration(long align) {
        fMaxAlign = Math.max(align, 1);
        fFieldNames = ImmutableList.of();
    }

    /**
     * Struct declaration constructor
     *
     * @param names
     *            the names of all the fields
     * @param declarations
     *            all the fields
     * @since 3.0
     */
    @SuppressWarnings("null")
    // ImmutableList.of()
    public StructDeclaration(String[] names, Declaration[] declarations) {
        fMaxAlign = 1;
        fFieldNames = ImmutableList.of();
        for (int i = 0; i < names.length; i++) {
            addField(names[i], declarations[i]);
        }
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Get current alignment
     *
     * @return the alignment of the struct and all its fields
     */
    public long getMaxAlign() {
        return fMaxAlign;
    }

    /**
     * Query if the struct has a given field
     *
     * @param name
     *            the name of the field, scopeless please
     * @return does the field exist?
     */
    public boolean hasField(String name) {
        return fFieldMap.containsKey(name);
    }

    /**
     * get the fields of the struct in a map. Faster access time than a list.
     *
     * @return a HashMap of the fields (key is the name)
     * @since 2.0
     */
    public Map<String, IDeclaration> getFields() {
        return fFieldMap;
    }

    /**
     * Gets the field list. Very important since the map of fields does not
     * retain the order of the fields.
     *
     * @return the field list.
     * @since 3.0
     */
    public Iterable<String> getFieldsList() {
        return fFieldMap.keySet();
    }

    @Override
    public long getAlignment() {
        return this.fMaxAlign;
    }

    /**
     * @since 3.0
     */
    @Override
    public int getMaximumSize() {
        int maxSize = 0;
        if (fFieldDeclarations != null) {
            for (IDeclaration field : fFieldDeclarations) {
                maxSize += field.getMaximumSize();
            }
        }
        return Math.min(maxSize, Integer.MAX_VALUE);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * @since 3.0
     */
    @SuppressWarnings("null")
    // immutablelist
    @Override
    public StructDefinition createDefinition(IDefinitionScope definitionScope,
            String fieldName, BitBuffer input) throws CTFReaderException {
        alignRead(input);
        final Definition[] myFields = new Definition[fFieldNames.size()];
        StructDefinition structDefinition = new StructDefinition(this, definitionScope, fieldName, fFieldNames, myFields);
        for (int i = 0; i < fFieldNames.size(); i++) {
            myFields[i] = fFieldDeclarations.get(i).createDefinition(structDefinition, fFieldNames.get(i), input);
        }
        return structDefinition;
    }

    /**
     * Add a field to the struct
     *
     * @param name
     *            the name of the field, scopeless
     * @param declaration
     *            the declaration of the field
     */
    @SuppressWarnings("null")
    // Immutable list copyof cannot return null
    public void addField(String name, IDeclaration declaration) {
        fFieldMap.put(name, declaration);
        fMaxAlign = Math.max(fMaxAlign, declaration.getAlignment());
        fFieldNames = ImmutableList.copyOf(fFieldMap.keySet());
        fFieldDeclarations = ImmutableList.<IDeclaration>copyOf(fFieldMap.values());
    }

    @Override
    public String toString() {
        /* Only used for debugging */
        return "[declaration] struct[" + Integer.toHexString(hashCode()) + ']'; //$NON-NLS-1$
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + fFieldMap.entrySet().hashCode();
        result = (prime * result) + (int) (fMaxAlign ^ (fMaxAlign >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof StructDeclaration)) {
            return false;
        }
        StructDeclaration other = (StructDeclaration) obj;
        if (!fFieldMap.entrySet().equals(other.fFieldMap.entrySet())) {
            return false;
        }
        if (fMaxAlign != other.fMaxAlign) {
            return false;
        }
        return true;
    }

}
