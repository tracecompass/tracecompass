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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * <b><u>ControlPreferenceInitializer</u></b>
 * <p>
 * A class to initialize the preferences.
 * </p>
 */
public class ControlPreferenceInitializer extends AbstractPreferenceInitializer {
    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        final IPreferenceStore store = ControlPreferences.getInstance().getPreferenceStore();

        //Set default User ID if none already stored in preferences
        store.setDefault(ControlPreferences.TRACE_CONTROL_TRACING_GROUP_PREF, ControlPreferences.TRACE_CONTROL_DEFAULT_TRACING_GROUP);
        store.setDefault(ControlPreferences.TRACE_CONTROL_LOG_APPEND_PREF, false);
        store.setDefault(ControlPreferences.TRACE_CONTROL_LOG_FILE_PATH_PREF, ControlPreferences.TRACE_CONTROL_DEFAULT_LOG_PATH);
        store.setDefault(ControlPreferences.TRACE_CONTROL_LOG_COMMANDS_PREF, false); 
        store.setDefault(ControlPreferences.TRACE_CONTROL_VERBOSE_LEVEL_PREF, ControlPreferences.TRACE_CONTROL_VERBOSE_LEVEL_NONE);
    }
}
