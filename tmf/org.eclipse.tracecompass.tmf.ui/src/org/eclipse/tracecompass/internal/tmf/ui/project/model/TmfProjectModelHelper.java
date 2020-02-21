/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.ui.project.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Helper utility class for the project model. It provides methods for verifying
 * shadow projects created when the tracing nature is added to an existing project.
 *
 * @author Bernd Hufmann
 *
 */
public class TmfProjectModelHelper {

    // Shadow project name
    private static final String SHADOW_PROJECT_NAME_PREFIX = ".tracecompass-"; //$NON-NLS-1$

    private TmfProjectModelHelper() {
        // Do nothing, private constructor
    }

    /**
     * Returns the parent project for a given shadow project.
     *
     * @param shadowProject
     *            the shadow project
     * @return shadow project or null if the shadow project doesn't exist
     */
    public static @Nullable IProject getProjectFromShadowProject(IProject shadowProject) {
        if (shadowProject != null && isShadowProject(shadowProject)) {
            String projName = shadowProject.getName().substring(SHADOW_PROJECT_NAME_PREFIX.length());
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IProject project = workspace.getRoot().getProject(projName);
            if (project.exists()) {
                return project;
            }
        }
        return null;
    }

    /**
     * Returns whether the given project is a shadow project.
     *
     * @param project
     *            the project
     * @return true if given project is a shadow project or not
     */
    public static boolean isShadowProject(@NonNull IProject project) {
        return project.getName().startsWith(SHADOW_PROJECT_NAME_PREFIX);
    }

    /**
     * Returns whether the shadow project of a given project exists in the
     * workspace.
     *
     * @param project
     *            the project
     * @return true if the shadow project exists.
     *
     */
    public static boolean shadowProjectExists(@NonNull IProject project) {
        IProject shadowProject = getShadowProject(project);
        return shadowProject.exists();
    }

    /**
     * Returns whether the shadow project of a given project exists in the
     * workspace and is accessible.
     *
     * @param project
     *            the project
     * @return true if the shadow project exists and accessible.
     *
     */
    public static boolean shadowProjectAccessible(@NonNull IProject project) {
        IProject shadowProject = getShadowProject(project);
        return shadowProject.exists() && shadowProject.isAccessible();
    }

    /**
     * Gets the shadow project name from an input project name.
     *
     * @param name
     *            the name string
     *
     * @return shadow project name
     */
    public static String getShadowProjectName(String name) {
        return SHADOW_PROJECT_NAME_PREFIX + name;
    }

    /**
     * Gets the shadow project from an input project.
     * It doesn't check if the project exists or not.
     *
     * @param project
     *            the input project
     *
     * @return shadow project
     */
    public static IProject getShadowProject(IProject project) {
        return getShadowProject(project.getName());
    }

    /**
     * Gets the shadow project from an input project name.
     * It doesn't check if the project exists or not.
     *
     * @param name
     *            the input project name
     *
     * @return shadow project
     */
    public static IProject getShadowProject (String name) {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        return workspace.getRoot().getProject(getShadowProjectName(name));
    }
}
