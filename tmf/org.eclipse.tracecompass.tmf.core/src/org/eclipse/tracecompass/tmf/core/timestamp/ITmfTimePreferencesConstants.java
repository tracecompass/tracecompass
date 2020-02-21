/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.timestamp;

/**
 * @noimplement This interface is not intended to be implemented by clients.
 */
@SuppressWarnings({ "javadoc", "nls" })
public interface ITmfTimePreferencesConstants {
    String TIME_FORMAT_PREF = "org.eclipse.linuxtools.tmf.core.prefs.time.format";
    String DEFAULT_TIME_PATTERN = "HH:mm:ss.SSS SSS SSS";
    String DATIME = TIME_FORMAT_PREF + ".datime";
    String SUBSEC = TIME_FORMAT_PREF + ".subsec";
    String TIME_ZONE = TIME_FORMAT_PREF + ".timezone";
    String DATE_DELIMITER = TIME_FORMAT_PREF + ".date.delimiter";
    String TIME_DELIMITER = TIME_FORMAT_PREF + ".time.delimiter";
    String SSEC_DELIMITER = TIME_FORMAT_PREF + ".ssec.delimiter";
    String DATE_YEAR_FMT = "yyyy-MM-dd HH:mm:ss";
    String DATE_YEAR2_FMT = "yy-MM-dd HH:mm:ss";
    String DATE_MONTH_FMT = "MM-dd HH:mm:ss";
    String DATE_DAY_FMT = "dd HH:mm:ss";
    String DATE_JDAY_FMT = "DDD HH:mm:ss";
    String DATE_NO_FMT = "HH:mm:ss";
    String TIME_HOUR_FMT = "HH:mm:ss";
    String TIME_MINUTE_FMT = "mm:ss";
    String TIME_SECOND_FMT = "ss";
    String TIME_ELAPSED_FMT = "TTT";
    String TIME_NO_FMT = "";
    String SUBSEC_MILLI_FMT = "SSS";
    String SUBSEC_MICRO_FMT = "SSS SSS";
    String SUBSEC_NANO_FMT = "SSS SSS SSS";
    String SUBSEC_NO_FMT = "";
    String DELIMITER_NONE = "";
    String DELIMITER_SPACE = " ";
    String DELIMITER_PERIOD = ".";
    String DELIMITER_COMMA = ",";
    String DELIMITER_DASH = "-";
    String DELIMITER_UNDERLINE = "_";
    String DELIMITER_COLON = ":";
    String DELIMITER_SEMICOLON = ";";
    String DELIMITER_SLASH = "/";
    String DELIMITER_DQUOT = "\"";
    String DELIMITER_QUOTE = "''";
    String LOCALE = TIME_FORMAT_PREF + ".locale";
}
