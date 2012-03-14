/*******************************************************************************
 * Copyright (c) 2011 MontaVista Software
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Yufen Kuo (ykuo@mvista.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

public class TraceHelper {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static String TRACE_LIB_PATH = "traceLibraryPath"; //$NON-NLS-1$
    private static String QUALIFIER = "org.eclipse.linuxtools.lttng.jni"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
     * Get Trace Library Directory from Project Preference.
     * 
     * @param project
     *            The <b>project</b> in the workspace.
     * @return The <b>directory</b> of the trace libraries. null if not defined
     */
    public static String getTraceLibDirFromProject(IProject project) {
        if (project != null && project.exists()) {
            return getProjectPreference(project, TRACE_LIB_PATH);
        }
        return null;
    }

    /**
     * Get the project preference with the specified name
     * 
     * @param project
     *            The <b>project</b> in the workspace.
     * @param preferenceName
     *            name of the preference.
     * @return The project preference value.
     */
    public static String getProjectPreference(IProject project, String preferenceName) {
        if (project.exists()) {
            IEclipsePreferences prefs = new ProjectScope(project).getNode(QUALIFIER);

            return prefs.get(preferenceName, null);
        }
        return null;
    }

    /**
     * Set the project preference with the specified value
     * 
     * @param project
     *            The <b>project</b> in the workspace.
     * @param preferenceName
     *            name of the preference.
     * @param preferenceValue
     *            value of the preference.
     * @return true if preference is successfully set, false otherwise.
     */
    public static boolean setProjectPreference(IProject project, String preferenceName, String preferenceValue) {
        if (project.exists()) {
            IEclipsePreferences prefs = new ProjectScope(project).getNode(QUALIFIER);

            prefs.put(preferenceName, preferenceValue);
            try {
                prefs.flush();
                return true;
            } catch (org.osgi.service.prefs.BackingStoreException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    /**
     * Remove the project preference with the specified name
     * 
     * @param project
     *            The <b>project</b> in the workspace.
     * @param preferenceName
     *            name of the preference.
     * @return true if preference name is successfully remove, false otherwise.
     */
    public static boolean removeProjectPreference(IProject project, String preferenceName) {
        if (project.exists()) {
            IEclipsePreferences prefs = new ProjectScope(project).getNode(QUALIFIER);

            prefs.remove(preferenceName);
            try {
                prefs.flush();
                return true;
            } catch (org.osgi.service.prefs.BackingStoreException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

}
