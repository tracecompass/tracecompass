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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.tmf.ui.views.project.TmfProjectNature;

/**
 * <b><u>TmfProjectNode</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfProjectNode extends TmfProjectTreeNode {

	public static final String TRACE_FOLDER_NAME = "Traces";
	public static final String EXPER_FOLDER_NAME = "Experiments";

	private final IProject fProject;
	private boolean fIsTmfProject;
	private boolean fIsOpen;
	private TmfTraceFolderNode fTracesFolder;
	private TmfExperimentFolderNode fExperimentsFolder;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public TmfProjectNode(IProject project) {
		this(null, project);
	}

	public TmfProjectNode(ITmfProjectTreeNode parent, IProject project) {
		super(parent);
		fProject = project;
		updateState();
	}

	// ------------------------------------------------------------------------
	// TmfProjectTreeNode
	// ------------------------------------------------------------------------

	@Override
	public String getName() {
		return fProject.getName();
	}

	@Override
	public void refreshChildren() {

		if (!(fIsOpen && fIsTmfProject))
			return;

		try {
			IResource[] resources = fProject.members();
			for (IResource resource : resources) {
				if (resource.getType() == IResource.FOLDER) {
					String name = resource.getName();
					if (name.equals(TRACE_FOLDER_NAME) && !isIncluded(true, name, fChildren)) {
						fTracesFolder = new TmfTraceFolderNode(this, (IFolder) resource);
						fChildren.add(fTracesFolder);
					} else
					if (name.equals(EXPER_FOLDER_NAME) && !isIncluded(false, name, fChildren)) {
						fExperimentsFolder = new TmfExperimentFolderNode(this, (IFolder) resource);
						fChildren.add(fExperimentsFolder);
					}
				}
			}
        	List<ITmfProjectTreeNode> toRemove = new ArrayList<ITmfProjectTreeNode>();
	        for (ITmfProjectTreeNode node : fChildren) {
	        	if (exists(node.getName(), resources)) {
	        		node.refreshChildren();
	        	}
	        	else {
	        		toRemove.add(node);
	        	}
	        }
    		for (ITmfProjectTreeNode node : toRemove) {
    			fChildren.remove(node);
    		}
	        
		} catch (CoreException e) {
		}
	}

	private boolean isIncluded(boolean isTraces, String name, List<ITmfProjectTreeNode> list) {
		boolean found = false;
		for (ITmfProjectTreeNode node : list) {
			if (node instanceof TmfTraceFolderNode && isTraces) {
				found |= node.getName().equals(name);
			} else
			if (node instanceof TmfExperimentFolderNode && !isTraces) {
				found |= node.getName().equals(name);
			}
		}
		return found;
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

	public void openProject() {
		try {
			fProject.open(null);
			updateState();
			refreshChildren();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public void closeProject() {
		try {
			fProject.close(null);
			updateState();
			removeChildren();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private boolean isTmfProject(IProject project) {
		boolean result = false;
		if (project != null && project.isAccessible()) {
			try {
				result = project.hasNature(TmfProjectNature.ID);
			} catch (CoreException e) {
			}
		}
		return result;
	}

	public void updateState() {
		fIsOpen = (fProject != null) ? fProject.isAccessible() : false;
		if (!fIsOpen) {
		    removeChildren();
		}
		fIsTmfProject = isTmfProject(fProject);
	}

	// ------------------------------------------------------------------------
	// Accessors
	// ------------------------------------------------------------------------

	/**
	 * @return
	 */
	public boolean isTmfProject() {
		return fIsTmfProject;
	}

	/**
	 * @return
	 */
	public boolean isOpen() {
		return fIsOpen;
	}

	/**
	 * @return
	 */
	public IProject getProject() {
		return fProject;
	}

	/**
	 * @return
	 */
	public TmfTraceFolderNode getTracesFolder() {
		return fTracesFolder;
	}

	/**
	 * @return
	 */
	public TmfExperimentFolderNode getExperimentsFolder() {
		return fExperimentsFolder;
	}

}
