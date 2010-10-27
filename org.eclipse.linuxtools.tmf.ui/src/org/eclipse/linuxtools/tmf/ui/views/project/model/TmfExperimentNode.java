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

package org.eclipse.linuxtools.tmf.ui.views.project.model;

import java.lang.reflect.Array;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * <b><u>TmfExperimentNode</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfExperimentNode extends TmfProjectTreeNode {

	private final IFolder fExperiment;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public TmfExperimentNode(IFolder resource) {
		this(null, resource);
	}
	
	public TmfExperimentNode(ITmfProjectTreeNode parent, IFolder folder) {
		super(parent);
		fExperiment = folder;
	}
	
	// ------------------------------------------------------------------------
	// TmfProjectTreeNode
	// ------------------------------------------------------------------------

	@Override
	public String getName() {
		return fExperiment.getName();
	}

	@Override
	public void refreshChildren() {
		try {
			IResource[] resources = fExperiment.members(0);
			for (IResource resource : resources) {
				TmfTraceNode node = find(resource.getName());
				if (node == null) {
					node = new TmfTraceNode(this, resource);
					fChildren.add(node);
				}
			}
	        for (ITmfProjectTreeNode node : fChildren) {
	        	if (!exists(node.getName(), resources)) {
	        		fChildren.remove(node);
	        	}
	        }
		} catch (CoreException e) {
		}
	}

	private TmfTraceNode find(String name) {
		for (ITmfProjectTreeNode node : fChildren) {
			if (node instanceof TmfTraceNode && node.getName().equals(name)) {
				return (TmfTraceNode) node;
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
	public void addTrace(IResource trace) {
//		TmfTraceNode node = new TmfTraceNode(this, trace);
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
	public TmfTraceNode[] getTraces() {
		TmfTraceNode[] result = (TmfTraceNode[]) Array.newInstance(TmfTraceNode.class, fChildren.size());
		return fChildren.toArray(result);
	}

	/**
	 * @return
	 */
	public TmfProjectNode getProject() {
		return (TmfProjectNode) getParent().getParent();
	}

}