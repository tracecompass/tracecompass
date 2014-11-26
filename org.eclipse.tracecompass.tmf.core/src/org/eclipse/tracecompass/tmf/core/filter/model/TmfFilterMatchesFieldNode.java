/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.filter.model;

import java.util.regex.Pattern;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Filter node that matches on an event field (using a field ID).
 *
 * @author Alexandre Montplaisir
 */
public final class TmfFilterMatchesFieldNode extends TmfFilterMatchesNode {

    /** Name/ID of this node */
    public static final String NODE_NAME = "MATCHES"; //$NON-NLS-1$
    /** The string attribute of this node type */
    public static final String FIELD_ATTR = "field"; //$NON-NLS-1$

    private String fField;

    /**
     * Constructor
     *
     * @param parent
     *            The parent node
     */
    public TmfFilterMatchesFieldNode(ITmfFilterTreeNode parent) {
        super(parent);
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public boolean matches(ITmfEvent event) {
        Pattern pattern = getPattern();
        boolean isNot = isNot();

        if (pattern == null) {
            return false ^ isNot;
        }

        Object value = getFieldValue(event, fField);
        if (value == null) {
            return false ^ isNot;
        }
        String valueString = value.toString();

        return pattern.matcher(valueString).matches() ^ isNot;
    }

    /**
     * @return the field name
     */
    public String getField() {
        return fField;
    }

    /**
     * @param field
     *            the field name
     */
    public void setField(String field) {
        this.fField = field;
    }

    @Override
    public ITmfFilterTreeNode clone() {
        TmfFilterMatchesFieldNode clone = (TmfFilterMatchesFieldNode) super.clone();
        clone.fField = fField;
        return clone;
    }

    @Override
    public String toString() {
        return fField + (isNot() ? " not" : "") + " matches \"" + getRegex() + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fField == null) ? 0 : fField.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TmfFilterMatchesFieldNode other = (TmfFilterMatchesFieldNode) obj;
        if (fField == null) {
            if (other.fField != null) {
                return false;
            }
        } else if (!fField.equals(other.fField)) {
            return false;
        }
        return true;
    }

}
