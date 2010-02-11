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

import java.util.Vector;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * <b><u>LTTngExperimentEntry</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class LTTngExperimentEntry {

	private final IFolder fResource;
	private final LTTngProject fProject;
	private Vector<LTTngTraceEntry> traces = new Vector<LTTngTraceEntry>();

	/**
	 * @param name
	 */
	public LTTngExperimentEntry(LTTngProject project, IFolder resource) {
		fResource = resource;
		fProject = project;
		try {
			IResource[] resources = resource.members();
			int nbTraces = resources.length;
			for (int i = 0; i < nbTraces; i++) {
				if (resources[i] instanceof IFolder)
					traces.add(new LTTngTraceEntry(fProject, (IFolder) resources[i]));
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @return
	 */
	public String getName() {
		return fResource.getName();
	}

	/**
	 * @return
	 */
	public IFolder getFolder() {
		return fResource;
	}

	/**
	 * @return
	 */
	public void addTrace(LTTngTraceEntry trace) {
		traces.add(new LTTngTraceEntry(fProject, (IFolder) trace.getResource()));
	}

	/**
	 * 
	 */
	public LTTngTraceEntry[] getTraces() {
		LTTngTraceEntry[] entries = new LTTngTraceEntry[traces.size()];
		return traces.toArray(entries);
	}

	/**
	 * @return
	 */
	public LTTngProject getProject() {
		return fProject;
	}

}