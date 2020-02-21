/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.preferences;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.File;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.logging.ControlCommandLogger;

/**
 * <p>
 * Singleton class to access LTTng tracer control preferences.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class ControlPreferences {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Trace control log file
     */
    public static final String TRACE_CONTROL_LOG_FILENAME = "lttng_tracer_control.log"; //$NON-NLS-1$

    // Preference strings
    /**
     * The tracing group preference
     */
    public static final String TRACE_CONTROL_TRACING_GROUP_PREF = "trace.control.tracing.group"; //$NON-NLS-1$
    /**
     * The log commands preference
     */
    public static final String TRACE_CONTROL_LOG_COMMANDS_PREF = "trace.control.log.commands"; //$NON-NLS-1$
    /**
     * The log append preference
     */
    public static final String TRACE_CONTROL_LOG_APPEND_PREF = "trace.control.log.append"; //$NON-NLS-1$
    /**
     * The log file path preference
     */
    public static final String TRACE_CONTROL_LOG_FILE_PATH_PREF = "trace.control.log.path"; //$NON-NLS-1$
    /**
     * The verbose level preference
     */
    public static final String TRACE_CONTROL_VERBOSE_LEVEL_PREF = "trace.control.verbose.level"; //$NON-NLS-1$
    /**
     * The verbose level value for none
     */
    public static final String TRACE_CONTROL_VERBOSE_LEVEL_NONE = "trace.control.verbose.level.none"; //$NON-NLS-1$
    /**
     * The verbose level value for level 1 (-v)
     */
    public static final String TRACE_CONTROL_VERBOSE_LEVEL_VERBOSE = "trace.control.verbose.level.v"; //$NON-NLS-1$
    /**
     * The verbose level value for level 2 (-vv)
     */
    public static final String TRACE_CONTROL_VERBOSE_LEVEL_V_VERBOSE = "trace.control.verbose.level.vv"; //$NON-NLS-1$
    /**
     * The verbose level value for level 3 (-vvv)
     */
    public static final String TRACE_CONTROL_VERBOSE_LEVEL_V_V_VERBOSE = "trace.control.verbose.level.vvv"; //$NON-NLS-1$
    /**
     * The default tracing group
     */
    public static final String TRACE_CONTROL_DEFAULT_TRACING_GROUP = "tracing"; //$NON-NLS-1$
    /**
     * The default tracing log file name with absolute path
     */
    public static final String TRACE_CONTROL_DEFAULT_LOG_PATH = System.getProperty("user.home") + File.separator + TRACE_CONTROL_LOG_FILENAME; //$NON-NLS-1$
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
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The trace control preferences singleton instance.
     */
    private static ControlPreferences fInstance = null;
    /**
     * The preference store reference
     */
    private IPreferenceStore fPreferenceStore = null;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Private constructor
     */
    private ControlPreferences() {
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Returns the trace control preferences singleton instance
     *
     * @return the trace control preferences singleton instance
     */
    public static synchronized ControlPreferences getInstance() {
        if (fInstance == null) {
            fInstance = new ControlPreferences();
        }
        return fInstance;
    }

    /**
     * @return the preference store
     */
    public IPreferenceStore getPreferenceStore() {
        return fPreferenceStore;
    }

    /**
     * @return true if tracing group is set to default
     */
    public boolean isDefaultTracingGroup() {
        return fPreferenceStore.getString(TRACE_CONTROL_TRACING_GROUP_PREF).equals(fPreferenceStore.getDefaultString(TRACE_CONTROL_TRACING_GROUP_PREF));
    }

    /**
     * @return value of tracing group preference
     */
    public @NonNull String getTracingGroup() {
        return checkNotNull(fPreferenceStore.getString(TRACE_CONTROL_TRACING_GROUP_PREF));
    }

    /**
     * @return whether is logging is enabled
     */
    public boolean isLoggingEnabled() {
        return fPreferenceStore.getBoolean(TRACE_CONTROL_LOG_COMMANDS_PREF);
    }

    /**
     * @return whether an existing log file will appended or not
     */
    public boolean isAppend() {
        return fPreferenceStore.getBoolean(ControlPreferences.TRACE_CONTROL_LOG_APPEND_PREF);
    }

    /**
     * @return verbose level preference
     */
    public String getVerboseLevel() {
        return fPreferenceStore.getString(TRACE_CONTROL_VERBOSE_LEVEL_PREF);
    }

    /**
     * @return absolute log file path
     */
    public String getLogfilePath() {
        return fPreferenceStore.getString(TRACE_CONTROL_LOG_FILE_PATH_PREF);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Initializes the control preferences (e.g. enable open log file)
     *
     * @param preferenceStore
     *            The preference store to assign
     */
    public void init(IPreferenceStore preferenceStore) {
        fPreferenceStore = preferenceStore;

        if (fPreferenceStore.getBoolean(ControlPreferences.TRACE_CONTROL_LOG_COMMANDS_PREF)) {
            ControlCommandLogger.init(getLogfilePath(), isAppend());
        }
    }

    /**
     * Disposes any resource (e.g. close log file).
     */
    public void dispose() {
        ControlCommandLogger.close();
    }
}
