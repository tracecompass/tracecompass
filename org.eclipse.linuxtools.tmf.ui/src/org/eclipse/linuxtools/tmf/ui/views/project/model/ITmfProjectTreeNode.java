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

import java.util.List;

/**
 * <b><u>ITmfProjectTreeNode</u></b>
 * <p>
 * TODO: Implement me. Please.
 * TODO: Make ITmfProjectTreeNode extends IAdaptable
 */
public interface ITmfProjectTreeNode {

	public String getName();

	public ITmfProjectTreeNode getParent();

	public boolean hasChildren();
	
	public List<ITmfProjectTreeNode> getChildren();
	
	public void removeChild(ITmfProjectTreeNode child);

	public void removeChildren();

	public void refreshChildren();

	public void refresh();

}
