/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.core.model;


public class LTTngTreeNode extends
		LTTngTreeNodeGeneric<LTTngTreeNode> {

	// ========================================================================
	// Data
	// ========================================================================


	// ========================================================================
	// Constructors
	// ========================================================================
	/**
	 * @param id
	 * @param parent
	 * @param name
	 */
	public LTTngTreeNode(Long id, LTTngTreeNode parent, String name) {
		super(id, parent, name, null);
	}

	/**
	 * @param id
	 * @param parent
	 * @param name
	 * @param value
	 */
	public LTTngTreeNode(Long id, LTTngTreeNode parent, String name,
			Object value) {
		super(id, parent, name, value);
	}

	/**
	 * When parent is not know just yet
	 * 
	 * @param id
	 * @param name
	 * @param value
	 */
	public LTTngTreeNode(Long id, String name, Object value) {
		this(id, null, name, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.model.LTTngTreeNodeGeneric#getChildren()
	 */
	@Override
	public LTTngTreeNode[] getChildren() {
		return childrenToArray(fchildren.values(), this.getClass());
	}

}
