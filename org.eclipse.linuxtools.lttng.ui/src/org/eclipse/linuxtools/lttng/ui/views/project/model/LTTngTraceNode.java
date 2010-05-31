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

import org.eclipse.core.resources.IFolder;

/**
 * <b><u>LTTngTraceNode</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class LTTngTraceNode extends LTTngProjectTreeNode {
	
	private final IFolder fTrace;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public LTTngTraceNode(IFolder folder) {
		this(null, folder);
	}

	public LTTngTraceNode(ILTTngProjectTreeNode parent, IFolder trace) {
		super(parent);
		fTrace = trace;
	}

	// ------------------------------------------------------------------------
	// LTTngProjectTreeNode
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
	public LTTngProjectNode getProject() {
		return (LTTngProjectNode) getParent().getParent();
	}

	/**
	 * @return
	 */
	public IFolder getFolder() {
		return fTrace;
	}

}
