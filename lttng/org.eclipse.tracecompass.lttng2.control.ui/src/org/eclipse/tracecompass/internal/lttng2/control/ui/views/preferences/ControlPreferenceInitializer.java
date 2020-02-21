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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * <p>
 * A class to initialize the preferences.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class ControlPreferenceInitializer extends AbstractPreferenceInitializer {

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
