/*******************************************************************************
 * Copyright (c) 2010, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.filter.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Filter node for the '==' operation
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfFilterEqualsNode extends TmfFilterAspectNode implements ITmfFilterWithValue {

    /** equals node name */
    public static final String NODE_NAME = "EQUALS"; //$NON-NLS-1$
    /** ignorecase attribute name */
    public static final String IGNORECASE_ATTR = "ignorecase"; //$NON-NLS-1$

    private String fValue;
    private boolean fIgnoreCase = false;

    /**
     * @param parent the parent node
     */
    public TmfFilterEqualsNode(ITmfFilterTreeNode parent) {
        super(parent);
    }

    @Override
    public String getValue() {
        return fValue;
    }

    @Override
    public void setValue(String value) {
        this.fValue = value;
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
            return valueString.equalsIgnoreCase(fValue) ^ isNot();
        }
        return valueString.equals(fValue) ^ isNot();
    }

    @Override
    public List<String> getValidChildren() {
        return new ArrayList<>(0);
    }

    @Override
    public String toString(boolean explicit) {
        return getAspectLabel(explicit) + (isNot() ? " not equals " : " equals ") + (fIgnoreCase ? "ignorecase \"" : "\"") + fValue + '\"'; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    @Override
    public ITmfFilterTreeNode clone() {
        TmfFilterEqualsNode clone = (TmfFilterEqualsNode) super.clone();
        clone.setValue(fValue);
        return clone;
    }
}
