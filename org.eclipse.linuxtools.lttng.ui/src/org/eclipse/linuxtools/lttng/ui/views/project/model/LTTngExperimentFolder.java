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
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * <b><u>LTTngExperimentFolder</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
@SuppressWarnings("restriction")
public class LTTngExperimentFolder {

	private final Folder fExperimentFolder;
	private final LTTngProject fProject;
	private LTTngExperimentEntry[] experiments = new LTTngExperimentEntry[0];

	/**
	 * @param resource
	 */
	public LTTngExperimentFolder(LTTngProject project, Folder folder) {
		fExperimentFolder = folder;
		fProject = project;
		try {
			IResource[] resources = folder.members();
			int nbExperiments = resources.length;
			experiments = new LTTngExperimentEntry[nbExperiments];
			for (int i = 0; i < nbExperiments; i++) {
				if (resources[i] instanceof IFolder) {
					experiments[i] = new LTTngExperimentEntry(project, (IFolder) resources[i]);
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public LTTngProject getProject() {
		return fProject;
	}

	/**
	 * 
	 */
	public Folder getFolder() {
		return fExperimentFolder;
	}

	/**
	 * 
	 */
	public String getName() {
		return fExperimentFolder.getName();
	}
	/**
	 * 
	 */
	public LTTngExperimentEntry[] getExperiments() {
		return experiments;
	}


}
