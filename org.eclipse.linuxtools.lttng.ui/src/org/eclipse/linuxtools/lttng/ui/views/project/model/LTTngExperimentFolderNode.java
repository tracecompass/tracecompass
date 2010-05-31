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

import java.lang.reflect.Array;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * <b><u>LTTngExperimentFolderNode</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class LTTngExperimentFolderNode extends LTTngProjectTreeNode {

	private final IFolder fExperimentFolder;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public LTTngExperimentFolderNode(IFolder folder) {
		this(null, folder);
	}

	public LTTngExperimentFolderNode(ILTTngProjectTreeNode parent, IFolder folder) {
		super(parent);
		fExperimentFolder = folder;
	}

	// ------------------------------------------------------------------------
	// LTTngProjectTreeNode
	// ------------------------------------------------------------------------

	public String getName() {
		return fExperimentFolder.getName();
	}

	@Override
	public void refreshChildren() {
		try {
			IResource[] resources = fExperimentFolder.members();
			for (IResource resource : resources) {
				if (resource instanceof IFolder) {
					LTTngExperimentNode node = find(resource.getName());
					if (node == null) {
						node = new LTTngExperimentNode(this, (IFolder) resource);
						fChildren.add(node);
					}
				}
			}
	        for (ILTTngProjectTreeNode node : fChildren) {
	        	if (exists(node.getName(), resources)) {
	        		node.refreshChildren();
	        	}
	        	else {
	        		fChildren.remove(node);
	        	}
	        }
		} catch (CoreException e) {
		}
	}

	private LTTngExperimentNode find(String name) {
		for (ILTTngProjectTreeNode node : fChildren) {
			if (node instanceof LTTngExperimentNode && node.getName().equals(name)) {
				return (LTTngExperimentNode) node;
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
	// Accessors
	// ------------------------------------------------------------------------

	/**
	 * @return
	 */
	public LTTngProjectNode getProject() {
		return (LTTngProjectNode) getParent();
	}

	/**
	 * @return
	 */
	public IFolder getFolder() {
		return fExperimentFolder;
	}

	/**
	 * @return
	 */
	public LTTngExperimentNode[] getExperiments() {
		LTTngExperimentNode[] result = (LTTngExperimentNode[]) Array.newInstance(LTTngExperimentNode.class, fChildren.size());
		return fChildren.toArray(result);
	}

	// ------------------------------------------------------------------------
	// Modifiers
	// ------------------------------------------------------------------------

// 	No longer needed: handled by the IResourceChangeListener in the View
	public void addExperiment(IFolder experiment) {
//		LTTngExperimentNode node = new LTTngExperimentNode(this, experiment);
//		fChildren.add(node);
//		refresh();
	}

}
