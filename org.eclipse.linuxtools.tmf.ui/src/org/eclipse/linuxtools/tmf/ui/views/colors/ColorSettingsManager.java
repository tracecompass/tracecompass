/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Bernd Hufmann - Updated to use RGB for the tick color
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.colors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

/**
 * Static class for managing color settings.
 *
 * @version 1.0
 * @author Patrick Tasse
 *
 */
public class ColorSettingsManager {

    // The color settings file name
	private static final String COLOR_SETTINGS_FILE_NAME = "color_settings.xml"; //$NON-NLS-1$

	// The path for the color settings file
	private static final String COLOR_SETTINGS_PATH_NAME =
        Activator.getDefault().getStateLocation().addTrailingSeparator().append(COLOR_SETTINGS_FILE_NAME).toString();

	// The default color setting
	private static final ColorSetting DEFAULT_COLOR_SETTING = new ColorSetting(
			Display.getDefault().getSystemColor(SWT.COLOR_LIST_FOREGROUND).getRGB(),
			Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB(),
			Display.getDefault().getSystemColor(SWT.COLOR_LIST_FOREGROUND).getRGB(),
			null);

	/**
	 * Special value for priority if unknown.
	 */
	public static final int PRIORITY_NONE = Integer.MAX_VALUE;

	// The stored color settings
	private static ColorSetting[] fColorSettings = ColorSettingsXML.load(COLOR_SETTINGS_PATH_NAME);

	// The listener list
	private static List<IColorSettingsListener> fListeners = new ArrayList<>();

	/**
	 * Returns an array of color settings.
	 *
	 * @return an array of color settings.
	 */
	public static ColorSetting[] getColorSettings() {
		return (fColorSettings != null) ? Arrays.copyOf(fColorSettings, fColorSettings.length) : null;
	}

	/**
	 * Sets the array of color settings.
	 *
	 * @param colorSettings A array of color settings to set
	 */
	public static void setColorSettings(ColorSetting[] colorSettings) {
		fColorSettings = (colorSettings != null) ? Arrays.copyOf(colorSettings, colorSettings.length) : null;
		ColorSettingsXML.save(COLOR_SETTINGS_PATH_NAME, fColorSettings);
		fireColorSettingsChanged();
	}

	    /**
     * Gets the color settings that matches the filter for given event.
     *
     * @param event
     *            The event to check
     *
     * @return color settings defined for filter if found else default color
     *         settings
     */
	public static ColorSetting getColorSetting(ITmfEvent event) {
        for (int i = 0; i < fColorSettings.length; i++) {
        	ColorSetting colorSetting = fColorSettings[i];
        	if (colorSetting.getFilter() != null && colorSetting.getFilter().matches(event)) {
        		return colorSetting;
        	}
        }
        return DEFAULT_COLOR_SETTING;
	}

	/**
	 * Gets the color settings priority for the given event.
	 *
	 * @param event A event the event to check
	 * @return the priority defined for the filter else PRIORITY_NONE
	 */
	public static int getColorSettingPriority(ITmfEvent event) {
        for (int i = 0; i < fColorSettings.length; i++) {
        	ColorSetting colorSetting = fColorSettings[i];
        	if (colorSetting.getFilter() != null && colorSetting.getFilter().matches(event)) {
        		return i;
        	}
        }
        return PRIORITY_NONE;
	}

	/**
	 * Returns the color settings based the priority.
	 *
	 * @param priority A priority (index) of color settings
	 * @return the color settings defined for the priority else default color settings
	 */
	public static ColorSetting getColorSetting(int priority) {
		if (priority < fColorSettings.length) {
			return fColorSettings[priority];
		}
		return DEFAULT_COLOR_SETTING;
	}

	/**
	 * Adds a color settings listener.
	 *
	 * @param listener A listener to add.
	 */
	public static void addColorSettingsListener(IColorSettingsListener listener) {
		if (! fListeners.contains(listener)) {
			fListeners.add(listener);
		}
	}

	/**
	 * Removes a color settings listener.
	 *
	 * @param listener A listener to remove.
	 */
	public static void removeColorSettingsListener(IColorSettingsListener listener) {
		fListeners.remove(listener);
	}

	// Notify listeners
	private static void fireColorSettingsChanged() {
		for (IColorSettingsListener listener : fListeners) {
			listener.colorSettingsChanged(fColorSettings);
		}
	}
}
