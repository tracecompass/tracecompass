/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.stubs.trace.text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.linuxtools.tmf.core.trace.text.TextTrace;
import org.eclipse.linuxtools.tmf.core.trace.text.TextTraceEventContent;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.text.SyslogEventType.Index;

/**
 * Extension of TmfTrace for handling of system logs.
 */
public class SyslogTrace extends TextTrace<SyslogEvent> {

    /** The cache size for system log traces. */
    private static final int CACHE_SIZE = 100;
    /** The time stamp format of the trace type. */
    public static final String TIMESTAMP_FORMAT = "MMM dd HH:mm:ss"; //$NON-NLS-1$
    /** The corresponding date format of the time stamp. */
    public static final SimpleDateFormat TIMESTAMP_SIMPLEDATEFORMAT = new SimpleDateFormat(TIMESTAMP_FORMAT);
    /** The scale of the time stamps. */
    public static final byte TIMESTAMP_SCALE = ITmfTimestamp.MILLISECOND_SCALE;
    /** The regular expression pattern of the first line of an event. */
    public static final Pattern LINE1_PATTERN = Pattern.compile(
            "\\s*(\\S\\S\\S \\d\\d? \\d\\d:\\d\\d:\\d\\d)\\s*(\\S*)\\s*(\\S*):+\\s*(.*\\S)?"); //$NON-NLS-1$

    /* The current calendar to use */
    private static final Calendar CURRENT = Calendar.getInstance();

    /**
     * Constructor
     */
    public SyslogTrace() {
        setCacheSize(CACHE_SIZE);
    }

    @Override
    protected Pattern getFirstLinePattern() {
        return LINE1_PATTERN;
    }

    @Override
    protected SyslogEvent parseFirstLine(Matcher matcher, String line) {

        ITmfTimestamp timestamp = null;

        try {
            synchronized (TIMESTAMP_SIMPLEDATEFORMAT) {
                TIMESTAMP_SIMPLEDATEFORMAT.setTimeZone(TmfTimestampFormat.getDefaulTimeFormat().getTimeZone());
                Date date = TIMESTAMP_SIMPLEDATEFORMAT.parse(matcher.group(1));
                GregorianCalendar calendar = new GregorianCalendar();
                calendar.setTime(date);
                calendar.set(Calendar.YEAR, CURRENT.get(Calendar.YEAR));
                if (calendar.after(CURRENT)) {
                    calendar.set(Calendar.YEAR, CURRENT.get(Calendar.YEAR) - 1);
                }
                long ms = calendar.getTimeInMillis();
                timestamp = new TmfTimestamp(ms, TIMESTAMP_SCALE);
            }
        } catch (ParseException e) {
            timestamp = new TmfTimestamp();
        }

        TextTraceEventContent content = new TextTraceEventContent(SyslogEventType.LABELS);
        content.setValue(new StringBuffer(line));
        content.setFieldValue(Index.TIMESTAMP, matcher.group(1));
        content.setFieldValue(Index.HOST, matcher.group(2));
        content.setFieldValue(Index.LOGGER, matcher.group(3));
        content.setFieldValue(Index.MESSAGE, new StringBuffer(matcher.group(4) != null ? matcher.group(4) : "")); //$NON-NLS-1$

        SyslogEvent event = new SyslogEvent(
                this,
                timestamp,
                "", //$NON-NLS-1$
                SyslogEventType.INSTANCE,
                content,
                ""); //$NON-NLS-1$

        return event;
    }

    @Override
    protected void parseNextLine(SyslogEvent event, String line) {
        TextTraceEventContent content = (TextTraceEventContent) event.getContent();
        ((StringBuffer) content.getValue()).append("\n").append(line); //$NON-NLS-1$
        if (line.trim().length() > 0) {
            ((StringBuffer) content.getFieldValue(Index.MESSAGE)).append(SEPARATOR + line.trim());
        }
    }

    @Override
    public ITmfTimestamp getInitialRangeOffset() {
        return new TmfTimestamp(60, ITmfTimestamp.SECOND_SCALE);
    }

}
