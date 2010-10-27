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
import java.util.Iterator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * <b><u>TmfTraceFolderNode</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfTraceFolderNode extends TmfProjectTreeNode {

	private final IFolder fTraceFolder;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public TmfTraceFolderNode(IFolder folder) {
		this(null, folder);
	}

	public TmfTraceFolderNode(ITmfProjectTreeNode parent, IFolder folder) {
		super(parent);
		fTraceFolder = folder;
	}

	// ------------------------------------------------------------------------
	// TmfProjectTreeNode
	// ------------------------------------------------------------------------

	@Override
	public String getName() {
		return fTraceFolder.getName();
	}

	@Override
	public void refreshChildren() {
		try {
			IResource[] resources = fTraceFolder.members();
			for (IResource resource : resources) {
				TmfTraceNode node = find(resource.getName());
				if (node == null) {
					node = new TmfTraceNode(this, resource);
					fChildren.add(node);
				}
			}
			Iterator<ITmfProjectTreeNode> iterator = fChildren.iterator();
			while (iterator.hasNext()) {
				ITmfProjectTreeNode node = iterator.next();
	        	if (!exists(node.getName(), resources)) {
	        		iterator.remove();
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
		return fTraceFolder;
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
	public TmfTraceNode getTraceForLocation(IPath location) {
	    for (ITmfProjectTreeNode node : fChildren) {
	        TmfTraceNode trace = (TmfTraceNode) node;
	        if (trace.getResource().getLocation().equals(location)) {
	            return trace;
	        }
	    }
	    return null;
	}
	
	// ------------------------------------------------------------------------
	// Modifiers
	// ------------------------------------------------------------------------

// 	No longer needed: handled by the IResourceChangeListener in the View
	public void addTrace(IFolder trace) {
//		TmfTraceNode node = new TmfTraceNode(this, trace);
//		fChildren.add(node);
//		refresh();
	}

}
