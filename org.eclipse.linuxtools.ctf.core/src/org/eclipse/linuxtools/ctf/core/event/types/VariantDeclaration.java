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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

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
public class VariantDeclaration extends Declaration {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private String fTag = null;
    private static final long ALIGNMENT = 1;
    private final Map<String, IDeclaration> fFields = Collections.synchronizedMap(new HashMap<String, IDeclaration>());
    private EnumDefinition fTagDef;
    private IDeclaration fDeclarationToPopulate;
    private IDefinitionScope fPrevDefinitionScope;

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
        return fTag != null;
    }

    /**
     * Lookup if a field exists in the variant
     *
     * @param fieldTag
     *            the field tag name
     * @return true = field tag exists
     */
    public boolean hasField(String fieldTag) {
        return fFields.containsKey(fieldTag);
    }

    /**
     * Sets the tag in a variant
     *
     * @param tag
     *            the tag
     */
    public void setTag(String tag) {
        fTag = tag;
        fTagDef = null;
    }

    /**
     * Gets current variant tag
     *
     * @return the variant tag.
     */
    public String getTag() {
        return fTag;
    }

    /**
     * Gets the fields of the variant
     *
     * @return the fields of the variant
     * @since 2.0
     */
    public Map<String, IDeclaration> getFields() {
        return this.fFields;
    }

    @Override
    public long getAlignment() {
        return ALIGNMENT;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * @since 3.0
     */
    @Override
    public VariantDefinition createDefinition(IDefinitionScope definitionScope,
            String fieldName, BitBuffer input) throws CTFReaderException {
        alignRead(input);
        if (fPrevDefinitionScope != definitionScope) {
            fTagDef = null;
            fPrevDefinitionScope = definitionScope;
        }
        EnumDefinition tagDef = fTagDef;
        if (tagDef == null) {
            Definition def = definitionScope.lookupDefinition(fTag);
            tagDef = (EnumDefinition) ((def instanceof EnumDefinition) ? def : null);
        }
        if (tagDef == null) {
            throw new CTFReaderException("Tag is not defined " + fTag); //$NON-NLS-1$
        }
        String varFieldName = tagDef.getStringValue();
        fDeclarationToPopulate = fFields.get(varFieldName);
        if (fDeclarationToPopulate == null) {
            throw new CTFReaderException("Unknown enum selector for variant " + //$NON-NLS-1$
                    definitionScope.getScopePath().toString());
        }
        Definition fieldValue = fDeclarationToPopulate.createDefinition(definitionScope, fieldName, input);
        return new VariantDefinition(this, definitionScope, varFieldName, fieldName, fieldValue);
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
        fFields.put(fieldTag, declaration);
    }

    /**
     * gets the tag definition
     *
     * @return the fTagDef
     * @since 3.0
     */
    public EnumDefinition getTagDef() {
        return fTagDef;
    }

    /**
     * @since 3.0
     */
    @Override
    public int getMaximumSize() {
        Collection<IDeclaration> values = fFields.values();
        int maxSize = 0;
        for (IDeclaration field : values) {
            maxSize = Math.max(maxSize, field.getMaximumSize());
        }
        return maxSize;
    }

    @Override
    public String toString() {
        /* Only used for debugging */
        return "[declaration] variant[" + Integer.toHexString(hashCode()) + ']'; //$NON-NLS-1$
    }

}
