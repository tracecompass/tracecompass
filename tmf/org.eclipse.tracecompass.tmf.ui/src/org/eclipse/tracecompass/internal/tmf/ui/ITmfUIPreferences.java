/*******************************************************************************
 * Copyright (c) 2015, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui;

/**
 * Preferences for TMF UI
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITmfUIPreferences {
    /**
     * Preference for aligning the views based on the time axis
     */
    String PREF_ALIGN_VIEWS = "PREF_ALIGN_VIEWS"; //$NON-NLS-1$

    /**
     * Preference (not user visible) for saving the last location used when using "Open Trace"
     */
    String PREF_SAVED_OPEN_FILE_LOCATION = "PREF_LAST_OPEN_FILE_LOCATION"; //$NON-NLS-1$

    /**
     * Preference to switch to associated perspective when opening a trace
     */
    String SWITCH_TO_PERSPECTIVE = "SWITCH_TO_PERSPECTIVE"; //$NON-NLS-1$

    /**
     * Preference to display a trace's time range in the project explorer
     */
    String TRACE_DISPLAY_RANGE_PROJECTEXPLORER = "EXPLORER_TIMERANGE"; //$NON-NLS-1$

    /**
     * Preference to confirm before deleting supplementary files
     */
    String CONFIRM_DELETION_SUPPLEMENTARY_FILES = "CONFIRM_DELETION_SUPPLEMENTARY_FILES"; //$NON-NLS-1$

    /**
     * Preference for behavior on resource refresh: always delete supplementary files and close editors.
     */
    String ALWAYS_CLOSE_ON_RESOURCE_CHANGE = "ALWAYS_CLOSE_ON_RESOURCE_CHANGE"; //$NON-NLS-1$
    /**
     * Preference for hiding the Many Entries Selected MessageDialog
     */
    String HIDE_MANY_ENTRIES_SELECTED_TOGGLE = "HIDE_MANY_ENTRIES_SELECTED_TOGGLE"; //$NON-NLS-1$

    /**
     * Preference to use HTML Tooltips
     */
    String USE_BROWSER_TOOLTIPS = "USE_HTML_TOOLTIPS"; //$NON-NLS-1$
}
