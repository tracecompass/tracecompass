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

package org.eclipse.linuxtools.lttng.ui.views.project.model;

import org.eclipse.core.internal.resources.Folder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * <b><u>LTTngProject</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
@SuppressWarnings("restriction")
public class LTTngProject {

	private final IProject fProject;
	private LTTngTraceFolder fTracesFolder;
	private LTTngExperimentFolder fExperimentsFolder;

	/**
	 * @param project
	 */
	public LTTngProject(IProject project) {

		fProject = project;
		fTracesFolder = null;
		fExperimentsFolder = null;
		try {
			IResource[] resources = project.members();
			for (IResource resource : resources) {
				if (resource.getType() == IResource.FOLDER) {
					String name = resource.getName();
					if (name.equals("Traces")) {
						fTracesFolder = new LTTngTraceFolder(this, (Folder) resource);
					}
					if (name.equals("Experiments")) {
						fExperimentsFolder = new LTTngExperimentFolder(this, (Folder) resource);
					}
				}
			}
		} catch (CoreException e) {
		}
	}

	/**
	 * @return
	 */
	public IProject getProject() {
		return fProject;
	}

	/**
	 * @return
	 */
	public String getName() {
		return fProject.getName();
	}

	/**
	 * @return
	 */
	public LTTngTraceFolder getTracesFolder() {
		return fTracesFolder;
	}

	/**
	 * @return
	 */
	public LTTngExperimentFolder getExperimentsFolder() {
		return fExperimentsFolder;
	}

}
