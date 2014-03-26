/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Added project creation utility
 *   Patrick Tasse - Refactor resource change listener
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.TmfProjectNature;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Factory class storing TMF tracing projects and creating TMF project model elements.
 * <p>
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfProjectRegistry implements IResourceChangeListener {

    // Create the singleton instance
    static {
        new TmfProjectRegistry();
    }

    // The map of project resource to project model elements
    private static Map<IProject, TmfProjectElement> registry = new HashMap<>();

    private TmfProjectRegistry() {
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
    }

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
            // force the model to be populated
            element.refreshChildren();
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
    public static IProject createProject(String projectName, final URI projectLocation, IProgressMonitor monitor) {

        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        final IProject project = root.getProject(projectName);
        WorkspaceModifyOperation action = new WorkspaceModifyOperation() {
            @Override
            protected void execute(IProgressMonitor progressMonitor) throws CoreException, InvocationTargetException, InterruptedException {
                if (!project.exists()) {
                    IProjectDescription description = workspace.newProjectDescription(project.getName());
                    if (projectLocation != null) {
                        description.setLocationURI(projectLocation);
                    }
                    project.create(description, progressMonitor);
                }

                if (!project.isOpen()) {
                    project.open(progressMonitor);
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
                folder = project.getFolder(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER_NAME);

                if (!folder.exists()) {
                    folder.create(true, true, null);
                }
            }
        };
        try {
            PlatformUI.getWorkbench().getProgressService().run(false, false, action);
        } catch (InvocationTargetException e) {
            Activator.getDefault().logError("Error creating TMF project " + project.getName(), e); //$NON-NLS-1$
        } catch (InterruptedException e) {
        }
        return project;
    }

    // ------------------------------------------------------------------------
    // IResourceChangeListener
    // ------------------------------------------------------------------------

    /**
     * @since 3.0
     */
    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
            for (IResourceDelta delta : event.getDelta().getAffectedChildren()) {
                if (delta.getResource() instanceof IProject) {
                    IProject project = (IProject) delta.getResource();
                    try {
                        if (delta.getKind() == IResourceDelta.CHANGED &&
                                project.isOpen() && project.hasNature(TmfProjectNature.ID)) {
                            TmfProjectElement projectElement = getProject(project, true);
                            projectElement.refresh();
                        } else if (delta.getKind() == IResourceDelta.REMOVED) {
                            registry.remove(project);
                        }
                    } catch (CoreException e) {
                        Activator.getDefault().logError("Error handling resource change event for " + project.getName(), e); //$NON-NLS-1$
                    }
                }
            }
        }
    }

}
