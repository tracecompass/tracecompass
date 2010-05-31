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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * <b><u>LTTngProjectContentProvider</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
 public class LTTngProjectContentProvider implements ITreeContentProvider {

	public Object[] getChildren(Object parentElement) {
		return ((ILTTngProjectTreeNode) parentElement).getChildren().toArray();
	}

	public Object getParent(Object element) {
		return ((ILTTngProjectTreeNode) element).getParent();
	}

	public boolean hasChildren(Object element) {
		return ((ILTTngProjectTreeNode) element).hasChildren();
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof LTTngProjectRoot) {
			return ((LTTngProjectRoot) inputElement).getChildren().toArray();
		}
        return null;
	}

	public void dispose() {
		// TODO Auto-generated method stub
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub
	}

}
