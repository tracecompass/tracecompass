/*******************************************************************************
 * Copyright (c) 2010, 2012 Ericsson
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
	 * @return TBD
	 */
	public String getName() {
		return fName;
	}

	/**
	 * @param name TBD
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
