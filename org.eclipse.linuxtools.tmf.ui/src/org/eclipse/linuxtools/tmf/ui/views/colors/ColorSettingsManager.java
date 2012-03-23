/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.colors;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.linuxtools.internal.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.internal.tmf.ui.viewers.timeAnalysis.widgets.TraceColorScheme;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

public class ColorSettingsManager {

	private static final String COLOR_SETTINGS_FILE_NAME = "color_settings.xml"; //$NON-NLS-1$
	private static final String COLOR_SETTINGS_PATH_NAME =
        TmfUiPlugin.getDefault().getStateLocation().addTrailingSeparator().append(COLOR_SETTINGS_FILE_NAME).toString();
	private static final ColorSetting DEFAULT_COLOR_SETTING = new ColorSetting(
			Display.getDefault().getSystemColor(SWT.COLOR_LIST_FOREGROUND).getRGB(),
			Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB(),
			TraceColorScheme.BLACK_STATE,
			null);
	public static final int PRIORITY_NONE = Integer.MAX_VALUE;
	
	private static ColorSetting[] fColorSettings = ColorSettingsXML.load(COLOR_SETTINGS_PATH_NAME);
	private static ArrayList<IColorSettingsListener> fListeners = new ArrayList<IColorSettingsListener>();
	
	public static ColorSetting[] getColorSettings() {
		return (fColorSettings != null) ? Arrays.copyOf(fColorSettings, fColorSettings.length) : null;
	}
	
	public static void setColorSettings(ColorSetting[] colorSettings) {
		fColorSettings = (colorSettings != null) ? Arrays.copyOf(colorSettings, colorSettings.length) : null;
		ColorSettingsXML.save(COLOR_SETTINGS_PATH_NAME, fColorSettings);
		fireColorSettingsChanged();
	}
	
	public static ColorSetting getColorSetting(ITmfEvent event) {
        for (int i = 0; i < fColorSettings.length; i++) {
        	ColorSetting colorSetting = fColorSettings[i];
        	if (colorSetting.getFilter() != null && colorSetting.getFilter().matches(event)) {
        		return colorSetting;
        	}
        }
        return DEFAULT_COLOR_SETTING;
	}
	
	public static int getColorSettingPriority(ITmfEvent event) {
        for (int i = 0; i < fColorSettings.length; i++) {
        	ColorSetting colorSetting = fColorSettings[i];
        	if (colorSetting.getFilter() != null && colorSetting.getFilter().matches(event)) {
        		return i;
        	}
        }
        return PRIORITY_NONE;
	}

	public static ColorSetting getColorSetting(int priority) {
		if (priority < fColorSettings.length) {
			return fColorSettings[priority];
		}
		return DEFAULT_COLOR_SETTING; 
	}

	public static void addColorSettingsListener(IColorSettingsListener listener) {
		if (! fListeners.contains(listener)) {
			fListeners.add(listener);
		}
	}
	
	public static void removeColorSettingsListener(IColorSettingsListener listener) {
		fListeners.remove(listener);
	}
	
	private static void fireColorSettingsChanged() {
		for (IColorSettingsListener listener : fListeners) {
			listener.colorSettingsChanged(fColorSettings);
		}
	}
}
