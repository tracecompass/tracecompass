/**********************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.tmf.remote.core.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.tracecompass.internal.tmf.remote.core.Activator;

/**
 * Singleton class to access the remote control preferences of Trace Compass.
 *
 * @author Bernd Hufmann
 */
public final class TmfRemotePreferences {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The command time-out preference
     */
    public static final String TRACE_CONTROL_COMMAND_TIMEOUT_PREF = "trace.control.command.timeout"; //$NON-NLS-1$
    /**
     * Default timeout value used for executing commands, in seconds
     */
    public static final int TRACE_CONTROL_DEFAULT_TIMEOUT_VALUE = 15;
    /**
     * Minimum timeout value used for executing commands, in seconds
     */
    public static final int TRACE_CONTROL_MIN_TIMEOUT_VALUE = 5;
    /**
     * Maximum timeout value used for executing commands, in seconds
     */
    public static final int TRACE_CONTROL_MAX_TIMEOUT_VALUE = 600;

    // ------------------------------------------------------------------------
    // operations
    // ------------------------------------------------------------------------

    /**
     * Initialize the default preferences and the singleton
     */
    public static void init() {
        IEclipsePreferences defaultPreferences = DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID);

       //Set default User ID if none already stored in preferences
        defaultPreferences.put(TRACE_CONTROL_COMMAND_TIMEOUT_PREF, String.valueOf(TmfRemotePreferences.TRACE_CONTROL_DEFAULT_TIMEOUT_VALUE));

    }

    private static void prefToMap(IEclipsePreferences node, Map<String, String> prefsMap, String key, String defaultValue) {
        prefsMap.put(key, node.get(key, defaultValue));
    }

    /**
     * Get the default preferences map
     *
     * @return a collection containing the default preferences
     */
    public static Map<String, String> getDefaultPreferenceMap() {
        return getPreferenceMap(true);
    }

    /**
     * Get the current preferences map
     *
     * @return a collection containing the current preferences
     */
    public static Map<String, String> getPreferenceMap() {
        return getPreferenceMap(false);
    }

    private static Map<String, String> getPreferenceMap(boolean defaultValues) {
        Map<String, String> prefsMap = new HashMap<>();
        IEclipsePreferences prefs = defaultValues ? DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID) : InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        prefToMap(prefs, prefsMap, TRACE_CONTROL_COMMAND_TIMEOUT_PREF, String.valueOf(TmfRemotePreferences.TRACE_CONTROL_DEFAULT_TIMEOUT_VALUE));
        return prefsMap;
    }

    /**
     * @return command timeout value
     */
    public static int getCommandTimeout() {
        return Integer.parseInt(getPreferenceMap().get(TRACE_CONTROL_COMMAND_TIMEOUT_PREF));
    }

}
