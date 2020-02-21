/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * The TMF basic tracing project nature.
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfProjectNature implements IProjectNature {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The nature ID
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.project.nature"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private IProject fProject;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void configure() throws CoreException {
        // Do nothing
    }

    @Override
    public void deconfigure() throws CoreException {
        // Do nothing
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
