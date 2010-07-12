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

package org.eclipse.linuxtools.tmf.ui.views.project.model;

import java.util.ArrayList;
import java.util.List;

/**
 * <b><u>TmfProjectTreeNode</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public abstract class TmfProjectTreeNode implements ITmfProjectTreeNode {

	protected ITmfProjectTreeNode fParent = null;
	protected List<ITmfProjectTreeNode> fChildren = null;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public TmfProjectTreeNode(ITmfProjectTreeNode parent) {
		fParent = parent;
		fChildren = new ArrayList<ITmfProjectTreeNode>();
	}

	@Override
	public String toString() {
		return getName();
	}
	
	// ------------------------------------------------------------------------
	// ITmfProjectTreeNode
	// ------------------------------------------------------------------------

	public ITmfProjectTreeNode getParent() {
		return fParent;
	}

	public boolean hasChildren() {
		return fChildren.size() > 0;
	}

	public List<ITmfProjectTreeNode> getChildren() {
		return fChildren;
	}

	public abstract void refreshChildren();

	public void refresh() {
		fParent.refresh();
	}

	public void removeChild(ITmfProjectTreeNode child) {
        for (ITmfProjectTreeNode node : fChildren) {
        	if (node == child) {
        		node.removeChildren();
        		// We can do it since we are returning right away
        		fChildren.remove(node);
        		return;
        	}
        }
	}

	public void removeChildren() {
        for (ITmfProjectTreeNode node : fChildren) {
        		node.removeChildren();
        }
		fChildren.clear();
	}

}
