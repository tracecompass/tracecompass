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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.tracecompass.internal.tmf.core.markers.MarkerConfigXmlParser;
import org.eclipse.tracecompass.internal.tmf.core.markers.MarkerSet;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;

/**
 * Utility class for markers
 */
public class MarkerUtils {

    private static final String MARKER_SET_KEY = "marker.set"; //$NON-NLS-1$

    private static MarkerSet fDefaultMarkerSet = null;

    /**
     * Get the default marker set
     *
     * @return the default marker set, or null if none is set
     */
    public static synchronized @Nullable MarkerSet getDefaultMarkerSet() {
        String id = getDialogSettings().get(MARKER_SET_KEY);
        if (id == null) {
            fDefaultMarkerSet = null;
        } else {
            if (fDefaultMarkerSet == null || !fDefaultMarkerSet.getId().equals(id)) {
                for (MarkerSet markerSet : MarkerConfigXmlParser.getMarkerSets()) {
                    if (markerSet.getId().equals(id)) {
                        fDefaultMarkerSet = markerSet;
                    }
                }
            }
        }
        return fDefaultMarkerSet;
    }

    /**
     * Set the default marker set
     *
     * @param markerSet
     *            the default marker set, or null to set none
     */
    public static synchronized void setDefaultMarkerSet(@Nullable MarkerSet markerSet) {
        fDefaultMarkerSet = markerSet;
        String id = (markerSet == null) ? null : markerSet.getId();
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
