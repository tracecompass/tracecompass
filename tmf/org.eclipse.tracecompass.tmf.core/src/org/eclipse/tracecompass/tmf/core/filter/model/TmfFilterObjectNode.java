/*******************************************************************************
 * Copyright (c) 2016 Ericsson
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
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;

/**
 * Filter node that contains its own filter object
 *
 * @version 1.0
 * @author Patrick Tasse
 * @since 2.0
 */
public class TmfFilterObjectNode extends TmfFilterTreeNode implements ITmfFilterWithNot {

    /** filter node name */
    public static final String NODE_NAME = "FILTEROBJECT"; //$NON-NLS-1$
    /** name attribute name */
    public static final String NAME_ATTR = "name"; //$NON-NLS-1$

    private final ITmfFilter fFilter;
    private boolean fNot;

    /**
     * @param filter the filter object
     */
    public TmfFilterObjectNode(ITmfFilter filter) {
        super(null);
        fFilter = filter;
    }

    /**
     * @param parent the parent node
     * @param filter the filter object
     */
    public TmfFilterObjectNode(ITmfFilterTreeNode parent, ITmfFilter filter) {
        super(parent);
        fFilter = filter;
    }

    /**
     * @return the filter object
     */
    public ITmfFilter getFilter() {
        return fFilter;
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public boolean matches(ITmfEvent event) {
        // There should be at most one child
        for (ITmfFilterTreeNode node : getChildren()) {
            if (node.matches(event)) {
                return !isNot();
            }
        }
        return isNot();
    }

    @Override
    public List<String> getValidChildren() {
        return new ArrayList<>(0);
    }

    @Override
    public String toString(boolean explicit) {
        if (fFilter instanceof ITmfFilterTreeNode) {
            return ((ITmfFilterTreeNode) fFilter).toString(explicit);
        }
        if (isNot()) {
            return "not " + fFilter.toString(); //$NON-NLS-1$
        }
        return fFilter.toString();
    }

    @Override
    public boolean isNot() {
        return fNot;
    }

    @Override
    public void setNot(boolean not) {
        fNot = not;
    }
}
