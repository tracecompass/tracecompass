/*******************************************************************************
 * Copyright (c) 2010, 2020 Ericsson
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

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Filter node for the 'and' operation
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfFilterAndNode extends TmfFilterTreeNode implements ITmfFilterWithNot {

    /** and node name */
    public static final String NODE_NAME = "AND"; //$NON-NLS-1$

    private boolean fNot = false;

    /**
     * @param parent the parent node
     */
    public TmfFilterAndNode(ITmfFilterTreeNode parent) {
        super(parent);
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public boolean matches(ITmfEvent event) {
        // Empty children
        if (getChildren().length == 0) {
            return isNot();
        }
        for (ITmfFilterTreeNode node : getChildren()) {
            if (!node.matches(event)) {
                return isNot();
            }
        }
        // All children match
        return !isNot();
    }

    @Override
    public String toString(boolean explicit) {
        StringBuilder buf = new StringBuilder();
        if (isNot()) {
            buf.append("not "); //$NON-NLS-1$
        }
        if (getParent() != null && !(getParent() instanceof TmfFilterRootNode) && !(getParent() instanceof TmfFilterNode)) {
            buf.append("( "); //$NON-NLS-1$
        }
        for (int i = 0; i < getChildrenCount(); i++) {
            ITmfFilterTreeNode node = getChildren()[i];
            buf.append(node.toString(explicit));
            if (i < getChildrenCount() - 1) {
                buf.append(" and "); //$NON-NLS-1$
            }
        }
        if (getParent() != null && !(getParent() instanceof TmfFilterRootNode) && !(getParent() instanceof TmfFilterNode)) {
            buf.append(" )"); //$NON-NLS-1$
        }
        return buf.toString();
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
