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

/**
 * <b><u>VariantDeclaration</u></b>
 */
public class VariantDeclaration implements IDeclaration {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private String tag = null;
    private long alignment;
    private final HashMap<String, IDeclaration> fields = new HashMap<String, IDeclaration>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public VariantDeclaration() {
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    public boolean isTagged() {
        return tag != null;
    }

    public boolean hasField(String fieldTag) {
        return fields.containsKey(fieldTag);
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return this.tag;
    }

    public HashMap<String, IDeclaration> getFields() {
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

    public void addField(String fieldTag, IDeclaration declaration) {
        fields.put(fieldTag, declaration);
        alignment = Math.max(alignment, declaration.getAlignment());
    }

    @Override
    public String toString() {
        /* Only used for debugging */
        return "[declaration] variant[" + Integer.toHexString(hashCode()) + ']'; //$NON-NLS-1$
    }

}
