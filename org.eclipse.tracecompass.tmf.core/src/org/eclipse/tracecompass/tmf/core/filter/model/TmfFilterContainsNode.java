/*******************************************************************************
 * Copyright (c) 2010, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.filter.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Filter node for the 'contains' operation
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfFilterContainsNode extends TmfFilterAspectNode {

    /** contains node name */
    public static final String NODE_NAME = "CONTAINS"; //$NON-NLS-1$
    /** not attribute name */
    public static final String NOT_ATTR = "not"; //$NON-NLS-1$
    /** value attribute name */
    public static final String VALUE_ATTR = "value"; //$NON-NLS-1$
    /** ignorecase attribute name */
    public static final String IGNORECASE_ATTR = "ignorecase"; //$NON-NLS-1$

    private boolean fNot = false;
    private String fValue;
    private transient String fValueUpperCase;
    private boolean fIgnoreCase = false;

    /**
     * @param parent the parent node
     */
    public TmfFilterContainsNode(ITmfFilterTreeNode parent) {
        super(parent);
    }

    /**
     * @return the NOT state
     */
    public boolean isNot() {
        return fNot;
    }

    /**
     * @param not the NOT state
     */
    public void setNot(boolean not) {
        this.fNot = not;
    }

    /**
     * @return the contains value
     */
    public String getValue() {
        return fValue;
    }

    /**
     * @param value the contains value
     */
    public void setValue(String value) {
        fValue = value;
        fValueUpperCase = null;
        if (value != null) {
            fValueUpperCase = value.toUpperCase();
        }
    }

    /**
     * @return the ignoreCase state
     */
    public boolean isIgnoreCase() {
        return fIgnoreCase;
    }

    /**
     * @param ignoreCase the ignoreCase state
     */
    public void setIgnoreCase(boolean ignoreCase) {
        this.fIgnoreCase = ignoreCase;
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public boolean matches(ITmfEvent event) {
        if (event == null || fEventAspect == null) {
            return false ^ fNot;
        }
        Object value = fEventAspect.resolve(event);
        if (value == null) {
            return false ^ fNot;
        }
        String valueString = value.toString();
        if (fIgnoreCase) {
            return valueString.toUpperCase().contains(fValueUpperCase) ^ fNot;
        }
        return valueString.contains(fValue) ^ fNot;
    }

    @Override
    public List<String> getValidChildren() {
        return new ArrayList<>(0);
    }

    @Override
    public String toString() {
        String aspectName = fEventAspect != null ? fEventAspect.getName() : ""; //$NON-NLS-1$
        return aspectName + (fNot ? " not" : "") + " contains \"" + fValue + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    @Override
    public ITmfFilterTreeNode clone() {
        TmfFilterContainsNode clone = (TmfFilterContainsNode) super.clone();
        clone.setValue(fValue);
        return clone;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (fIgnoreCase ? 1231 : 1237);
        result = prime * result + (fNot ? 1231 : 1237);
        result = prime * result + ((fValue == null) ? 0 : fValue.hashCode());
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
        TmfFilterContainsNode other = (TmfFilterContainsNode) obj;
        if (fIgnoreCase != other.fIgnoreCase) {
            return false;
        }
        if (fNot != other.fNot) {
            return false;
        }
        if (fValue == null) {
            if (other.fValue != null) {
                return false;
            }
        } else if (!fValue.equals(other.fValue)) {
            return false;
        }
        return true;
    }
}
