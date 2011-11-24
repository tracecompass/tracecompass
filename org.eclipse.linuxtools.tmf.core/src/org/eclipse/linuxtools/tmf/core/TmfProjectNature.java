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

package org.eclipse.linuxtools.tmf.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * <b><u>TmfProjectNature</u></b>
 * <p>
 * This is really a marker for the tracing projects.
 */
public class TmfProjectNature implements IProjectNature {

	public static final String ID = "org.eclipse.linuxtools.tmf.project.nature"; //$NON-NLS-1$

	private IProject fProject;
	
	@Override
	public void configure() throws CoreException {
	}

	@Override
	public void deconfigure() throws CoreException {
	}

	@Override
	public IProject getProject() {
		return fProject;
	}

	@Override
	public void setProject(IProject project) {
		fProject = project;
	}

}
