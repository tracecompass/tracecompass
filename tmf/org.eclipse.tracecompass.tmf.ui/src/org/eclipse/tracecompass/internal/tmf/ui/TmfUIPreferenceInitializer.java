/*******************************************************************************
 * Copyright (c) 2015, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;

/**
 * Initializes TMF UI preferences
 */
public class TmfUIPreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IEclipsePreferences defaultPreferences = DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        defaultPreferences.putBoolean(ITmfUIPreferences.PREF_ALIGN_VIEWS, true);
        defaultPreferences.put(ITmfUIPreferences.SWITCH_TO_PERSPECTIVE, MessageDialogWithToggle.PROMPT);
        defaultPreferences.putBoolean(ITmfUIPreferences.CONFIRM_DELETION_SUPPLEMENTARY_FILES, true);
        defaultPreferences.putBoolean(ITmfUIPreferences.FILTER_EMPTY_ROWS, true);
    }
}
