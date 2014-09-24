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
 * Filter node for an event
 *
 * @version 1.0
 * @author Patrick Tasse
 */
@SuppressWarnings("javadoc")
public class TmfFilterEventTypeNode extends TmfFilterTreeNode {

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fName == null) ? 0 : fName.hashCode());
        result = prime * result + ((fType == null) ? 0 : fType.hashCode());
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
        TmfFilterEventTypeNode other = (TmfFilterEventTypeNode) obj;
        if (fName == null) {
            if (other.fName != null) {
                return false;
            }
        } else if (!fName.equals(other.fName)) {
            return false;
        }
        if (fType == null) {
            if (other.fType != null) {
                return false;
            }
        } else if (!fType.equals(other.fType)) {
            return false;
        }
        return true;
    }

    public static final String NODE_NAME = "EVENTTYPE"; //$NON-NLS-1$
    public static final String TYPE_ATTR = "type"; //$NON-NLS-1$
    public static final String NAME_ATTR = "name"; //$NON-NLS-1$

    private String fType;
    private String fName;

    /**
     * @param parent the parent node
     */
    public TmfFilterEventTypeNode(ITmfFilterTreeNode parent) {
        super(parent);
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    /**
     * @return the event type
     */
    public String getEventType() {
        return fType;
    }

    /**
     * @param type the event type
     */
    public void setEventType(String type) {
        this.fType = type;
    }

    /**
     * @return the category and trace type name
     */
    public String getName() {
        return fName;
    }

    /**
     * @param name the category and trace type name
     */
    public void setName(String name) {
        this.fName = name;
    }

    @Override
    public boolean matches(ITmfEvent event) {
        boolean match = false;
        if (fType.contains(":")) { //$NON-NLS-1$
            // special case for custom parsers
            if (fType.startsWith(event.getClass().getCanonicalName())) {
                if (fType.endsWith(event.getType().getName())) {
                    match = true;
                }
            }
        } else {
            if (event.getClass().getCanonicalName().equals(fType)) {
                match = true;
            }
        }
        if (match) {
            // There should be at most one child
            for (ITmfFilterTreeNode node : getChildren()) {
                if (! node.matches(event)) {
                    return false;
                }
            }
            return true;
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
        buf.append("EventType is " + fName); //$NON-NLS-1$
        if (getChildrenCount() > 0) {
            buf.append(" and "); //$NON-NLS-1$
        }
        if (getChildrenCount() > 1) {
            buf.append("( "); //$NON-NLS-1$
        }
        for (int i = 0; i < getChildrenCount(); i++) {
            ITmfFilterTreeNode node = getChildren()[i];
            buf.append(node.toString());
            if (i < getChildrenCount() - 1) {
                buf.append(" and "); //$NON-NLS-1$
            }
        }
        if (getChildrenCount() > 1) {
            buf.append(" )"); //$NON-NLS-1$
        }
        return buf.toString();
    }
}
