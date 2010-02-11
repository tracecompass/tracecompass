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

/**
 * <b><u>LTTngTraceEntry</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class LTTngTraceEntry {
	
	private final LTTngProject fProject;
	private final IFolder fFolder;

	/**
	 * @param name
	 */
	public LTTngTraceEntry(LTTngProject project, IFolder folder) {
		fProject = project;
		fFolder = folder;
	}

	/**
	 * @return
	 */
	public LTTngProject getProject() {
		return fProject;
	}

	/**
	 * @return
	 */
	public String getName() {
		return fFolder.getName();
	}

	/**
	 * @return
	 */
	public IFolder getResource() {
		return fFolder;
	}
}
