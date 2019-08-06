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
public class TmfFilterContainsNode extends TmfFilterAspectNode implements ITmfFilterWithValue {

    /** contains node name */
    public static final String NODE_NAME = "CONTAINS"; //$NON-NLS-1$
    /** not attribute name */
    public static final String NOT_ATTR = "not"; //$NON-NLS-1$
    /** value attribute name */
    public static final String VALUE_ATTR = "value"; //$NON-NLS-1$
    /** ignorecase attribute name */
    public static final String IGNORECASE_ATTR = "ignorecase"; //$NON-NLS-1$

    private String fValue;
    private transient String fValueUpperCase;
    private boolean fIgnoreCase = false;

    /**
     * @param parent the parent node
     */
    public TmfFilterContainsNode(ITmfFilterTreeNode parent) {
        super(parent);
    }

    @Override
    public String getValue() {
        return fValue;
    }

    @Override
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
            return false ^ isNot();
        }
        Object value = fEventAspect.resolve(event);
        if (value == null) {
            return false ^ isNot();
        }
        String valueString = value.toString();
        if (fIgnoreCase) {
            return valueString.toUpperCase().contains(fValueUpperCase) ^ isNot();
        }
        return valueString.contains(fValue) ^ isNot();
    }

    @Override
    public List<String> getValidChildren() {
        return new ArrayList<>(0);
    }

    @Override
    public String toString(boolean explicit) {
        return getAspectLabel(explicit) + (isNot() ? " not contains " : " contains ") + (fIgnoreCase ? "ignorecase \"" : "\"") + fValue + '\"'; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    @Override
    public ITmfFilterTreeNode clone() {
        TmfFilterContainsNode clone = (TmfFilterContainsNode) super.clone();
        clone.setValue(fValue);
        return clone;
    }
}
