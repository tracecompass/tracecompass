/*******************************************************************************
 * Copyright (c) 2009 Ericsson
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

import java.lang.reflect.Array;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * <b><u>LTTngExperimentNode</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class LTTngExperimentNode extends LTTngProjectTreeNode {

	private final IFolder fExperiment;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public LTTngExperimentNode(IFolder resource) {
		this(null, resource);
	}
	
	public LTTngExperimentNode(ILTTngProjectTreeNode parent, IFolder folder) {
		super(parent);
		fExperiment = folder;
	}
	
	// ------------------------------------------------------------------------
	// LTTngProjectTreeNode
	// ------------------------------------------------------------------------

	public String getName() {
		return fExperiment.getName();
	}

	@Override
	public void refreshChildren() {
		try {
			IResource[] resources = fExperiment.members(0);
			for (IResource resource : resources) {
				LTTngTraceNode node = find(resource.getName());
				if (node == null) {
					node = new LTTngTraceNode(this, (IFolder) resource);
					fChildren.add(node);
				}
			}
	        for (ILTTngProjectTreeNode node : fChildren) {
	        	if (!exists(node.getName(), resources)) {
	        		fChildren.remove(node);
	        	}
	        }
		} catch (CoreException e) {
		}
	}

	private LTTngTraceNode find(String name) {
		for (ILTTngProjectTreeNode node : fChildren) {
			if (node instanceof LTTngTraceNode && node.getName().equals(name)) {
				return (LTTngTraceNode) node;
			}
		}
		return null;
	}

	private boolean exists(String name, IResource[] resources) {
        for (IResource resource : resources) {
			if (resource.getName().equals(name))
				return true;
        }
		return false;
	}

	// ------------------------------------------------------------------------
	// Modifiers
	// ------------------------------------------------------------------------

// 	No longer needed: handled by the IResourceChangeListener in the View
	public void addTrace(IFolder trace) {
//		LTTngTraceNode node = new LTTngTraceNode(this, trace);
//		fChildren.add(node);
//		refresh();
	}

	// ------------------------------------------------------------------------
	// Accessors
	// ------------------------------------------------------------------------

	/**
	 * @return
	 */
	public IFolder getFolder() {
		return fExperiment;
	}

	/**
	 * @return
	 */
	public LTTngTraceNode[] getTraces() {
		LTTngTraceNode[] result = (LTTngTraceNode[]) Array.newInstance(LTTngTraceNode.class, fChildren.size());
		return fChildren.toArray(result);
	}

	/**
	 * @return
	 */
	public LTTngProjectNode getProject() {
		return (LTTngProjectNode) getParent().getParent();
	}

}