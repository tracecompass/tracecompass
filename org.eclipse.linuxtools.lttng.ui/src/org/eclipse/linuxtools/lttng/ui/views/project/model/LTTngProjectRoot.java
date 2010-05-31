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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.linuxtools.lttng.ui.views.project.ProjectView;

/**
 * <b><u>LTTngProjectRoot</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class LTTngProjectRoot extends LTTngProjectTreeNode {

	private final ProjectView fView;
	
	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public LTTngProjectRoot(ProjectView view) {
		super(null);
		fView = view;
		refreshChildren();
	}

	@Override
	public void refresh() {
		fView.refresh();
	}

	// ------------------------------------------------------------------------
	// LTTngProjectTreeNode
	// ------------------------------------------------------------------------

	public String getName() {
		return null;
	}

	@Override
	public void refreshChildren() {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject[] projects = root.getProjects();
        for (IProject project : projects) {
			LTTngProjectNode node = find(project.getName());
			if (node == null) {
				node = new LTTngProjectNode(this, project);
				fChildren.add(node);
			} else {
				node.updateState();
			}
        }
      	List<ILTTngProjectTreeNode> toRemove = new ArrayList<ILTTngProjectTreeNode>();
       	for (ILTTngProjectTreeNode node : fChildren) {
       		if (exists(node.getName(), projects)) {
       			node.refreshChildren();
       		}
       		else {
        		toRemove.add(node);
       		}
        }
		for (ILTTngProjectTreeNode node : toRemove) {
			fChildren.remove(node);
		}
	}

	private LTTngProjectNode find(String name) {
		for (ILTTngProjectTreeNode node : fChildren) {
			if (node instanceof LTTngProjectNode && node.getName().equals(name)) {
				return (LTTngProjectNode) node;
			}
		}
		return null;
	}

	private boolean exists(String name, IProject[] projects) {
        for (IProject project : projects) {
			if (project.getName().equals(name))
				return true;
        }
		return false;
	}

}
