/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.markers;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;

/**
 * Utility class for markers
 */
public class MarkerUtils {

    private static final String MARKER_SET_KEY = "marker.set"; //$NON-NLS-1$

    /**
     * Get the default marker set id
     *
     * @return the default marker set id, or null if none is set
     */
    public static String getDefaultMarkerSetId() {
        return getDialogSettings().get(MARKER_SET_KEY);
    }

    /**
     * Set the default marker set id
     *
     * @param id
     *            the default marker set id, or null to set none
     */
    public static void setDefaultMarkerSetId(String id) {
        getDialogSettings().put(MARKER_SET_KEY, id);
    }

    private static IDialogSettings getDialogSettings() {
        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        IDialogSettings dialogSettings = settings.getSection(MarkerUtils.class.getName());
        if (dialogSettings == null) {
            dialogSettings = settings.addNewSection(MarkerUtils.class.getName());
        }
        return dialogSettings;
    }
}
