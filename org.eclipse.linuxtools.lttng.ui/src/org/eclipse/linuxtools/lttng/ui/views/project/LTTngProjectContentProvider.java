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

import org.eclipse.core.internal.resources.WorkspaceRoot;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngExperimentEntry;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngExperimentFolder;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngProject;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngTraceFolder;

/**
 * <b><u>LTTngProjectContentProvider</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
@SuppressWarnings("restriction")
public class LTTngProjectContentProvider implements ITreeContentProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {

		// Children are limited to Traces and Experiments folders
        if (parentElement instanceof LTTngProject) {
        	LTTngProject entry = (LTTngProject) parentElement;
        	Vector<Object> result =  new Vector<Object>();

        	LTTngTraceFolder traces = entry.getTracesFolder();
        	if (entry.getTracesFolder() != null) {
        		result.add(traces);
        	}

        	LTTngExperimentFolder experiments = entry.getExperimentsFolder();
        	if (entry.getExperimentsFolder() != null) {
        		result.add(experiments);
        	}

        	return result.toArray();
        }

		// Return the list of available traces
        if (parentElement instanceof LTTngTraceFolder) {
        	LTTngTraceFolder entry = (LTTngTraceFolder) parentElement;
			return entry.getTraces();
        }

        // Return the list of experiments
        if (parentElement instanceof LTTngExperimentFolder) {
        	LTTngExperimentFolder entry = (LTTngExperimentFolder) parentElement;
			return entry.getExperiments();
        }

        // Return the list of traces in the experiment
        if (parentElement instanceof LTTngExperimentEntry) {
        	LTTngExperimentEntry entry = (LTTngExperimentEntry) parentElement;
			return entry.getTraces();
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

		// Children are limited to Traces and Experiments folders
        if (element instanceof LTTngProject) {
        	LTTngProject entry = (LTTngProject) element;
        	if (entry.getTracesFolder() != null)
        		return true;
        	if (entry.getExperimentsFolder() != null)
        		return true;
        }

		// Return the list of available traces
        if (element instanceof LTTngTraceFolder) {
        	LTTngTraceFolder entry = (LTTngTraceFolder) element;
        	return entry.getTraces().length > 0;
        }

        // Return the list of experiments
        if (element instanceof LTTngExperimentFolder) {
        	LTTngExperimentFolder entry = (LTTngExperimentFolder) element;
        	return entry.getExperiments().length > 0;
        }

        // Return the list of traces in the experiment
        if (element instanceof LTTngExperimentEntry) {
        	LTTngExperimentEntry entry = (LTTngExperimentEntry) element;
        	return entry.getTraces().length > 0;
        }

		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {

		// Return the list of LTTng projects
        if (inputElement instanceof WorkspaceRoot) {
        	IProject[] projects = ((WorkspaceRoot) inputElement).getProjects();
        	Vector<LTTngProject> tmfProjects = new Vector<LTTngProject>();
        	for (IProject project : projects) {
        		try {
        			if (project.isOpen() && project.hasNature(LTTngProjectNature.ID)) {
						LTTngProject tmfProject = new LTTngProject(project);
						tmfProjects.add(tmfProject);
					}
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        	return tmfProjects.toArray();
        }

        return null;
	}

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

}
