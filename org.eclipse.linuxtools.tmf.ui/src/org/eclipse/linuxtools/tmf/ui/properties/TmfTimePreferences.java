/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.properties;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestampFormat;

/**
 * TMF Time format preferences
 *
 * @author Francois Chouinard
 * @version 1.0
 * @since 2.0
 */
@SuppressWarnings("javadoc")
public class TmfTimePreferences {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss.SSS_CCC_NNN"; //$NON-NLS-1$

    static final String TIME_FORMAT_PREF = "org.eclipse.linuxtools.tmf.ui.prefs.time.format"; //$NON-NLS-1$
    static final String DATIME = TIME_FORMAT_PREF + ".datime";   //$NON-NLS-1$
    static final String SUBSEC = TIME_FORMAT_PREF + ".subsec";   //$NON-NLS-1$

    static final String DATE_DELIMITER = TIME_FORMAT_PREF + ".date.delimiter";   //$NON-NLS-1$
    static final String TIME_DELIMITER = TIME_FORMAT_PREF + ".time.delimiter";   //$NON-NLS-1$
    static final String SSEC_DELIMITER = TIME_FORMAT_PREF + ".ssec.delimiter";   //$NON-NLS-1$

    static final String DATE_YEAR_FMT  = "yyyy-MM-dd HH:mm:ss";  //$NON-NLS-1$
    static final String DATE_YEAR2_FMT = "yy-MM-dd HH:mm:ss";    //$NON-NLS-1$
    static final String DATE_MONTH_FMT = "MM-dd HH:mm:ss";       //$NON-NLS-1$
    static final String DATE_DAY_FMT   = "dd HH:mm:ss";          //$NON-NLS-1$
    static final String DATE_JDAY_FMT  = "DDD HH:mm:ss";         //$NON-NLS-1$
    static final String DATE_NO_FMT    = "HH:mm:ss";             //$NON-NLS-1$

    static final String TIME_HOUR_FMT    = "HH:mm:ss";           //$NON-NLS-1$
    static final String TIME_MINUTE_FMT  = "mm:ss";              //$NON-NLS-1$
    static final String TIME_SECOND_FMT  = "ss";                 //$NON-NLS-1$
    static final String TIME_ELAPSED_FMT = "TTT";                //$NON-NLS-1$
    static final String TIME_NO_FMT      = "";                   //$NON-NLS-1$

    static final String SUBSEC_MILLI_FMT = "SSS";                //$NON-NLS-1$
    static final String SUBSEC_MICRO_FMT = "SSS CCC";            //$NON-NLS-1$
    static final String SUBSEC_NANO_FMT  = "SSS CCC NNN";        //$NON-NLS-1$
    static final String SUBSEC_NO_FMT    = "";                   //$NON-NLS-1$

    static final String DELIMITER_NONE      = "";    //$NON-NLS-1$
    static final String DELIMITER_SPACE     = " ";   //$NON-NLS-1$
    static final String DELIMITER_PERIOD    = ".";   //$NON-NLS-1$
    static final String DELIMITER_COMMA     = ",";   //$NON-NLS-1$
    static final String DELIMITER_DASH      = "-";   //$NON-NLS-1$
    static final String DELIMITER_UNDERLINE = "_";   //$NON-NLS-1$
    static final String DELIMITER_COLON     = ":";   //$NON-NLS-1$
    static final String DELIMITER_SEMICOLON = ";";   //$NON-NLS-1$
    static final String DELIMITER_SLASH     = "/";   //$NON-NLS-1$
    static final String DELIMITER_DQUOT     = "\"";  //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static TmfTimePreferences fPreferences;

    private static IPreferenceStore fPreferenceStore;
    private static String fTimestampPattern;
    private static String fIntervalPattern;

    private String fDatimeFormat;
    private String fDateFormat;
    private String fTimeFormat;
    private String fSSecFormat;

    private String fDateFieldSep = "-"; //$NON-NLS-1$
    private String fTimeFieldSep = ":"; //$NON-NLS-1$
    private String fSSecFieldSep = " "; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public static void init() {
        fPreferenceStore = Activator.getDefault().getPreferenceStore();
        fPreferenceStore.setDefault(TmfTimePreferences.DATIME, TIME_HOUR_FMT);
        fPreferenceStore.setDefault(TmfTimePreferences.SUBSEC, SUBSEC_NANO_FMT);
        fPreferenceStore.setDefault(TmfTimePreferences.DATE_DELIMITER, DELIMITER_DASH);
        fPreferenceStore.setDefault(TmfTimePreferences.TIME_DELIMITER, DELIMITER_COLON);
        fPreferenceStore.setDefault(TmfTimePreferences.SSEC_DELIMITER, DELIMITER_SPACE);

        // Create the singleton and initialize format preferences
        getInstance();
    }

    public static synchronized IPreferenceStore getPreferenceStore() {
        if (fPreferenceStore == null) {
            init();
        }
        return fPreferenceStore;
    }

    public static synchronized TmfTimePreferences getInstance() {
        if (fPreferences == null) {
            fPreferences = new TmfTimePreferences();
        }
        return fPreferences;
    }

    /**
     * Local constructor
     */
    private TmfTimePreferences() {
        initPatterns();
        setTimePattern(fTimestampPattern);
    }

    // ------------------------------------------------------------------------
    // Getters/Setters
    // ------------------------------------------------------------------------

    /**
     * @return the timestamp pattern
     */
    public static String getTimePattern() {
        return fTimestampPattern;
    }

    /**
     * Sets the timestamp pattern and updates TmfTimestampFormat
     *
     * @param timePattern the new timestamp pattern
     */
    static void setTimePattern(String timePattern) {
        fTimestampPattern = timePattern;
        TmfTimestampFormat.setDefaultTimeFormat(fTimestampPattern);
        TmfTimestampFormat.setDefaultIntervalFormat(fIntervalPattern);
    }

    /**
     * Update the Date field separator
     * @param pattern the Date field separator
     */
    void setDateFieldSep(String pattern) {
        fDateFieldSep = pattern;
    }

    /**
     * Update the Time field separator
     * @param pattern the Time field separator
     */
    void setTimeFieldSep(String pattern) {
        fTimeFieldSep = pattern;
    }

    /**
     * Update the Subseconds field separator
     * @param pattern the Subseconds field separator
     */
    void setSSecFieldSep(String pattern) {
        fSSecFieldSep = pattern;
    }

    /**
     * Update the Date/Time format
     * @param pattern the Date/Time format
     */
    void setDateTimeFormat(String pattern) {
        fDatimeFormat = pattern;
        if (fDatimeFormat == null) {
            fDatimeFormat = DEFAULT_TIME_PATTERN;
        }
        int index = fDatimeFormat.indexOf(' ');
        if (index != -1) {
            fDateFormat = fDatimeFormat.substring(0, fDatimeFormat.indexOf(' ') + 1);
            fTimeFormat = fDatimeFormat.substring(fDateFormat.length());
        } else {
            fDateFormat = ""; //$NON-NLS-1$
            fTimeFormat = fDatimeFormat;
        }
    }

    /**
     * Update the Subseconds format
     * @param pattern the Subseconds format
     */
    void setSSecFormat(String pattern) {
        fSSecFormat = pattern;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    void initPatterns() {
        setDateTimeFormat(fPreferenceStore.getString(DATIME));
        fSSecFormat   = fPreferenceStore.getString(SUBSEC);
        fDateFieldSep = fPreferenceStore.getString(DATE_DELIMITER);
        fTimeFieldSep = fPreferenceStore.getString(TIME_DELIMITER);
        fSSecFieldSep = fPreferenceStore.getString(SSEC_DELIMITER);
        updatePatterns();
    }

    void updatePatterns() {
        String dateFmt = fDateFormat.replaceAll("-", fDateFieldSep); //$NON-NLS-1$
        String timeFmt = fTimeFormat.replaceAll(":", fTimeFieldSep); //$NON-NLS-1$
        String ssecFmt = fSSecFormat.replaceAll(" ", fSSecFieldSep); //$NON-NLS-1$

        fTimestampPattern = dateFmt + timeFmt + "." + ssecFmt; //$NON-NLS-1$
        fIntervalPattern = "TTT." + ssecFmt; //$NON-NLS-1$
    }

    void setDefaults() {
        setDateTimeFormat(TmfTimePreferences.TIME_HOUR_FMT);
        setSSecFormat(TmfTimePreferences.SUBSEC_NANO_FMT);
        setDateFieldSep(TmfTimePreferences.DELIMITER_DASH);
        setTimeFieldSep(TmfTimePreferences.DELIMITER_COLON);
        setSSecFieldSep(TmfTimePreferences.DELIMITER_SPACE);
        updatePatterns();
    }

}
