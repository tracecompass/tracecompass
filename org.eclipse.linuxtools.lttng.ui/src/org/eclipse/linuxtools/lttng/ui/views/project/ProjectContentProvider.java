/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views.project;

import java.util.Vector;

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.internal.resources.Folder;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.internal.resources.WorkspaceRoot;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * <b><u>ProjectContentProvider</u></b>
 * <p>
 *
 * TODO: Implement me. Please.
 */
@SuppressWarnings("restriction")
public class ProjectContentProvider implements ITreeContentProvider {

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parentElement) {
    	if (parentElement instanceof Project) {
    		Project parent = (Project) parentElement;
    		IResource[] content = new IResource[0];
    		try {
    		    IResource[] members = parent.members();
    			Vector<IResource> children = new Vector<IResource>();
    			for (IResource resource : members) {
    			    if (resource instanceof File) {
    			        File file = (File) resource;
    			        if (!file.getName().startsWith(".")) {
                            children.add(resource);
    			        }
    			    }
    			    else {
    			        children.add(resource);
    			    }
    			}
    			content = children.toArray(content);
    		} catch (CoreException e) {
    		}
    		return content;
    	}
    	if (parentElement instanceof Folder) {
    		Folder parent = (Folder) parentElement;
    		IResource[] content = null;
    		try {
    			content = parent.members();
    		} catch (CoreException e) {
    		}
    		return content;
    	}
		return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
    	if (element instanceof Project) {
    		Project project = (Project) element;
    		int nbChildren = 0;
    		try {
				nbChildren = project.members().length;
			} catch (CoreException e) {
			}
			return nbChildren > 0;
    	}
    	if (element instanceof Folder) {
    		Folder folder = (Folder) element;
    		int nbChildren = folder.countResources(1, false);
			return nbChildren > 0;
    	}
    	return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof WorkspaceRoot) {
        	WorkspaceRoot root = (WorkspaceRoot) inputElement;
        	return root.getProjects();
        }
        return null;
    }

}
