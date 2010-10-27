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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * <b><u>TmfProjectContentProvider</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
 public class TmfProjectContentProvider implements ITreeContentProvider {

	 @Override
	public Object[] getChildren(Object parentElement) {
		return ((ITmfProjectTreeNode) parentElement).getChildren().toArray();
	 }

	@Override
	public Object getParent(Object element) {
		return ((ITmfProjectTreeNode) element).getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		return ((ITmfProjectTreeNode) element).hasChildren();
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof TmfProjectRoot) {
			return ((TmfProjectRoot) inputElement).getChildren().toArray();
		}
        return null;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub
	}

}
