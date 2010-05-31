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

import java.util.List;

/**
 * <b><u>ILTTngProjectTreeNode</u></b>
 * <p>
 * TODO: Implement me. Please.
 * TODO: Make ILTTngProjectTreeNode extends IAdaptable
 */
public interface ILTTngProjectTreeNode {

	public String getName();

	public ILTTngProjectTreeNode getParent();

	public boolean hasChildren();
	
	public List<ILTTngProjectTreeNode> getChildren();
	
	public void removeChild(ILTTngProjectTreeNode child);

	public void removeChildren();

	public void refreshChildren();

	public void refresh();

}
