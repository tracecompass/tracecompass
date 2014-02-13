/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.timestamp;

/**
 * @since 2.1
 */
@SuppressWarnings({ "javadoc", "nls" })
public interface ITmfTimePreferencesConstants {
    public static final String TIME_FORMAT_PREF = "org.eclipse.linuxtools.tmf.core.prefs.time.format";
    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss.SSS SSS SSS";
    public static final String DATIME = TIME_FORMAT_PREF + ".datime";
    public static final String SUBSEC = TIME_FORMAT_PREF + ".subsec";
    public static final String TIME_ZONE = TIME_FORMAT_PREF + ".timezone";
    public static final String DATE_DELIMITER = TIME_FORMAT_PREF + ".date.delimiter";
    public static final String TIME_DELIMITER = TIME_FORMAT_PREF + ".time.delimiter";
    public static final String SSEC_DELIMITER = TIME_FORMAT_PREF + ".ssec.delimiter";
    public static final String DATE_YEAR_FMT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_YEAR2_FMT = "yy-MM-dd HH:mm:ss";
    public static final String DATE_MONTH_FMT = "MM-dd HH:mm:ss";
    public static final String DATE_DAY_FMT = "dd HH:mm:ss";
    public static final String DATE_JDAY_FMT = "DDD HH:mm:ss";
    public static final String DATE_NO_FMT = "HH:mm:ss";
    public static final String TIME_HOUR_FMT = "HH:mm:ss";
    public static final String TIME_MINUTE_FMT = "mm:ss";
    public static final String TIME_SECOND_FMT = "ss";
    public static final String TIME_ELAPSED_FMT = "TTT";
    public static final String TIME_NO_FMT = "";
    public static final String SUBSEC_MILLI_FMT = "SSS";
    public static final String SUBSEC_MICRO_FMT = "SSS SSS";
    public static final String SUBSEC_NANO_FMT = "SSS SSS SSS";
    public static final String SUBSEC_NO_FMT = "";
    public static final String DELIMITER_NONE = "";
    public static final String DELIMITER_SPACE = " ";
    public static final String DELIMITER_PERIOD = ".";
    public static final String DELIMITER_COMMA = ",";
    public static final String DELIMITER_DASH = "-";
    public static final String DELIMITER_UNDERLINE = "_";
    public static final String DELIMITER_COLON = ":";
    public static final String DELIMITER_SEMICOLON = ";";
    public static final String DELIMITER_SLASH = "/";
    public static final String DELIMITER_DQUOT = "\"";
    /** @since 3.0 */
    public static final String DELIMITER_QUOTE = "''";
}
