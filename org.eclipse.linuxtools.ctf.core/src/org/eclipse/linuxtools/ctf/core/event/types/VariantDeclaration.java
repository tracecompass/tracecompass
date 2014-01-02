/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
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

/**
 * A CTFC variant declaration.
 *
 * A variant is similar to a C union, only taking the minimum size of the types,
 * it is a compound data type that contains other datatypes in fields. they are
 * stored in an hashmap and indexed by names which are strings.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public class VariantDeclaration implements IDeclaration {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private String tag = null;
    private static final long alignment = 1;
    private final Map<String, IDeclaration> fields = new HashMap<>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public VariantDeclaration() {
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * @return Does the variant have a tag
     */
    public boolean isTagged() {
        return tag != null;
    }

    /**
     * Lookup if a field exists in the variant
     * @param fieldTag the field tag name
     * @return true = field tag exists
     */
    public boolean hasField(String fieldTag) {
        return fields.containsKey(fieldTag);
    }

    /**
     * Sets the tag in a variant
     * @param tag the tag
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Gets current variant tag
     * @return the variant tag.
     */
    public String getTag() {
        return this.tag;
    }

    /**
     * Gets the fields of the variant
     * @return the fields of the variant
     * @since 2.0
     */
    public Map<String, IDeclaration> getFields() {
        return this.fields;
    }

    @Override
    public long getAlignment() {
        return alignment;
    }
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public VariantDefinition createDefinition(IDefinitionScope definitionScope,
            String fieldName) {
        return new VariantDefinition(this, definitionScope, fieldName);
    }


    /**
     * Add a field to this CTF Variant
     *
     * @param fieldTag
     *            The tag of the new field
     * @param declaration
     *            The Declaration of this new field
     */
    public void addField(String fieldTag, IDeclaration declaration) {
        fields.put(fieldTag, declaration);
    }

    @Override
    public String toString() {
        /* Only used for debugging */
        return "[declaration] variant[" + Integer.toHexString(hashCode()) + ']'; //$NON-NLS-1$
    }

}
