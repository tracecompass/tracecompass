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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
public class StructDeclaration implements IDeclaration {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final Map<String, IDeclaration> fields = new HashMap<String, IDeclaration>();
    private final List<String> fieldsList = new LinkedList<String>();
    private long maxAlign;

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
        this.maxAlign = Math.max(align, 1);
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Get current alignment
     * @return the alignment of the struct and all its fields
     */
    public long getMaxAlign() {
        return maxAlign;
    }

    /**
     * Query if the struct has a given field
     * @param name the name of the field, scopeless please
     * @return does the field exist?
     */
    public boolean hasField(String name) {
        return this.fields.containsKey(name);
    }

    /**
     * get the fields of the struct in a map. Faster access time than a list.
     * @return a HashMap of the fields (key is the name)
     * @since 2.0
     */
    public Map<String, IDeclaration> getFields() {
        return this.fields;
    }

    /**
     * Gets the field list. Very important since the map of fields does not retain the order of the fields.
     * @return the field list.
     */
    public List<String> getFieldsList() {
        return this.fieldsList;
    }

    @Override
    public long getAlignment() {
        return this.maxAlign;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public StructDefinition createDefinition(IDefinitionScope definitionScope,
            String fieldName) {
        return new StructDefinition(this, definitionScope, fieldName);
    }

    /**
     * Add a field to the struct
     * @param name the name of the field, scopeless
     * @param declaration the declaration of the field
     */
    public void addField(String name, IDeclaration declaration) {
        this.fields.put(name, declaration);
        this.fieldsList.add(name);
        maxAlign = Math.max(maxAlign, declaration.getAlignment());
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
        result = (prime * result) + fieldsList.hashCode();
        result = (prime * result) + (int) (maxAlign ^ (maxAlign >>> 32));
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
        if (!fieldsList.equals(other.fieldsList)) {
            return false;
        }
        if (maxAlign != other.maxAlign) {
            return false;
        }
        return true;
    }

}
