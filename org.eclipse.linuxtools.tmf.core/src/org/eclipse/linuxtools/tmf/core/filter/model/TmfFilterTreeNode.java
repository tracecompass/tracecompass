/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Yuriy Vashchuk (yvashchuk@gmail.com) - Initial API and implementation
 *   Patrick Tasse - Refactoring
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.filter.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;

/**
 * The base class for the Filter tree nodes
 *
 * @version 1.0
 * @author Yuriy Vashchuk
 * @author Patrick Tasse
 */
public abstract class TmfFilterTreeNode implements ITmfFilterTreeNode, Cloneable {

	private static final String[] VALID_CHILDREN = {
		TmfFilterEventTypeNode.NODE_NAME,
		TmfFilterAndNode.NODE_NAME,
		TmfFilterOrNode.NODE_NAME,
		TmfFilterContainsNode.NODE_NAME,
		TmfFilterEqualsNode.NODE_NAME,
		TmfFilterMatchesNode.NODE_NAME,
		TmfFilterCompareNode.NODE_NAME
	};

	private ITmfFilterTreeNode parent = null;
	private ArrayList<ITmfFilterTreeNode> children = new ArrayList<ITmfFilterTreeNode>();

    /**
     * @param parent the parent node
     */
    public TmfFilterTreeNode(final ITmfFilterTreeNode parent) {
    	if (parent != null) {
    		parent.addChild(this);
    	}
    }

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.filter.model.ITmfFilterTreeNode#getParent()
	 */
	@Override
	public ITmfFilterTreeNode getParent() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.filter.model.ITmfFilterTreeNode#getNodeName()
	 */
	@Override
	public abstract String getNodeName();

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.filter.model.ITmfFilterTreeNode#hasChildren()
	 */
	@Override
	public boolean hasChildren() {
		return (children.size() > 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.filter.model.ITmfFilterTreeNode#getChildrenCount()
	 */
	@Override
	public int getChildrenCount() {
		return children.size();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.filter.model.ITmfFilterTreeNode#getChildren()
	 */
	@Override
	public ITmfFilterTreeNode[] getChildren() {
		return children.toArray(new ITmfFilterTreeNode[0]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.filter.model.ITmfFilterTreeNode#getChild(int)
	 */
	@Override
	public ITmfFilterTreeNode getChild(final int index) throws IndexOutOfBoundsException {
		return children.get(index);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.filter.model.ITmfFilterTreeNode#remove()
	 */
	@Override
	public ITmfFilterTreeNode remove() {
		if (getParent() != null) {
			getParent().removeChild(this);
		}
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.filter.model.ITmfFilterTreeNode#removeChild(org.eclipse.linuxtools.tmf.filter.model.ITmfFilterTreeNode)
	 */
	@Override
	public ITmfFilterTreeNode removeChild(ITmfFilterTreeNode node) {
		children.remove(node);
		node.setParent(null);
		return node;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.filter.model.ITmfFilterTreeNode#addChild(org.eclipse.linuxtools.tmf.filter.model.ITmfFilterTreeNode)
	 */
	@Override
	public int addChild(final ITmfFilterTreeNode node) {
		node.setParent(this);
		if(children.add(node)) {
			return (children.size() - 1);
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.filter.model.ITmfFilterTreeNode#replaceChild(int, org.eclipse.linuxtools.tmf.filter.model.ITmfFilterTreeNode)
	 */
	@Override
	public ITmfFilterTreeNode replaceChild(final int index, final ITmfFilterTreeNode node) throws IndexOutOfBoundsException {
		node.setParent(this);
		return children.set(index, node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.filter.model.ITmfFilterTreeNode#setParent(org.eclipse.linuxtools.tmf.filter.model.ITmfFilterTreeNode)
	 */
	@Override
	public void setParent(final ITmfFilterTreeNode parent) {
		this.parent = parent;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.filter.model.ITmfFilterTreeNode#matches(org.eclipse.linuxtools.tmf.event.TmfEvent)
	 */
	@Override
	public abstract boolean matches(ITmfEvent event);

    /**
     * @param event the event
     * @param field the field id
     * @return the field value
     */
    protected Object getFieldValue(ITmfEvent event, String field) {
        Object value = null;
        if (ITmfEvent.EVENT_FIELD_CONTENT.equals(field)) {
            ITmfEventField content = event.getContent();
            value = (content.getValue() != null) ? content.getValue().toString() : content.toString();
        }
        else if (ITmfEvent.EVENT_FIELD_TYPE.equals(field)) {
            value = event.getType().getName();
        }
        else if (ITmfEvent.EVENT_FIELD_TIMESTAMP.equals(field)) {
            value = event.getTimestamp().toString();
        }
        else if (ITmfEvent.EVENT_FIELD_SOURCE.equals(field)) {
            value = event.getSource();
        }
        else if (ITmfEvent.EVENT_FIELD_REFERENCE.equals(field)) {
            value = event.getReference();
        }
        else {
            ITmfEventField eventField = event.getContent().getField(field);
            if (eventField != null) {
                value = eventField.getValue();
            }
        }
        return value;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.filter.model.ITmfFilterTreeNode#getValidChildren()
	 *
	 * By default, all node types are valid children. Override if different.
	 */
	@Override
	public List<String> getValidChildren() {
		return Arrays.asList(VALID_CHILDREN);
	}

	@Override
	public ITmfFilterTreeNode clone() {
		try {
			TmfFilterTreeNode clone = (TmfFilterTreeNode) super.clone();
			clone.parent = null;
			clone.children = new ArrayList<ITmfFilterTreeNode>(children.size());
			for (ITmfFilterTreeNode child : getChildren()) {
				clone.addChild(child.clone());
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
