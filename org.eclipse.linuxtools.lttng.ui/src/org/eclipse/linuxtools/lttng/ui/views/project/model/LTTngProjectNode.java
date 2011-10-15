/*******************************************************************************
 * Copyright (c) 2009, 2010, 2011 Ericsson, MontaVista Software
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Yufen Kuo (ykuo@mvista.com) - bug 354541: implement IAdaptable Project->Properties action is enabled when project is selected.
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views.project.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.lttng.LTTngProjectNature;

/**
 * <b><u>LTTngProjectNode</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class LTTngProjectNode extends LTTngProjectTreeNode implements IAdaptable {

	public static final String TRACE_FOLDER_NAME = "Traces"; //$NON-NLS-1$
	public static final String EXPER_FOLDER_NAME = "Experiments"; //$NON-NLS-1$

	private final IProject fProject;
	private boolean fIsLTTngProject;
	private boolean fIsOpen;
	private LTTngTraceFolderNode fTracesFolder;
	private LTTngExperimentFolderNode fExperimentsFolder;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public LTTngProjectNode(IProject project) {
		this(null, project);
	}

	public LTTngProjectNode(ILTTngProjectTreeNode parent, IProject project) {
		super(parent);
		fProject = project;
		updateState();
	}

	// ------------------------------------------------------------------------
	// LTTngProjectTreeNode
	// ------------------------------------------------------------------------

	@Override
	public String getName() {
		return fProject.getName();
	}

	@Override
	public void refreshChildren() {

		if (!(fIsOpen && fIsLTTngProject))
			return;

		try {
			IResource[] resources = fProject.members();
			for (IResource resource : resources) {
				if (resource.getType() == IResource.FOLDER) {
					String name = resource.getName();
					if (name.equals(TRACE_FOLDER_NAME) && !isIncluded(true, name, fChildren)) {
						fTracesFolder = new LTTngTraceFolderNode(this, (IFolder) resource);
						fChildren.add(fTracesFolder);
					} else
					if (name.equals(EXPER_FOLDER_NAME) && !isIncluded(false, name, fChildren)) {
						fExperimentsFolder = new LTTngExperimentFolderNode(this, (IFolder) resource);
						fChildren.add(fExperimentsFolder);
					}
				}
			}
        	List<ILTTngProjectTreeNode> toRemove = new ArrayList<ILTTngProjectTreeNode>();
	        for (ILTTngProjectTreeNode node : fChildren) {
	        	if (exists(node.getName(), resources)) {
	        		node.refreshChildren();
	        	}
	        	else {
	        		toRemove.add(node);
	        	}
	        }
    		for (ILTTngProjectTreeNode node : toRemove) {
    			fChildren.remove(node);
    		}
	        
		} catch (CoreException e) {
		}
	}

	private boolean isIncluded(boolean isTraces, String name, List<ILTTngProjectTreeNode> list) {
		boolean found = false;
		for (ILTTngProjectTreeNode node : list) {
			if (node instanceof LTTngTraceFolderNode && isTraces) {
				found |= node.getName().equals(name);
			} else
			if (node instanceof LTTngExperimentFolderNode && !isTraces) {
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

	private boolean isLTTngProject(IProject project) {
		boolean result = false;
		if (project != null && project.isAccessible()) {
			try {
				result = project.hasNature(LTTngProjectNature.ID);
			} catch (CoreException e) {
			}
		}
		return result;
	}

	public void updateState() {
		fIsOpen = (fProject != null) ? fProject.isAccessible() : false;
		fIsLTTngProject = isLTTngProject(fProject);
	}

	// ------------------------------------------------------------------------
	// Accessors
	// ------------------------------------------------------------------------

	/**
	 * @return
	 */
	public boolean isLTTngProject() {
		return fIsLTTngProject;
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
	public LTTngTraceFolderNode getTracesFolder() {
		return fTracesFolder;
	}

	/**
	 * @return
	 */
	public LTTngExperimentFolderNode getExperimentsFolder() {
		return fExperimentsFolder;
	}

    /**
     * Returns the adapter
     */
    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class adapter) {
        if (adapter == IResource.class) {
            return getProject();
        }
        // Defer to the platform
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

}
