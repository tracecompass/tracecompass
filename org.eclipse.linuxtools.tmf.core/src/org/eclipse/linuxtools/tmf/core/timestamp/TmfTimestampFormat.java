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
 *     Patrick Tasse - Updated for negative value formatting and fraction of sec
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.timestamp;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A formatting and parsing facility that can handle timestamps that span the
 * epoch with a precision down to the nanosecond. It can be understood as an
 * extension of SimpleDateFormat that supports seconds since the epoch (Jan 1,
 * 1970, 00:00:00 GMT), additional sub-second patterns and optional delimiters.
 * <p>
 * The timestamp representation is broken down into a number of optional
 * components that can be assembled into a fairly simple way.
 *
 * <h4>Date and Time Patterns</h4>
 * All date and time pattern letters defined in {@link SimpleDateFormat} are
 * supported with the following exceptions:
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0 >
 *     <tr bgcolor="#ccccff">
 *         <th align=left>Format
 *         <th align=left>Description
 *         <th align=left>Value Range
 *         <th align=left>Example
 *     <tr bgcolor="#eeeeff">
 *         <td><code>T</code>
 *         <td>The seconds since the epoch
 *         <td><code>0-9223372036</code>
 *         <td><code>1332170682</code>
 *     <tr>
 *         <td><code>S</code>
 *         <td>Millisecond
 *         <td><code>N/A</code>
 *         <td><code>Not supported</code>
 *     <tr bgcolor="#eeeeff">
 *         <td><code>W</code>
 *         <td>Week in month
 *         <td><code>N/A</code>
 *         <td><code>Not supported</code>
 * </table>
 * </blockquote>
 * <p>
 * <strong>Note:</strong> When parsing, if "T" is used, no other Date and Time
 * pattern letter will be interpreted and the entire pre-delimiter input string
 * will be parsed as a number. Also, "T" should be used for time intervals.
 * <p>
 * <strong>Note:</strong> The decimal separator between the Date and Time
 * pattern and the Sub-Seconds pattern is mandatory (if there is a fractional
 * part) and must be one of the sub-second delimiters. Date and Time pattern
 * letters are not interpreted after the decimal separator.
 * <p>
 * <h4>Sub-Seconds Patterns</h4>
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0 >
 *     <tr bgcolor="#ccccff">
 *         <th align=left>Format
 *         <th align=left>Description
 *         <th align=left>Value Range
 *         <th align=left>Example
 *     <tr>
 *         <td><code>S</code>
 *         <td>Fraction of second
 *         <td><code>0-999999999</code>
 *         <td><code>123456789</code>
 *     <tr bgcolor="#eeeeff">
 *         <td><code>C</code>
 *         <td>Microseconds in ms
 *         <td><code>0-999</code>
 *         <td><code>456</code>
 *     <tr>
 *         <td><code>N</code>
 *         <td>Nanoseconds in &#181s
 *         <td><code>0-999</code>
 *         <td><code>789</code>
 * </table>
 * </blockquote>
 * <strong>Note:</strong> The fraction of second pattern can be split, in which
 * case parsing and formatting continues at the next digit. Digits beyond the
 * total number of pattern letters are ignored when parsing and truncated when
 * formatting.
 * <p>
 * <strong>Note:</strong> When parsing, "S", "C" and "N" are interchangeable
 * and are all handled as fraction of second ("S"). The use of "C" and "N" is
 * discouraged but is supported for backward compatibility.
 * <p>
 *
 * The recognized sub-second delimiters are:
 * <ul>
 * <li>Space ("<code> </code>")
 * <li>Period ("<code>.</code>")
 * <li>Comma ("<code>,</code>")
 * <li>Dash ("<code>-</code>")
 * <li>Underline ("<code>_</code>")
 * <li>Colon ("<code>:</code>")
 * <li>Semicolon ("<code>;</code>")
 * <li>Slash ("<code>/</code>")
 * <li>Single-quote ("<code>''</code>")
 * <li>Double-quote ("<code>"</code>")
 * </ul>
 * <p>
 * <strong>Note:</strong> When parsing, sub-second delimiters are optional if
 * unquoted. However, an extra delimiter or any other unexpected character in
 * the input string ends the parsing of digits. All other quoted or unquoted
 * characters in the sub-second pattern are matched against the input string.
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
 *         <td><code>"yyyy-MM-dd HH:mm:ss.SSS.SSS.SSS"</code>
 *         <td><code>2012-03-19 11:24:42.539.677.389</code>
 *     <tr bgcolor="#eeeeff">
 *         <td><code>"yyyy-MM-dd HH:mm:ss.SSS.SSS"</code>
 *         <td><code>2012-03-19 11:24:42.539.677</code>
 *     <tr>
 *         <td><code>"yyyy-D HH:mm:ss.SSS.SSS"</code>
 *         <td><code>2012-79 11:24:42.539.677</code>
 *     <tr bgcolor="#eeeeff">
 *         <td><code>"ss,SSSS"</code>
 *         <td><code>42,5397</code>
 *     <tr>
 *         <td><code>"T.SSS SSS SSS"</code>
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
    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss.SSS SSS SSS"; //$NON-NLS-1$

    /**
     * The default interval pattern
     */
    public static final String DEFAULT_INTERVAL_PATTERN = "TTT.SSS SSS SSS"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The default timestamp pattern
    private static TmfTimestampFormat fDefaultTimeFormat = null;

    // The default time interval format
    private static TmfTimestampFormat fDefaultIntervalFormat = null;

    // The timestamp pattern
    private String fPattern;

    // The index of the decimal separator in the pattern
    private int fPatternDecimalSeparatorIndex;

    // The decimal separator
    private char fDecimalSeparator = '\0';

    // The date and time pattern unquoted characters
    private String fDateTimePattern;

    // The sub-seconds pattern
    private String fSubSecPattern;

    // The list of supplementary patterns
    private List<String> fSupplPatterns = new ArrayList<>();

    // The locale
    private final Locale fLocale;

    /**
     * The supplementary pattern letters. Can be redefined by sub-classes
     * to either override existing letters or augment the letter set.
     * If so, the format() method must provide the (re-)implementation of the
     * pattern.
     */
    protected String fSupplPatternLetters = "TSCN"; //$NON-NLS-1$
    /**
     * The sub-second pattern letters.
     * @since 3.0
     */
    protected String fSubSecPatternChars = "SCN"; //$NON-NLS-1$
    /**
     * The optional sub-second delimiter characters.
     * @since 3.0
     */
    protected String fDelimiterChars = " .,-_:;/'\""; //$NON-NLS-1$

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
        fLocale = Locale.getDefault();
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
        fLocale = Locale.getDefault();
        setTimeZone(timeZone);
        applyPattern(pattern);
    }

    /**
     * The fuller constructor
     *
     * @param pattern the format pattern
     * @param timeZone the time zone
     * @param locale the locale
     * @since 3.1
     */
    public TmfTimestampFormat(String pattern, TimeZone timeZone, Locale locale) {
        super("", locale); //$NON-NLS-1$
        fLocale = locale;
        setTimeZone(timeZone);
        setCalendar(Calendar.getInstance(timeZone, locale));
        applyPattern(pattern);
    }

    /**
     * The copy constructor
     *
     * @param other the other format pattern
     */
    public TmfTimestampFormat(TmfTimestampFormat other) {
        this(other.fPattern, other.getTimeZone(), other.fLocale);
    }

    // ------------------------------------------------------------------------
    // Getters/setters
    // ------------------------------------------------------------------------

    /**
     * @since 2.1
     */
    public static void updateDefaultFormats() {
        fDefaultTimeFormat = new TmfTimestampFormat(
                TmfTimePreferences.getInstance().getTimePattern(),
                TmfTimePreferences.getInstance().getTimeZone(),
                TmfTimePreferences.getInstance().getLocale());
        fDefaultIntervalFormat = new TmfTimestampFormat(TmfTimePreferences.getInstance().getIntervalPattern());
    }

    /**
     * @return the default time format pattern
     */
    public static TmfTimestampFormat getDefaulTimeFormat() {
        if (fDefaultTimeFormat == null) {
            fDefaultTimeFormat = new TmfTimestampFormat(
                    TmfTimePreferences.getInstance().getTimePattern(),
                    TmfTimePreferences.getInstance().getTimeZone(),
                    TmfTimePreferences.getInstance().getLocale());
        }
        return fDefaultTimeFormat;
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
        fPatternDecimalSeparatorIndex = indexOfPatternDecimalSeparator(pattern);
        fDateTimePattern = unquotePattern(pattern.substring(0, fPatternDecimalSeparatorIndex));
        // Check that 'S' is not present in the date and time pattern
        if (fDateTimePattern.indexOf('S') != -1) {
            throw new IllegalArgumentException("Illegal pattern character 'S'"); //$NON-NLS-1$
        }
        // Check that 'W' is not present in the date and time pattern
        if (fDateTimePattern.indexOf('W') != -1) {
            throw new IllegalArgumentException("Illegal pattern character 'W'"); //$NON-NLS-1$
        }
        // The super pattern is the date/time pattern, quoted and bracketed
        super.applyPattern(quoteSpecificTags(pattern.substring(0, fPatternDecimalSeparatorIndex), true));
        // The sub-seconds pattern is bracketed (but not quoted)
        fSubSecPattern = quoteSpecificTags(pattern.substring(fPatternDecimalSeparatorIndex), false);
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

        int fractionDigitsPrinted = 0;
        // Fill in our extensions
        for (String pattern : fSupplPatterns) {
            int length = pattern.length();
            long val = 0;
            int bufLength = 0;

            // Format the proper value as per the pattern
            switch (pattern.charAt(0)) {
                case 'T':
                    if (value < 0 && sec == 0) {
                        result.insert(0, '-');
                    }
                    val = sec;
                    bufLength = Math.min(length, 10);
                    break;
                case 'S':
                    val = 1000000 * ms + 1000 * cs + ns;
                    bufLength = 9;
                    break;
                case 'C':
                    val = cs;
                    bufLength = Math.min(length, 3);
                    break;
                case 'N':
                    val = ns;
                    bufLength = Math.min(length, 3);
                    break;
                default:
                    break;
            }

            // Prepare the format buffer
            StringBuffer fmt = new StringBuffer();
            for (int i = 0; i < bufLength; i++) {
                fmt.append("0"); //$NON-NLS-1$
            }
            DecimalFormat dfmt = new DecimalFormat(fmt.toString());
            String fmtVal = dfmt.format(val);
            if (pattern.charAt(0) == 'S') {
                fmtVal = fmtVal.substring(fractionDigitsPrinted, Math.min(bufLength, fractionDigitsPrinted + length));
                fractionDigitsPrinted += fmtVal.length();
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
     * @param source the source string
     * @param ref the reference (base) time (in ns)
     * @return the parsed value (in ns)
     * @throws ParseException if the string has an invalid format
     */
    public synchronized long parseValue(final String source, final long ref) throws ParseException {

        // Trivial case
        if (source == null || source.length() == 0) {
            return 0;
        }

        long seconds  = 0;
        boolean isNegative = source.charAt(0) == '-';
        boolean isDateTimeFormat = true;

        int index = indexOfSourceDecimalSeparator(source);

        // Check for seconds in epoch pattern
        for (String pattern : fSupplPatterns) {
            if (pattern.charAt(0) == 'T') {
                isDateTimeFormat = false;
                // Remove everything up to the first "." and compute the
                // number of seconds since the epoch. If there is no period,
                // assume an integer value and return immediately
                if (index == 0 || (isNegative && index <= 1)) {
                    seconds = 0;
                } else if (index == source.length()) {
                    return new DecimalFormat("0").parse(source).longValue() * 1000000000; //$NON-NLS-1$
                } else {
                    seconds = new DecimalFormat("0").parse(source.substring(0, index)).longValue(); //$NON-NLS-1$
                }
                break;
            }
        }

        // If there was no "T" (thus not an interval), parse as a date
        if (isDateTimeFormat && super.toPattern().length() > 0) {
            Date baseDate = super.parse(source.substring(0, index));
            getCalendar();

            if (ref != Long.MIN_VALUE) {
                Calendar baseTime = Calendar.getInstance(getTimeZone(), fLocale);
                baseTime.setTimeInMillis(baseDate.getTime());
                Calendar newTime = Calendar.getInstance(getTimeZone(), fLocale);
                newTime.setTimeInMillis(ref / 1000000);
                boolean setRemainingFields = false;
                if (dateTimePatternContains("yY")) { //$NON-NLS-1$
                    newTime.set(Calendar.YEAR, baseTime.get(Calendar.YEAR));
                    setRemainingFields = true;
                }
                if (setRemainingFields || dateTimePatternContains("M")) { //$NON-NLS-1$
                    newTime.set(Calendar.MONTH, baseTime.get(Calendar.MONTH));
                    setRemainingFields = true;
                }
                if (setRemainingFields || dateTimePatternContains("d")) { //$NON-NLS-1$
                    newTime.set(Calendar.DATE, baseTime.get(Calendar.DATE));
                    setRemainingFields = true;
                } else if (dateTimePatternContains("D")) { //$NON-NLS-1$
                    newTime.set(Calendar.DAY_OF_YEAR, baseTime.get(Calendar.DAY_OF_YEAR));
                    setRemainingFields = true;
                } else if (dateTimePatternContains("w")) { //$NON-NLS-1$
                    newTime.set(Calendar.WEEK_OF_YEAR, baseTime.get(Calendar.WEEK_OF_YEAR));
                    setRemainingFields = true;
                }
                if (dateTimePatternContains("F")) { //$NON-NLS-1$
                    newTime.set(Calendar.DAY_OF_WEEK_IN_MONTH, baseTime.get(Calendar.DAY_OF_WEEK_IN_MONTH));
                    setRemainingFields = true;
                }
                if (dateTimePatternContains("Eu")) { //$NON-NLS-1$
                    newTime.set(Calendar.DAY_OF_WEEK, baseTime.get(Calendar.DAY_OF_WEEK));
                    setRemainingFields = true;
                }
                if (setRemainingFields || dateTimePatternContains("aHkKh")) { //$NON-NLS-1$
                    newTime.set(Calendar.HOUR_OF_DAY, baseTime.get(Calendar.HOUR_OF_DAY));
                    setRemainingFields = true;
                }
                if (setRemainingFields || dateTimePatternContains("m")) { //$NON-NLS-1$
                    newTime.set(Calendar.MINUTE, baseTime.get(Calendar.MINUTE));
                    setRemainingFields = true;
                }
                if (setRemainingFields || dateTimePatternContains("s")) { //$NON-NLS-1$
                    newTime.set(Calendar.SECOND, baseTime.get(Calendar.SECOND));
                }
                newTime.set(Calendar.MILLISECOND, 0);
                seconds = newTime.getTimeInMillis() / 1000;
            } else {
                seconds = baseDate.getTime() / 1000;
            }
        } else if (isDateTimeFormat && ref != Long.MIN_VALUE) {
            // If the date and time pattern is empty, adjust for reference
            seconds = ref / 1000000000;
        }

        long nanos = parseSubSeconds(source.substring(index));
        if (isNegative && !isDateTimeFormat) {
            nanos = -nanos;
        }
        // Compute the value in ns
        return seconds * 1000000000 + nanos;
    }

    /**
     * Parse a string according to the format pattern
     *
     * @param source the source string
     * @return the parsed value (in ns)
     * @throws ParseException if the string has an invalid format
     */
    public long parseValue(final String source) throws ParseException {
        long result = parseValue(source, Long.MIN_VALUE);
        return result;

    }

    // ------------------------------------------------------------------------
    // Helper functions
    // ------------------------------------------------------------------------

    /**
     * Finds the index of the decimal separator in the pattern string, which is
     * the last delimiter found before the first sub-second pattern character.
     * Returns the pattern string length if decimal separator is not found.
     */
    private int indexOfPatternDecimalSeparator(String pattern) {
        int lastDelimiterIndex = pattern.length();
        boolean inQuote = false;
        int index = 0;
        while (index < pattern.length()) {
            char ch = pattern.charAt(index);
            if (ch == '\'') {
                if (index + 1 < pattern.length()) {
                    index++;
                    ch = pattern.charAt(index);
                    if (ch != '\'') {
                        inQuote = !inQuote;
                    }
                }
            }
            if (!inQuote) {
                if (fSubSecPatternChars.indexOf(ch) != -1) {
                    if (lastDelimiterIndex < pattern.length()) {
                        fDecimalSeparator = pattern.charAt(lastDelimiterIndex);
                    }
                    return lastDelimiterIndex;
                }
                if (fDelimiterChars.indexOf(ch) != -1) {
                    lastDelimiterIndex = index;
                    if (ch == '\'') {
                        lastDelimiterIndex--;
                    }
                }
            }
            index++;
        }
        return pattern.length();
    }

    /**
     * Finds the first index of a decimal separator in the source string.
     * Skips the number of decimal separators in the format pattern.
     * Returns the source string length if decimal separator is not found.
     */
    private int indexOfSourceDecimalSeparator(String source) {
        String pattern = fPattern.substring(0, fPatternDecimalSeparatorIndex);
        String separator = fDecimalSeparator == '\'' ? "''" : String.valueOf(fDecimalSeparator); //$NON-NLS-1$
        int sourcePos = source.indexOf(fDecimalSeparator);
        int patternPos = pattern.indexOf(separator);
        while (patternPos != -1 && sourcePos != -1) {
            sourcePos = source.indexOf(fDecimalSeparator, sourcePos + 1);
            patternPos = pattern.indexOf(separator, patternPos + separator.length());
        }
        if (sourcePos == -1) {
            sourcePos = source.length();
        }
        return sourcePos;
    }

    /**
     * Parse the sub-second digits in the input. Handle delimiters as optional
     * characters. Match any non-pattern and non-delimiter pattern characters
     * against the input. Returns the number of nanoseconds.
     */
    private long parseSubSeconds(String input) throws ParseException {
        StringBuilder digits = new StringBuilder("000000000"); //$NON-NLS-1$
        String pattern = fPattern.substring(fPatternDecimalSeparatorIndex);
        boolean inQuote = false;
        int digitIndex = 0;
        int inputIndex = 0;
        int patternIndex = 0;
        while (patternIndex < pattern.length()) {
            char ch = pattern.charAt(patternIndex);
            if (ch == '\'') {
                patternIndex++;
                if (patternIndex < pattern.length()) {
                    ch = pattern.charAt(patternIndex);
                    if (ch != '\'') {
                        inQuote = !inQuote;
                    }
                } else if (inQuote) {
                    // final end quote
                    break;
                }
            }
            if (fDelimiterChars.indexOf(ch) != -1 && !inQuote) {
                // delimiter is optional if not in quote
                if (inputIndex < input.length() && input.charAt(inputIndex) == ch) {
                    inputIndex++;
                }
                patternIndex++;
                continue;
            } else if (fSubSecPatternChars.indexOf(ch) != -1 && !inQuote) {
                // read digit if not in quote
                if (inputIndex < input.length() && Character.isDigit(input.charAt(inputIndex))) {
                    if (digitIndex < digits.length()) {
                        digits.setCharAt(digitIndex, input.charAt(inputIndex));
                        digitIndex++;
                    }
                    inputIndex++;
                } else {
                    // not a digit, stop parsing digits
                    digitIndex = digits.length();
                }
                patternIndex++;
                continue;
            }
            if (inputIndex >= input.length() || input.charAt(inputIndex) != ch) {
                throw new ParseException("Unparseable sub-seconds: \"" + input + '\"', inputIndex); //$NON-NLS-1$
            }
            patternIndex++;
            inputIndex++;
        }
        return Long.parseLong(digits.toString());
    }

    /**
     * Copy the pattern but quote (bracket with "[&" and "&]") the
     * TmfTimestampFormat specific tags. Optionally surround tags with single
     * quotes so these fields are treated as comments by the base class.
     *
     * It also keeps track of the corresponding quoted fields so they can be
     * properly populated later on (by format()).
     *
     * @param pattern
     *            the 'extended' pattern
     * @param includeQuotes
     *            true to include quotes from pattern and add single quotes
     *            around tags
     * @return the quoted and bracketed pattern
     */
    private String quoteSpecificTags(final String pattern, boolean includeQuotes) {

        StringBuffer result = new StringBuffer();

        int length = pattern.length();
        boolean inQuote = false;

        for (int i = 0; i < length; i++) {
            char c = pattern.charAt(i);
            if (c != '\'' || includeQuotes) {
                result.append(c);
            }
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
            if (!inQuote && (fSupplPatternLetters.indexOf(c) != -1)) {
                if (pattern.charAt(0) == fDecimalSeparator) {
                    if (fSubSecPatternChars.indexOf(c) == -1) {
                        // do not quote non-sub-second pattern letters in sub-second pattern
                        continue;
                    }
                } else {
                    if (fSubSecPatternChars.indexOf(c) != -1) {
                        // do not quote sub-second pattern letters in date and time pattern
                        continue;
                    }
                }
                StringBuilder pat = new StringBuilder();
                pat.append(c);
                if (includeQuotes) {
                    result.insert(result.length() - 1, "'"); //$NON-NLS-1$
                }
                result.insert(result.length() - 1, fOpenBracket);
                while ((i + 1) < length && pattern.charAt(i + 1) == c) {
                    result.append(c);
                    pat.append(c);
                    i++;
                }
                result.append(fCloseBracket);
                if (includeQuotes) {
                    result.append("'"); //$NON-NLS-1$
                }
                fSupplPatterns.add(pat.toString());
            }
        }
        return result.toString();
    }

    /**
     * Returns the unquoted characters in this pattern.
     */
    private static String unquotePattern(String pattern) {
        boolean inQuote = false;
        int index = 0;
        StringBuilder result = new StringBuilder();
        while (index < pattern.length()) {
            char ch = pattern.charAt(index);
            if (ch == '\'') {
                if (index + 1 < pattern.length()) {
                    index++;
                    ch = pattern.charAt(index);
                    if (ch != '\'') {
                        inQuote = !inQuote;
                    }
                }
            }
            if (!inQuote) {
                result.append(ch);
            }
            index++;
        }
        return result.toString();
    }

    /**
     * Returns true if the date and time pattern contains any of these chars.
     */
    private boolean dateTimePatternContains(String chars) {
        int index = 0;
        while (index < chars.length()) {
            char ch = chars.charAt(index);
            if (fDateTimePattern.indexOf(ch) != -1) {
                return true;
            }
            index++;
        }
        return false;
    }
}
