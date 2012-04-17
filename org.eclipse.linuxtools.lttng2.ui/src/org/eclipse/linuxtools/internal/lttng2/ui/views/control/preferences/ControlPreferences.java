/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.lttng2.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.logging.ControlCommandLogger;

/**
 * <b><u>ControlPreference</u></b>
 * <p>
 * Singleton class to access LTTng tracer control preferences.
 * </p>
 */
public class ControlPreferences {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    public static final String TRACE_CONTROL_LOG_FILENAME = "lttng_control.log"; //$NON-NLS-1$

    // Preference strings
    public static final String TRACE_CONTROL_TRACING_GROUP_PREF = "trace.control.tracing.group"; //$NON-NLS-1$
    public static final String TRACE_CONTROL_LOG_COMMANDS_PREF = "trace.control.log.commands"; //$NON-NLS-1$
    public static final String TRACE_CONTROL_LOG_APPEND_PREF = "trace.control.log.append"; //$NON-NLS-1$
    public static final String TRACE_CONTROL_LOG_FILE_PATH_PREF = "trace.control.log.path"; //$NON-NLS-1$  
    public static final String TRACE_CONTROL_VERBOSE_LEVEL_PREF = "trace.control.verbose.level"; //$NON-NLS-1$
    public static final String TRACE_CONTROL_VERBOSE_LEVEL_NONE = "trace.control.verbose.level.none"; //$NON-NLS-1$
    public static final String TRACE_CONTROL_VERBOSE_LEVEL_VERBOSE = "trace.control.verbose.level.v"; //$NON-NLS-1$
    public static final String TRACE_CONTROL_VERBOSE_LEVEL_V_VERBOSE = "trace.control.verbose.level.vv"; //$NON-NLS-1$
    public static final String TRACE_CONTROL_VERBOSE_LEVEL_V_V_VERBOSE = "trace.control.verbose.level.vvv"; //$NON-NLS-1$

    public static final String TRACE_CONTROL_DEFAULT_TRACING_GROUP = "tracing"; //$NON-NLS-1$    
    public static final String TRACE_CONTROL_DEFAULT_LOG_PATH = "${workspace_loc}/" + TRACE_CONTROL_LOG_FILENAME; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private static ControlPreferences fInstance = null;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    private ControlPreferences() {
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    public synchronized static ControlPreferences getInstance() {
        if (fInstance == null) {
            fInstance = new ControlPreferences();
        }
        return fInstance;
    }

    /**
     * @return the preference store
     */
    public IPreferenceStore getPreferenceStore() {
        return Activator.getDefault().getPreferenceStore();
    }

    /**
     * @return true if tracing group is set to default
     */
    public boolean isDefaultTracingGroup() {
        IPreferenceStore store = getPreferenceStore();
        return store.getString(TRACE_CONTROL_TRACING_GROUP_PREF).equals(store.getDefaultString(TRACE_CONTROL_TRACING_GROUP_PREF));
    }

    /**
     * @return value of tracing group preference 
     */
    public String getTracingGroup() {
        return getPreferenceStore().getString(TRACE_CONTROL_TRACING_GROUP_PREF);
    }

    /**
     * @return whether is logging is enabled 
     */
    public boolean isLoggingEnabled() {
        return getPreferenceStore().getBoolean(TRACE_CONTROL_LOG_COMMANDS_PREF);
    }

    /**
     * @return whether an existing log file will appended or not  
     */
    public boolean isAppend() {
        return getPreferenceStore().getBoolean(ControlPreferences.TRACE_CONTROL_LOG_APPEND_PREF);
    }

    /**
     * @return verbose level preference
     */
    public String getVerboseLevel() {
        return getPreferenceStore().getString(TRACE_CONTROL_VERBOSE_LEVEL_PREF);
    }
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Initializes the control preferences (e.g. enable open log file)
     */
    public void init() {
        if (getPreferenceStore().getBoolean(ControlPreferences.TRACE_CONTROL_LOG_COMMANDS_PREF)) {
            ControlCommandLogger.init(ControlPreferences.TRACE_CONTROL_LOG_FILENAME, isAppend());
        } 
    }

    /**
     * Disposes any resource (e.g. close log file). 
     */
    public void dispose() {
        ControlCommandLogger.close();
    }
}
