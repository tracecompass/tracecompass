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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * <b><u>LTTngTraceFolder</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class LTTngTraceFolder {

	private final IFolder fTraceFolder;
	private final LTTngProject fProject;
	private LTTngTraceEntry[] traces = new LTTngTraceEntry[0];

	/**
	 * @param resource
	 */
	public LTTngTraceFolder(LTTngProject project, IFolder folder) {
		fTraceFolder = folder;
		fProject = project;
		try {
			IResource[] resources = folder.members();
			int nbTraces = resources.length;
			traces = new LTTngTraceEntry[nbTraces];
			for (int i = 0; i < nbTraces; i++) {
				if (resources[i] instanceof IFolder)
					traces[i] = new LTTngTraceEntry(project, (IFolder) resources[i]);
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
	public IFolder getFolder() {
		return fTraceFolder;
	}

	/**
	 * 
	 */
	public String getName() {
		return fTraceFolder.getName();
	}

	/**
	 * 
	 */
	public LTTngTraceEntry[] getTraces() {
		return traces;
	}

}
