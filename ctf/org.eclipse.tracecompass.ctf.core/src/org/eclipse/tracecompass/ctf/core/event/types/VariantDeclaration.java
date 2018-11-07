/*******************************************************************************
 * Copyright (c) 2011, 2015 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.types;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;

/**
 * A CTF C variant declaration.
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
    private IDeclaration fDeclarationToPopulate;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public VariantDeclaration() {
        // Do nothing
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

    @Override
    public VariantDefinition createDefinition(IDefinitionScope definitionScope,
            String fieldName, BitBuffer input) throws CTFException {
        alignRead(input);
        IDefinition def = definitionScope.lookupDefinition(fTag);
        EnumDefinition tagDef = (EnumDefinition) ((def instanceof EnumDefinition) ? def : null);
        if (tagDef == null) {
            throw new CTFException("Tag is not defined " + fTag); //$NON-NLS-1$
        }
        String varFieldName = tagDef.getStringValue();
        if (varFieldName == null) {
            throw new CTFException("Undefined enum selector for variant " + //$NON-NLS-1$
                    definitionScope.getScopePath().getPath());
        }
        fDeclarationToPopulate = fFields.get(varFieldName);
        if (fDeclarationToPopulate == null) {
            throw new CTFException("Unknown enum selector for variant " + //$NON-NLS-1$
                    definitionScope.getScopePath().getPath());
        }
        Definition fieldValue = fDeclarationToPopulate.createDefinition(definitionScope, fieldName, input);
        return new VariantDefinition(this, definitionScope, tagDef, varFieldName, fieldName, fieldValue);
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
        StringBuilder sb = new StringBuilder();
        sb.append("[declaration] variant["); //$NON-NLS-1$
        for (Entry<String, IDeclaration> field : fFields.entrySet()) {
            sb.append(field.getKey()).append(':').append(field.getValue());
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fDeclarationToPopulate == null) ? 0 : fDeclarationToPopulate.hashCode());
        if (fFields == null) {
            result = prime * result;
        } else {
            for (Entry<String, IDeclaration> field : fFields.entrySet()) {
                result = prime * result + field.getValue().hashCode();
            }
        }
        result = prime * result + ((fTag == null) ? 0 : fTag.hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        VariantDeclaration other = (VariantDeclaration) obj;

        if (!Objects.equals(fDeclarationToPopulate, other.fDeclarationToPopulate)) {
            return false;
        }
        // do not check the order of the fields
        if (!Objects.equals(fFields, other.fFields)) {
            return false;
        }
        return (Objects.equals(fTag, other.fTag));
    }

    @Override
    public boolean isBinaryEquivalent(IDeclaration obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VariantDeclaration other = (VariantDeclaration) obj;
        if (fFields == null) {
            if (other.fFields != null) {
                return false;
            }
        } else {
            if (fFields.size() != other.fFields.size()) {
                return false;
            }
            for (Entry<String, IDeclaration> field : fFields.entrySet()) {
                if (!other.fFields.containsKey(field.getKey())) {
                    return false;
                }
                IDeclaration field2 = other.fFields.get(field.getKey());
                if ((field2 == null) || (!field2.isBinaryEquivalent(field.getValue()))) {
                    return false;
                }
            }
        }
        return true;
    }

}
