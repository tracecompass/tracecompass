/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.tmf.ui.project.model;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

/**
 * Singleton class to access the project model preferences of Trace Compass.
 *
 * This preference allows for customization of project model element label and
 * icon.
 *
 * @author Bernd Hufmann
 * @since 3.2
 */
public final class TmfProjectModelPreferences {

    private static final @NonNull String DEFAULT_LABEL_NAME = "Trace Compass"; //$NON-NLS-1$
    private static final @NonNull Image TRACECOMPASS_ICON = TmfProjectModelIcons.TRACECOMPASS_ICON;

    private static String fProjectModelLabel = DEFAULT_LABEL_NAME;
    private static Image fProjectModelIcon = null;

    /**
     * Private constructor
     */
    private TmfProjectModelPreferences() {
    }

    /**
     * Sets the preference of the project model element label
     *
     * @param bundleSymbolicName
     *            the symbolic name of the bundle defining the icon
     * @param label
     *            the label
     */
    public static synchronized void setProjectModelLabel(@NonNull String bundleSymbolicName, @NonNull String label) {
        Bundle bundle = Platform.getBundle(bundleSymbolicName);
        if (bundle == null) {
            return;
        }
        fProjectModelLabel = label;
    }

    /**
     * Sets the preference of the project model element icon
     *
     * @param bundleSymbolicName
     *            the symbolic name of the bundle defining the icon
     * @param iconPath
     *            the icon relative path to the bundle root
     */
    public static synchronized void setProjectModelIcon(@NonNull String bundleSymbolicName, @NonNull String iconPath) {
        Bundle bundle = Platform.getBundle(bundleSymbolicName);
        Image icon = TmfProjectModelIcons.loadIcon(bundle, iconPath);
        if (icon != null) {
             fProjectModelIcon = icon;
        }
    }

    /**
     * Get the preference value of the project model element label
     *
     * @return the label of the project model element
     */
    public static synchronized @NonNull String getProjectModelLabel() {
        String label = fProjectModelLabel;
        if (label == null) {
            return DEFAULT_LABEL_NAME;
        }
        return label;
    }

    /**
     * Get the preference value of the project model element icon
     *
     * @return the image object of the project element icon
     */
    public static synchronized @Nullable Image getProjectModelIcon() {
        Image image = fProjectModelIcon;
        if (image == null) {
            return TRACECOMPASS_ICON;
        }
        return image;
    }
}
