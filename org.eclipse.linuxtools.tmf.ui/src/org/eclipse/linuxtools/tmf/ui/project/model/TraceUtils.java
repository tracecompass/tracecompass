/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Marc-Andre Laperle - Add method to get opened tmf projects
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfProjectNature;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

/**
 * Utility class for common tmf.ui functionalities
 *
 * @since 2.1
 */
public class TraceUtils {

    /**
     * Displays an error message in a box
     *
     * @param boxTitle
     *            The message box title
     * @param errorMsg
     *            The error message to display
     */
    public static void displayErrorMsg(final String boxTitle, final String errorMsg) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                final MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
                mb.setText(boxTitle);
                mb.setMessage(errorMsg);
                mb.open();
            }
        });
    }

    /**
     * Get the opened (accessible) projects with Tmf nature
     *
     * @return the Tmf projects
     * @since 2.2
     */
    public static List<IProject> getOpenedTmfProjects() {
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        List<IProject> tmfProjects = new ArrayList<IProject>();
        for (IProject project : projects) {
            try {
                if (project.isAccessible() && project.getNature(TmfProjectNature.ID) != null) {
                    tmfProjects.add(project);
                }
            } catch (CoreException e) {
                Activator.getDefault().logError("Error getting opened tmf projects", e); //$NON-NLS-1$
            }
        }
        return tmfProjects;
    }
}
