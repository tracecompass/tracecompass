/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Francois Chouinard - Initial API and implementation
 *     Marc-Andre Laperle - Add time zone preference
 *     Patrick Tasse - Updated for negative value formatting
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.timestamp;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A formatting and parsing facility that can handle timestamps that span the
 * epoch with a precision down to the nanosecond. It can be understood as a
 * simplified and more constrained version of SimpleDateFormat as it limits the
 * number of allowed pattern characters and the acceptable timestamp formats.
 * <p>
 * The timestamp representation is broken down into a number of optional
 * components that can be assembled into a fairly simple way.
 *
 * <h4>Date Pattern</h4>
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0 >
 *     <tr bgcolor="#ccccff">
 *         <th align=left>Format
 *         <th align=left>Description
 *         <th align=left>Value Range
 *         <th align=left>Example
 *     <tr>
 *         <td><code>yyyy</code>
 *         <td>Year
 *         <td><code>1970-...</code>
 *         <td><code>2012</code>
 *     <tr bgcolor="#eeeeff">
 *         <td><code>MM</code>
 *         <td>Month in year
 *         <td><code>01-12</code>
 *         <td><code>09</code>
 *     <tr>
 *         <td><code>dd</code>
 *         <td>Day in month
 *         <td><code>01-31</code>
 *         <td><code>22</code>
 * </table>
 * </blockquote>
 *
 * <h4>Time Pattern</h4>
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0 >
 *     <tr bgcolor="#ccccff">
 *         <th align=left>Format
 *         <th align=left>Description
 *         <th align=left>Value Range
 *         <th align=left>Example
 *     <tr>
 *         <td><code>HH</code>
 *         <td>Hour in day
 *         <td><code>00-23</code>
 *         <td><code>07</code>
 *     <tr bgcolor="#eeeeff">
 *         <td><code>mm</code>
 *         <td>Minute in hour
 *         <td><code>00-59</code>
 *         <td><code>35</code>
 *     <tr>
 *         <td><code>ss</code>
 *         <td>Second in minute
 *         <td><code>00-59</code>
 *         <td><code>41</code>
 *     <tr bgcolor="#eeeeff">
 *         <td><code>T</code>
 *         <td>The seconds since the epoch
 *         <td><code>0-...</code>
 *         <td><code>1332170682</code>
 * </table>
 * </blockquote>
 *
 * <h4>Sub-Seconds Pattern</h4>
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0 >
 *     <tr bgcolor="#ccccff">
 *         <th align=left>Format
 *         <th align=left>Description
 *         <th align=left>Value Range
 *         <th align=left>Example
 *     <tr>
 *         <td><code>SSS</code>
 *         <td>Millisecond in second
 *         <td><code>000-999</code>
 *         <td><code>123</code>
 *     <tr bgcolor="#eeeeff">
 *         <td><code>CCC</code>
 *         <td>Microseconds in ms
 *         <td><code>000-999</code>
 *         <td><code>456</code>
 *     <tr>
 *         <td><code>NNN</code>
 *         <td>Nanoseconds in &#181s
 *         <td><code>000-999</code>
 *         <td><code>789</code>
 * </table>
 * </blockquote>
 *
 * <strong>Note: </strong>If "T" is used, no other Date or Time pattern
 * can be used. Also, "T" should be used for time intervals.
 * <p>
 * <strong>Note: </strong>When parsing, the Sub-Seconds sub-field digits can be
 * separated by a single, optional character delimiter. However, the delimiter
 * between the Date/Time pattern and the Sub-Seconds pattern is mandatory (if
 * there is a fractional part) and must be a "." (period). The Date and Time
 * patterns must not contain any "." (period).
 * <p>
 * The recognized Sub-Seconds delimiters are:
 * <ul>
 * <li>Space ("<code> </code>")
 * <li>Period (<code>".</code>")
 * <li>Comma ("<code>,</code>")
 * <li>Dash ("<code>-</code>")
 * <li>Underline ("<code>_</code>")
 * <li>Colon ("<code>:</code>")
 * <li>Semicolon ("<code>;</code>")
 * <li>Slash ("<code>/</code>")
 * <li>Double-quote ("<code>"</code>")
 * </ul>
 *
 * <h4>Examples</h4>
 * The following examples show how timestamp patterns are interpreted in
 * the U.S. locale. The given timestamp is 1332170682539677389L, the number
 * of nanoseconds since 1970/01/01.
 *
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0>
 *     <tr bgcolor="#ccccff">
 *         <th align=left>Date and Time Pattern
 *         <th align=left>Result
 *     <tr>
 *         <td><code>"yyyy-MM-dd HH:mm:ss.SSS.CCC.NNN"</code>
 *         <td><code>2012-03-19 11:24:42.539.677.389</code>
 *     <tr bgcolor="#eeeeff">
 *         <td><code>"yyyy-MM-dd HH:mm:ss.SSS.CCC"</code>
 *         <td><code>2012-03-19 11:24:42.539.677</code>
 *     <tr>
 *         <td><code>"yyyy-D HH:mm:ss.SSS.CCC"</code>
 *         <td><code>2012-79 11:24:42.539.677</code>
 *     <tr bgcolor="#eeeeff">
 *         <td><code>"ss.SSSCCCNNN"</code>
 *         <td><code>42.539677389</code>
 *     <tr>
 *         <td><code>"T.SSS CCC NNN"</code>
 *         <td><code>1332170682.539 677 389</code>
 *     <tr bgcolor="#eeeeff">
 *         <td><code>"T"</code>
 *         <td><code>1332170682</code>
 * </table>
 * </blockquote>
 * <p>
 * @version 1.0
 * @since 2.0
 * @author Francois Chouinard
 */
public class TmfTimestampFormat extends SimpleDateFormat {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * This class' serialization ID
     */
    private static final long serialVersionUID = 2835829763122454020L;

    /**
     * The default timestamp pattern
     */
    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss.SSS CCC NNN"; //$NON-NLS-1$

    /**
     * The default interval pattern
     */
    public static final String DEFAULT_INTERVAL_PATTERN = "TTT.SSS CCC NNN"; //$NON-NLS-1$

    // Fractions of seconds supported patterns
    private static final String DOT_RE = "\\.";                 //$NON-NLS-1$
    private static final String SEP_RE = "[ \\.,\\-_:;/\\\"]?"; //$NON-NLS-1$
    private static final String DGTS_3_RE  = "(\\d{3})";        //$NON-NLS-1$
    private static final String DGTS_13_RE = "(\\d{1,3})";      //$NON-NLS-1$

    private static final String MILLISEC_RE = DOT_RE + DGTS_13_RE;
    private static final String MICROSEC_RE = DOT_RE + DGTS_3_RE + SEP_RE + DGTS_13_RE;
    private static final String NANOSEC_RE  = DOT_RE + DGTS_3_RE + SEP_RE + DGTS_3_RE + SEP_RE + DGTS_13_RE;

    private static final Pattern MILLISEC_PAT = Pattern.compile(MILLISEC_RE);
    private static final Pattern MICROSEC_PAT = Pattern.compile(MICROSEC_RE);
    private static final Pattern NANOSEC_PAT  = Pattern.compile(NANOSEC_RE);

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The default timestamp pattern
    private static TmfTimestampFormat fDefaultTimeFormat = null;

    // The default time interval format
    private static TmfTimestampFormat fDefaultIntervalFormat = null;

    // The timestamp pattern
    private String fPattern;

    // The sub-seconds pattern
    private String fSubSecPattern;

    // The list of supplementary patterns
    private List<String> fSupplPatterns = new ArrayList<>();

    /**
     * The supplementary pattern letters. Can be redefined by sub-classes
     * to either override existing letters or augment the letter set.
     * If so, the format() method must provide the (re-)implementation of the
     * pattern.
     */
    protected String fSupplPatternLetters = "TSCN"; //$NON-NLS-1$

    /*
     * The bracketing symbols used to mitigate the risk of a format string
     * that contains escaped sequences that would conflict with our format
     * extension.
     */
    /** The open bracket symbol */
    protected String fOpenBracket   = "[&"; //$NON-NLS-1$

    /** The closing bracket symbol */
    protected String fCloseBracket  = "&]"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The default constructor (uses the default pattern)
     */
    public TmfTimestampFormat() {
        this(TmfTimePreferences.getInstance().getTimePattern());
    }

    /**
     * The normal constructor
     *
     * @param pattern the format pattern
     */
    public TmfTimestampFormat(String pattern) {
        applyPattern(pattern);
    }

    /**
     * The full constructor
     *
     * @param pattern the format pattern
     * @param timeZone the time zone
     * @since 2.1
     */
    public TmfTimestampFormat(String pattern, TimeZone timeZone) {
        setTimeZone(timeZone);
        applyPattern(pattern);
    }

    /**
     * The copy constructor
     *
     * @param other the other format pattern
     */
    public TmfTimestampFormat(TmfTimestampFormat other) {
        this(other.fPattern);
    }

    // ------------------------------------------------------------------------
    // Getters/setters
    // ------------------------------------------------------------------------

    /**
     * @since 2.1
     */
    public static void updateDefaultFormats() {
        fDefaultTimeFormat = new TmfTimestampFormat(TmfTimePreferences.getInstance().getTimePattern(), TmfTimePreferences.getInstance().getTimeZone());
        fDefaultIntervalFormat = new TmfTimestampFormat(TmfTimePreferences.getInstance().getIntervalPattern());
    }

    /**
     * @param pattern the new default time pattern
     * @deprecated The default time pattern depends on the preferences, see
     *             {@link TmfTimePreferences}. To change the default time
     *             pattern, modify the preferences and call {@link #updateDefaultFormats()}
     */
    @Deprecated
    public static void setDefaultTimeFormat(final String pattern) {
    }

    /**
     * @return the default time format pattern
     */
    public static TmfTimestampFormat getDefaulTimeFormat() {
        if (fDefaultTimeFormat == null) {
            fDefaultTimeFormat = new TmfTimestampFormat(TmfTimePreferences.getInstance().getTimePattern(), TmfTimePreferences.getInstance().getTimeZone());
        }
        return fDefaultTimeFormat;
    }

    /**
     * @param pattern the new default interval pattern
     * @deprecated The default interval format pattern depends on the
     *             preferences, see {@link TmfTimePreferences}. To change the
     *             default time pattern, modify the preferences and call
     *             {@link #updateDefaultFormats()}
     */
    @Deprecated
    public static void setDefaultIntervalFormat(final String pattern) {
    }

    /**
     * @return the default interval format pattern
     */
    public static TmfTimestampFormat getDefaulIntervalFormat() {
        if (fDefaultIntervalFormat == null) {
            fDefaultIntervalFormat = new TmfTimestampFormat(TmfTimePreferences.getInstance().getIntervalPattern());
        }
        return fDefaultIntervalFormat;
    }

    @Override
    public void applyPattern(String pattern) {
        fPattern = pattern;
        int dot = pattern.indexOf('.');
        if (dot == -1) {
            dot = pattern.length();
        }
        // The super pattern is the date/time pattern, quoted and bracketed
        super.applyPattern(quoteSpecificTags(pattern.substring(0, dot), true));
        // The sub-seconds pattern is bracketed (but not quoted)
        fSubSecPattern = quoteSpecificTags(pattern.substring(dot), false);
    }

    @Override
    public String toPattern() {
        return fPattern;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Format the timestamp according to its pattern.
     *
     * @param value the timestamp value to format (in ns)
     * @return the formatted timestamp
     */
    public synchronized String format(long value) {

        // Split the timestamp value into its sub-components
        long date = value / 1000000; // milliseconds since January 1, 1970, 00:00:00 GMT
        long sec = value / 1000000000;    // seconds since January 1, 1970, 00:00:00 GMT
        long ms  = Math.abs((value % 1000000000) / 1000000); // milliseconds
        long cs  = Math.abs((value % 1000000)    / 1000);    // microseconds
        long ns  = Math.abs(value % 1000);                   // nanoseconds

        // Adjust for negative value when formatted as a date
        if (value < 0 && ms + cs + ns > 0 && !super.toPattern().contains(fOpenBracket + "T")) { //$NON-NLS-1$
            date -= 1;
            long nanosec = 1000000000 - (1000000 * ms + 1000 * cs + ns);
            ms = nanosec / 1000000;
            cs = (nanosec % 1000000) / 1000;
            ns = nanosec % 1000;
        }

        // Let the base class format the date/time pattern
        StringBuffer result = new StringBuffer(super.format(date));
        // Append the sub-second pattern
        result.append(fSubSecPattern);

        // Fill in our extensions
        for (String pattern : fSupplPatterns) {
            int length = pattern.length();

            // Prepare the format buffer
            StringBuffer fmt = new StringBuffer(length);
            for (int i = 0; i < length; i++) {
                fmt.append("0"); //$NON-NLS-1$
            }
            DecimalFormat dfmt = new DecimalFormat(fmt.toString());
            String fmtVal = ""; //$NON-NLS-1$;

            // Format the proper value as per the pattern
            switch (pattern.charAt(0)) {
                case 'T':
                    if (value < 0 && sec == 0) {
                        result.insert(0, '-');
                    }
                    fmtVal = dfmt.format(sec);
                    break;
                case 'S':
                    fmtVal = dfmt.format(ms);
                    break;
                case 'C':
                    fmtVal = dfmt.format(cs);
                    break;
                case 'N':
                    fmtVal = dfmt.format(ns);
                    break;
                default:
                    break;
            }

            // Substitute the placeholder pattern with the formatted value
            String ph = new StringBuffer(fOpenBracket + pattern + fCloseBracket).toString();
            int loc = result.indexOf(ph);
            result.replace(loc, loc + length + fOpenBracket.length() + fCloseBracket.length(), fmtVal);
        }

        return result.toString();
    }

    /**
     * Parse a string according to the format pattern
     *
     * @param string the source string
     * @param ref the reference (base) time (in ns)
     * @return the parsed value (in ns)
     * @throws ParseException if the string has an invalid format
     */
    public synchronized long parseValue(final String string, final long ref) throws ParseException {

        // Trivial case
        if (string == null || string.length() == 0) {
            return 0;
        }

        // The timestamp sub-components
        long seconds  = -1;
        long millisec =  0;
        long microsec =  0;
        long nanosec  =  0;

        int dot = string.indexOf('.');
        if (dot == -1) {
            dot = string.length();
        }

        // Fill in our extensions
        for (String pattern : fSupplPatterns) {
            Matcher matcher;

            // Extract the substring corresponding to the extra pattern letters
            switch (pattern.charAt(0)) {
                case 'T':
                    // Remove everything up to the first "." and  compute the
                    // number of seconds since the epoch. If there is no period,
                    // assume an integer value and return immediately
                    if (dot < 0) {
                        return new DecimalFormat("0").parse(string).longValue() * 1000000000; //$NON-NLS-1$
                    } else if (dot == 0) {
                        seconds = 0;
                    } else {
                        seconds = new DecimalFormat("0").parse(string.substring(0, dot)).longValue(); //$NON-NLS-1$
                    }
                    break;
                case 'S':
                    matcher = MILLISEC_PAT.matcher(string.substring(dot));
                    if (matcher.find()) {
                        millisec = new Long(matcher.group(1));
                        for (int l = matcher.group(1).length(); l < 3; l++) {
                            millisec *= 10;
                        }
                    }
                    break;
                case 'C':
                    matcher = MICROSEC_PAT.matcher(string.substring(dot));
                    if (matcher.find()) {
                        microsec = new Long(matcher.group(2));
                        for (int l = matcher.group(2).length(); l < 3; l++) {
                            microsec *= 10;
                        }
                    }
                    break;
                case 'N':
                    matcher = NANOSEC_PAT.matcher(string.substring(dot));
                    if (matcher.find()) {
                        nanosec = new Long(matcher.group(3));
                        for (int l = matcher.group(3).length(); l < 3; l++) {
                            nanosec *= 10;
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        // If there was no "T" (thus not an interval), parse as a date
        if (seconds == -1) {
            Date baseDate = super.parse(string.substring(0, dot));

            Calendar refTime = Calendar.getInstance(getTimeZone());
            refTime.setTimeInMillis(ref / 1000000);
            Calendar newTime = Calendar.getInstance(getTimeZone());
            newTime.setTimeInMillis(baseDate.getTime());

            int[] fields = new int[] { Calendar.YEAR, Calendar.MONTH, Calendar.DATE, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND };
            for (int field : fields) {
                int value = newTime.get(field);
                // Do some adjustments...
                if (field == Calendar.YEAR) {
                    value -= 1970;
                } else if (field == Calendar.DATE) {
                    value -= 1;
                }
                // ... and fill-in the empty fields
                if (value == 0) {
                    newTime.set(field, refTime.get(field));
                } else {
                    break; // Get out as soon as we have a significant value
                }
            }
            seconds = newTime.getTimeInMillis() / 1000;
        }

        // Compute the value in ns
        return seconds * 1000000000 +  millisec * 1000000 +  microsec * 1000 +  nanosec;
    }

    /**
     * Parse a string according to the format pattern
     *
     * @param string the source string
     * @return the parsed value (in ns)
     * @throws ParseException if the string has an invalid format
     */
    public long parseValue(final String string) throws ParseException {
        long result = parseValue(string, 0);
        return result;

    }

    // ------------------------------------------------------------------------
    // Helper functions
    // ------------------------------------------------------------------------

    /**
     * Copy the pattern but quote (bracket with "[&" and "&]") the
     * TmfTimestampFormat specific tags. Optionally surround tags with single
     * quotes so these fields are treated as comments by the base class.
     *
     * It also keeps track of the corresponding quoted fields so they can be
     * properly populated later on (by format()).
     *
     * @param pattern the 'extended' pattern
     * @param addQuotes true to add single quotes around tags
     * @return the quoted and bracketed pattern
     */
    private String quoteSpecificTags(final String pattern, boolean addQuotes) {

        StringBuffer result = new StringBuffer();

        int length = pattern.length();
        boolean inQuote = false;

        for (int i = 0; i < length; i++) {
            char c = pattern.charAt(i);
            result.append(c);
            if (c == '\'') {
                // '' is treated as a single quote regardless of being
                // in a quoted section.
                if ((i + 1) < length) {
                    c = pattern.charAt(i + 1);
                    if (c == '\'') {
                        i++;
                        result.append(c);
                        continue;
                    }
                }
                inQuote = !inQuote;
                continue;
            }
            if (!inQuote) {
                if (fSupplPatternLetters.indexOf(c) != -1) {
                    StringBuilder pat = new StringBuilder();
                    pat.append(c);
                    if (addQuotes) {
                        result.insert(result.length() - 1, "'"); //$NON-NLS-1$
                    }
                    result.insert(result.length() - 1, fOpenBracket);
                    while ((i + 1) < length && pattern.charAt(i + 1) == c) {
                        result.append(c);
                        pat.append(c);
                        i++;
                    }
                    result.append(fCloseBracket);
                    if (addQuotes) {
                        result.append("'"); //$NON-NLS-1$
                    }
                    fSupplPatterns.add(pat.toString());
                }
            }
        }
        return result.toString();
    }

}
