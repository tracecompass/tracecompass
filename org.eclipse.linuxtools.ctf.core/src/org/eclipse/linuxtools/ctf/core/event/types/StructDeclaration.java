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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.scope.LexicalScope;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

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
    private final @NonNull Map<String, IDeclaration> fFieldMap = new LinkedHashMap<>();

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
    public StructDeclaration(long align) {
        fMaxAlign = Math.max(align, 1);
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
    public StructDeclaration(String[] names, Declaration[] declarations) {
        fMaxAlign = 1;

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
     * Get the fields of the struct as a map.
     *
     * @return a Map of the fields (key is the name)
     * @since 2.0
     */
    public Map<String, IDeclaration> getFields() {
        return fFieldMap;
    }

    /**
     * Get the field declaration corresponding to a field name.
     *
     * @param fieldName
     *            The field name
     * @return The declaration of the field, or null if there is no such field.
     * @since 3.1
     */
    @Nullable
    public IDeclaration getField(String fieldName) {
        return fFieldMap.get(fieldName);
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
        for (IDeclaration field : fFieldMap.values()) {
            maxSize += field.getMaximumSize();
        }
        return Math.min(maxSize, Integer.MAX_VALUE);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * @since 3.0
     */
    @Override
    public StructDefinition createDefinition(IDefinitionScope definitionScope,
            String fieldName, BitBuffer input) throws CTFReaderException {
        alignRead(input);
        final Definition[] myFields = new Definition[fFieldMap.size()];
        StructDefinition structDefinition = new StructDefinition(this, definitionScope, fieldName, fFieldMap.keySet(), myFields);
        fillStruct(input, myFields, structDefinition);
        return structDefinition;
    }

    /**
     * Create a definition from this declaration. This is a faster constructor
     * as it has a lexical scope and this does not need to look it up.
     *
     * @param definitionScope
     *            the definition scope, the parent where the definition will be
     *            placed
     * @param fieldScope
     *            the scope of the definition
     * @param input
     *            a bitbuffer to read from
     * @return a reference to the definition
     * @throws CTFReaderException
     *             error in reading
     * @since 3.1
     */
    public StructDefinition createDefinition(IDefinitionScope definitionScope,
            LexicalScope fieldScope, @NonNull BitBuffer input) throws CTFReaderException {
        alignRead(input);
        final Definition[] myFields = new Definition[fFieldMap.size()];
        /*
         * Key set is NOT null
         */
        @SuppressWarnings("null")
        StructDefinition structDefinition = new StructDefinition(this, definitionScope, fieldScope, fieldScope.getName(), fFieldMap.keySet(), myFields);
        fillStruct(input, myFields, structDefinition);
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
    public void addField(String name, IDeclaration declaration) {
        fFieldMap.put(name, declaration);
        fMaxAlign = Math.max(fMaxAlign, declaration.getAlignment());
    }

    @SuppressWarnings("null")
    private void fillStruct(@NonNull BitBuffer input, final Definition[] myFields, StructDefinition structDefinition) throws CTFReaderException {
        Iterator<Map.Entry<String, IDeclaration>> iter = fFieldMap.entrySet().iterator();
        for (int i = 0; i < fFieldMap.size(); i++) {
            Map.Entry<String, IDeclaration> entry = iter.next();
            myFields[i] = entry.getValue().createDefinition(structDefinition, entry.getKey(), input);
        }
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
