/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.filter.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;

/**
 * Filter node for the event match operation
 *
 * @version 1.0
 * @author Patrick Tasse
 */
@SuppressWarnings("javadoc")
public class TmfFilterNode extends TmfFilterTreeNode {

    public static final String NODE_NAME = "FILTER"; //$NON-NLS-1$
    public static final String NAME_ATTR = "name"; //$NON-NLS-1$

    String fFilterName;

    /**
     * @param filterName the filter name
     */
    public TmfFilterNode(String filterName) {
        super(null);
        fFilterName = filterName;
    }

    /**
     * @param parent the parent node
     * @param filterName the filter name
     */
    public TmfFilterNode(ITmfFilterTreeNode parent, String filterName) {
        super(parent);
        fFilterName = filterName;
    }

    /**
     * @return the filer name
     */
    public String getFilterName() {
        return fFilterName;
    }

    /**
     * @param filterName the filer name
     */
    public void setFilterName(String filterName) {
        fFilterName = filterName;
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
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> getValidChildren() {
        if (getChildrenCount() == 0) {
            return super.getValidChildren();
        }
        return new ArrayList<>(0); // only one child allowed
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (getChildrenCount() > 1) {
            buf.append("( "); //$NON-NLS-1$
        }
        for (int i = 0; i < getChildrenCount(); i++) {
            ITmfFilterTreeNode node = getChildren()[i];
            buf.append(node.toString());
            if (i < (getChildrenCount() - 1)) {
                buf.append(" and "); //$NON-NLS-1$
            }
        }
        if (getChildrenCount() > 1) {
            buf.append(" )"); //$NON-NLS-1$
        }
        return buf.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fFilterName == null) ? 0 : fFilterName.hashCode());
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
        TmfFilterNode other = (TmfFilterNode) obj;
        if (fFilterName == null) {
            if (other.fFilterName != null) {
                return false;
            }
        } else if (!fFilterName.equals(other.fFilterName)) {
            return false;
        }
        return true;
    }
}
