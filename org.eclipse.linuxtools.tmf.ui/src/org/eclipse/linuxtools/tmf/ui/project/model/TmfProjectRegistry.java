/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Added project creation utility
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.TmfProjectNature;

/**
 * Factory class storing TMF tracing projects and creating TMF project model elements.
 * <p>
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfProjectRegistry {

    // The map of project resource to project model elements
    private static Map<IProject, TmfProjectElement> registry = new HashMap<>();

    /**
     * Get the project model element for a project resource
     * @param project the project resource
     * @return the project model element or null if it does not exist
     */
    public static synchronized TmfProjectElement getProject(IProject project) {
        return getProject(project, false);
    }

    /**
     * Get the project model element for a project resource
     * @param project the project resource
     * @param force a flag controlling whether a new project should be created if it doesn't exist
     * @return the project model element
     */
    public static synchronized TmfProjectElement getProject(IProject project, boolean force) {
        TmfProjectElement element = registry.get(project);
        if (element == null && force) {
            registry.put(project, new TmfProjectElement(project.getName(), project, null));
            element = registry.get(project);
        }
        return element;
    }

    /**
     * Utility method to create a tracing project.
     *
     * @param projectName
     *          - A project name
     * @param projectLocation
     *          - A project location URI. Use null for default location (which is workspace).
     * @param monitor
     *          - A progress monitor
     * @return the IProject object or null
     * @since 2.0
     */
    public static IProject createProject(String projectName, URI projectLocation, IProgressMonitor monitor) {

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IProject project = root.getProject(projectName);
        try {
            if (!project.exists()) {
                IProjectDescription description = workspace.newProjectDescription(project.getName());
                if (projectLocation != null) {
                    description.setLocationURI(projectLocation);
                }
                project.create(description, monitor);
            }

            if (!project.isOpen()) {
                project.open(monitor);
            }

            IProjectDescription description = project.getDescription();
            description.setNatureIds(new String[] { TmfProjectNature.ID });
            project.setDescription(description, null);

            IFolder folder = project.getFolder(TmfTraceFolder.TRACE_FOLDER_NAME);
            if (!folder.exists()) {
                folder.create(true, true, null);
            }

            folder = project.getFolder(TmfExperimentFolder.EXPER_FOLDER_NAME);
            if (!folder.exists()) {
                folder.create(true, true, null);
            }

            // create folder for supplementary tracing files
            folder = project.getFolder(TmfCommonConstants.TRACE_SUPPLEMENATARY_FOLDER_NAME);

            if (!folder.exists()) {
                folder.create(true, true, null);
            }
            return project;
        } catch (CoreException e) {
            Activator.getDefault().logError("Error creating TMF project " + project.getName(), e); //$NON-NLS-1$
        }
        return null;
    }

}
