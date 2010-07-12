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

import org.eclipse.core.resources.IResource;

/**
 * <b><u>TmfTraceNode</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfTraceNode extends TmfProjectTreeNode {
	
	private final IResource fTrace;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public TmfTraceNode(IResource trace) {
		this(null, trace);
	}

	public TmfTraceNode(ITmfProjectTreeNode parent, IResource trace) {
		super(parent);
		fTrace = trace;
	}

	// ------------------------------------------------------------------------
	// TmfProjectTreeNode
	// ------------------------------------------------------------------------

	public String getName() {
		return fTrace.getName();
	}

	@Override
	public void refreshChildren() {
	}

	// ------------------------------------------------------------------------
	// Accessors
	// ------------------------------------------------------------------------

	/**
	 * @return
	 */
	public TmfProjectNode getProject() {
        ITmfProjectTreeNode node = this;
	    while (node != null) {
	        node = node.getParent();
	        if (node instanceof TmfProjectNode) {
	            return (TmfProjectNode) node;
	        }
	    }
		return null;
	}

	/**
	 * @return
	 */
	public IResource getResource() {
		return fTrace;
	}

}
