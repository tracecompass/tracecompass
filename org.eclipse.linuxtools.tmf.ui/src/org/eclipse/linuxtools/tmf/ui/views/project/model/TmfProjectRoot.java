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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.tmf.ui.views.project.ProjectView;
import org.eclipse.linuxtools.tmf.ui.views.project.TmfProjectNature;

/**
 * <b><u>TmfProjectRoot</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfProjectRoot extends TmfProjectTreeNode {

	private final ProjectView fView;
	
	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public TmfProjectRoot(ProjectView view) {
		super(null);
		fView = view;
		refreshChildren();
	}

	@Override
	public void refresh() {
		fView.refresh();
	}

	// ------------------------------------------------------------------------
	// TmfProjectTreeNode
	// ------------------------------------------------------------------------

	public String getName() {
		return null;
	}

	@Override
	public void refreshChildren() {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject[] projects = root.getProjects();
        for (IProject project : projects) {
            if (!project.isOpen() || isTmfProject(project)) {
            	TmfProjectNode node = find(project.getName());
            	if (node == null) {
            		node = new TmfProjectNode(this, project);
            		fChildren.add(node);
            	} else {
            		node.updateState();
            	}
            }
        }
      	List<ITmfProjectTreeNode> toRemove = new ArrayList<ITmfProjectTreeNode>();
       	for (ITmfProjectTreeNode node : fChildren) {
       		if (exists(node.getName(), projects)) {
       			node.refreshChildren();
       		} else {
        		toRemove.add(node);
       		}
        }
		for (ITmfProjectTreeNode node : toRemove) {
			fChildren.remove(node);
		}
	}

	private TmfProjectNode find(String name) {
		for (ITmfProjectTreeNode node : fChildren) {
			if (node instanceof TmfProjectNode && node.getName().equals(name)) {
				return (TmfProjectNode) node;
			}
		}
		return null;
	}

	private boolean exists(String name, IProject[] projects) {
        for (IProject project : projects) {
			if (project.getName().equals(name) && (!project.isOpen() || isTmfProject(project)))
				return true;
        }
		return false;
	}

	private boolean isTmfProject(IProject project) {
	    if (project.isOpen()) {
	        IProjectNature nature;
            try {
                nature = project.getNature(TmfProjectNature.ID);
                if (nature instanceof TmfProjectNature) {
                    return true;
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
	    }
	    return false;
	}
}
