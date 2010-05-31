/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views.project.model;

import java.util.ArrayList;
import java.util.List;

/**
 * <b><u>LTTngProjectTreeNode</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public abstract class LTTngProjectTreeNode implements ILTTngProjectTreeNode {

	protected ILTTngProjectTreeNode fParent = null;
	protected List<ILTTngProjectTreeNode> fChildren = null;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public LTTngProjectTreeNode(ILTTngProjectTreeNode parent) {
		fParent = parent;
		fChildren = new ArrayList<ILTTngProjectTreeNode>();
	}

	@Override
	public String toString() {
		return getName();
	}
	
	// ------------------------------------------------------------------------
	// ILTTngProjectTreeNode
	// ------------------------------------------------------------------------

	public ILTTngProjectTreeNode getParent() {
		return fParent;
	}

	public boolean hasChildren() {
		return fChildren.size() > 0;
	}

	public List<ILTTngProjectTreeNode> getChildren() {
		return fChildren;
	}

	public abstract void refreshChildren();

	public void refresh() {
		fParent.refresh();
	}

	public void removeChild(ILTTngProjectTreeNode child) {
        for (ILTTngProjectTreeNode node : fChildren) {
        	if (node == child) {
        		node.removeChildren();
        		// We can do it since we are returning right away
        		fChildren.remove(node);
        		return;
        	}
        }
	}

	public void removeChildren() {
        for (ILTTngProjectTreeNode node : fChildren) {
        		node.removeChildren();
        }
		fChildren.clear();
	}

}
