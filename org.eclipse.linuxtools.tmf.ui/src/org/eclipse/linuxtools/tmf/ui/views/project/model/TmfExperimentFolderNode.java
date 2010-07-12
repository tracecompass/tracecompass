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

import java.lang.reflect.Array;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * <b><u>TmfExperimentFolderNode</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfExperimentFolderNode extends TmfProjectTreeNode {

	private final IFolder fExperimentFolder;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public TmfExperimentFolderNode(IFolder folder) {
		this(null, folder);
	}

	public TmfExperimentFolderNode(ITmfProjectTreeNode parent, IFolder folder) {
		super(parent);
		fExperimentFolder = folder;
	}

	// ------------------------------------------------------------------------
	// TmfProjectTreeNode
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
					TmfExperimentNode node = find(resource.getName());
					if (node == null) {
						node = new TmfExperimentNode(this, (IFolder) resource);
						fChildren.add(node);
					}
				}
			}
	        for (ITmfProjectTreeNode node : fChildren) {
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

	private TmfExperimentNode find(String name) {
		for (ITmfProjectTreeNode node : fChildren) {
			if (node instanceof TmfExperimentNode && node.getName().equals(name)) {
				return (TmfExperimentNode) node;
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
	public TmfProjectNode getProject() {
		return (TmfProjectNode) getParent();
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
	public TmfExperimentNode[] getExperiments() {
		TmfExperimentNode[] result = (TmfExperimentNode[]) Array.newInstance(TmfExperimentNode.class, fChildren.size());
		return fChildren.toArray(result);
	}

	// ------------------------------------------------------------------------
	// Modifiers
	// ------------------------------------------------------------------------

// 	No longer needed: handled by the IResourceChangeListener in the View
	public void addExperiment(IFolder experiment) {
//		TmfExperimentNode node = new TmfExperimentNode(this, experiment);
//		fChildren.add(node);
//		refresh();
	}

}
