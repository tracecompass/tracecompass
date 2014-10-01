/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Francois Chouinard - Initial API and implementation
 *     Marc-Andre Laperle - Add time zone preference
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.timestamp;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.linuxtools.internal.tmf.core.Activator;

/**
 * TMF Time format preferences
 *
 * @author Francois Chouinard
 * @version 1.0
 * @since 2.1
 */
public class TmfTimePreferences {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String DATIME_DEFAULT = ITmfTimePreferencesConstants.TIME_HOUR_FMT;
    private static final String SUBSEC_DEFAULT = ITmfTimePreferencesConstants.SUBSEC_NANO_FMT;
    private static final String DATE_DELIMITER_DEFAULT = ITmfTimePreferencesConstants.DELIMITER_DASH;
    private static final String TIME_DELIMITER_DEFAULT = ITmfTimePreferencesConstants.DELIMITER_COLON;
    private static final String SSEC_DELIMITER_DEFAULT = ITmfTimePreferencesConstants.DELIMITER_SPACE;
    private static final String TIME_ZONE_DEFAULT = TimeZone.getDefault().getID();
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static TmfTimePreferences fPreferences;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Initialize the default preferences and the singleton
     */
    public static void init() {
        IEclipsePreferences defaultPreferences = DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        defaultPreferences.put(ITmfTimePreferencesConstants.DATIME, DATIME_DEFAULT);
        defaultPreferences.put(ITmfTimePreferencesConstants.SUBSEC, SUBSEC_DEFAULT);
        defaultPreferences.put(ITmfTimePreferencesConstants.DATE_DELIMITER, DATE_DELIMITER_DEFAULT);
        defaultPreferences.put(ITmfTimePreferencesConstants.TIME_DELIMITER, TIME_DELIMITER_DEFAULT);
        defaultPreferences.put(ITmfTimePreferencesConstants.SSEC_DELIMITER, SSEC_DELIMITER_DEFAULT);
        defaultPreferences.put(ITmfTimePreferencesConstants.TIME_ZONE, TIME_ZONE_DEFAULT);

        // Create the singleton and update default formats
        getInstance();
    }

    /**
     * Get the TmfTimePreferences singleton
     *
     * @return The TmfTimePreferences instance
     */
    public static synchronized TmfTimePreferences getInstance() {
        if (fPreferences == null) {
            fPreferences = new TmfTimePreferences();
            TmfTimestampFormat.updateDefaultFormats();
        }
        return fPreferences;
    }

    /**
     * Local constructor
     */
    private TmfTimePreferences() {
    }

    // ------------------------------------------------------------------------
    // Getters/Setters
    // ------------------------------------------------------------------------

    /**
     * Return the timestamp pattern
     *
     * @return the timestamp pattern
     */
    public String getTimePattern() {
        return computeTimePattern(getPreferenceMap(false));
    }

    /**
     * Return the interval pattern
     *
     * @return the interval pattern
     */
    public String getIntervalPattern() {
        return computeIntervalPattern(getPreferenceMap(false));
    }

    /**
     * Get the time zone
     *
     * @return the time zone
     */
    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone(Platform.getPreferencesService().getString(Activator.PLUGIN_ID, ITmfTimePreferencesConstants.TIME_ZONE, TimeZone.getDefault().getID(), null));
    }

    /**
     * Get the locale
     *
     * @return the locale
     * @since 3.2
     */
    public Locale getLocale() {
        return Locale.forLanguageTag(Platform.getPreferencesService().getString(Activator.PLUGIN_ID, ITmfTimePreferencesConstants.LOCALE, Locale.getDefault().toLanguageTag(), null));
    }

    /**
     * Get the default preferences map
     *
     * @return a collection containing the default preferences
     */
    public Map<String, String> getDefaultPreferenceMap() {
        return getPreferenceMap(true);
    }

    /**
     * Get the current preferences map
     *
     * @return a collection containing the current preferences
     */
    public Map<String, String> getPreferenceMap() {
        return getPreferenceMap(false);
    }

    private static Map<String, String> getPreferenceMap(boolean defaultValues) {
        Map<String, String> prefsMap = new HashMap<>();
        IEclipsePreferences prefs = defaultValues ? DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID) : InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        prefToMap(prefs, prefsMap, ITmfTimePreferencesConstants.SUBSEC, SUBSEC_DEFAULT);
        prefToMap(prefs, prefsMap, ITmfTimePreferencesConstants.TIME_DELIMITER, TIME_DELIMITER_DEFAULT);
        prefToMap(prefs, prefsMap, ITmfTimePreferencesConstants.SSEC_DELIMITER, SSEC_DELIMITER_DEFAULT);
        prefToMap(prefs, prefsMap, ITmfTimePreferencesConstants.DATIME, DATIME_DEFAULT);
        prefToMap(prefs, prefsMap, ITmfTimePreferencesConstants.DATE_DELIMITER, DATE_DELIMITER_DEFAULT);
        return prefsMap;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    private static String computeIntervalPattern(Map<String, String> prefsMap) {
        String ssecFmt = computeSubSecFormat(prefsMap);
        return ITmfTimePreferencesConstants.TIME_ELAPSED_FMT + "." + ssecFmt; //$NON-NLS-1$
    }

    private static String computeSubSecFormat(Map<String, String> prefsMap) {
        String sSecFormat = prefsMap.get(ITmfTimePreferencesConstants.SUBSEC);
        String sSecFieldSep = prefsMap.get(ITmfTimePreferencesConstants.SSEC_DELIMITER);
        String ssecFmt = sSecFormat.replaceAll(" ", sSecFieldSep); //$NON-NLS-1$
        return ssecFmt;
    }

    private static void prefToMap(IEclipsePreferences node, Map<String, String> prefsMap, String key, String defaultValue) {
        prefsMap.put(key, node.get(key, defaultValue));
    }

    /**
     * Compute the time pattern with the collection of preferences
     *
     * @param prefsMap the preferences to apply when computing the time pattern
     * @return the time pattern resulting in applying the preferences
     */
    public String computeTimePattern(Map<String, String> prefsMap) {
        String dateTimeFormat = prefsMap.get(ITmfTimePreferencesConstants.DATIME);
        if (dateTimeFormat == null) {
            dateTimeFormat = ITmfTimePreferencesConstants.DEFAULT_TIME_PATTERN;
        }

        String dateFormat;
        String timeFormat;
        int index = dateTimeFormat.indexOf(' ');
        if (index != -1) {
            dateFormat = dateTimeFormat.substring(0, dateTimeFormat.indexOf(' ') + 1);
            timeFormat = dateTimeFormat.substring(dateFormat.length());
        } else {
            dateFormat = ""; //$NON-NLS-1$
            timeFormat = dateTimeFormat;
        }

        String dateFieldSep = prefsMap.get(ITmfTimePreferencesConstants.DATE_DELIMITER);
        String timeFieldSep = prefsMap.get(ITmfTimePreferencesConstants.TIME_DELIMITER);
        String dateFmt = dateFormat.replaceAll("-", dateFieldSep); //$NON-NLS-1$
        String timeFmt = timeFormat.replaceAll(":", timeFieldSep); //$NON-NLS-1$

        String ssecFmt = computeSubSecFormat(prefsMap);
        return dateFmt + timeFmt + (ssecFmt.equals(ITmfTimePreferencesConstants.SUBSEC_NO_FMT) ? "" : '.' + ssecFmt); //$NON-NLS-1$;
    }

}
