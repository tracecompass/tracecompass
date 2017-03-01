/*******************************************************************************
 * Copyright (c) 2015, 2017 Ericsson
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
}
