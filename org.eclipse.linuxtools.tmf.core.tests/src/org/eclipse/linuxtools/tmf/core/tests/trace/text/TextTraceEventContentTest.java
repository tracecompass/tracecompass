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
package org.eclipse.linuxtools.tmf.core.tests.trace.text;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.trace.text.TextTraceEventContent;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.text.SyslogEventType;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.text.SyslogEventType.Index;
import org.junit.Test;

/**
 * Test suite for the {@link TextTraceEventContent} class.
 */
@SuppressWarnings({ "javadoc", "nls" })
public class TextTraceEventContentTest {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------
    private TextTraceEventContent fEventContent1;
    private TextTraceEventContent fEventContent2;

    public TextTraceEventContentTest () {
        fEventContent1 = new TextTraceEventContent(SyslogEventType.LABELS);
        fEventContent1.setValue("CONTENT");
        fEventContent1.setFieldValue(Index.TIMESTAMP, "Jan 1 01:01:01");
        fEventContent1.setFieldValue(Index.HOST, "HostA");
        fEventContent1.setFieldValue(Index.LOGGER, "LoggerA");
        fEventContent1.setFieldValue(Index.MESSAGE, "MessageA");

        fEventContent2 = new TextTraceEventContent(SyslogEventType.LABELS);
        fEventContent2.setFieldValue(SyslogEventType.LABELS[0], "Jan 1 02:02:02");
        fEventContent2.setFieldValue(SyslogEventType.LABELS[1], "HostB");
        fEventContent2.setFieldValue(SyslogEventType.LABELS[2], "LoggerB");
        StringBuffer buffer = new StringBuffer();
        buffer.append("Message B");
        fEventContent2.setFieldValue(SyslogEventType.LABELS[3],   buffer);
    }

    public void testConstructorConstructor() {
        assertEquals("getField:TIMESTAMP", "Jan 1 01:01:01", fEventContent1.getFieldValue(Index.TIMESTAMP));
        assertEquals("getField:HOST", "HostA", fEventContent1.getFieldValue(Index.HOST));
        assertEquals("getField:LOGGER", "LoggerA", fEventContent1.getFieldValue(Index.LOGGER));
        assertEquals("getField:MESSAGE", "MessageA", fEventContent1.getFieldValue(Index.MESSAGE).toString());
    }

    // ------------------------------------------------------------------------
    // Event Type
    // ------------------------------------------------------------------------

    @Test
    public void testEventTypeInstance() {
        SyslogEventType eventType = SyslogEventType.INSTANCE;
        assertEquals("getTypeId", "Syslog", eventType.getName());
        assertNotNull ("instance", eventType);
        assertEquals("getLabel", "Timestamp", eventType.getFieldNames()[0]);
        assertEquals("getLabel", "Host", eventType.getFieldNames()[1]);
        assertEquals("getLabel", "Logger", eventType.getFieldNames()[2]);
        assertEquals("getLabel", "Message", eventType.getFieldNames()[3]);
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    @Test
    public void testEqualsReflexivity() {
        TextTraceEventContent content1 = fEventContent1.clone();
        TextTraceEventContent content2 = fEventContent2.clone();
        assertEquals("equals", content1, content1);
        assertEquals("equals", content2, content2);

        assertTrue("equals", !content1.equals(content2));
        assertTrue("equals", !content2.equals(content1));
    }

    @Test
    public void testEqualsTransivity() {
        TextTraceEventContent content1 = fEventContent1.clone();
        TextTraceEventContent content2 = fEventContent1.clone();
        TextTraceEventContent content3 = fEventContent1.clone();

        assertEquals("equals", content1, content2);
        assertEquals("equals", content2, content3);
        assertEquals("equals", content1, content3);
    }

    @Test
    public void testEqualsNull() {
        assertTrue("equals", !fEventContent1.equals(null));
        assertTrue("equals", !fEventContent2.equals(null));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------
    @Test
    public void testHashCode() {
        TextTraceEventContent content1 = fEventContent1.clone();
        TextTraceEventContent content2 = fEventContent2.clone();

        assertEquals("hashCode", fEventContent1.hashCode(), content1.hashCode());
        assertEquals("hashCode", fEventContent2.hashCode(), content2.hashCode());

        assertTrue("hashCode", fEventContent1.hashCode() != content2.hashCode());
        assertTrue("hashCode", fEventContent2.hashCode() != content1.hashCode());
    }

    // ------------------------------------------------------------------------
    // Event Content
    // ------------------------------------------------------------------------

    @Test
    public void testGetFieldValueWithIndex() {
        assertEquals("getFieldValue:TIMESTAMP", "Jan 1 01:01:01", fEventContent1.getFieldValue(Index.TIMESTAMP));
        assertEquals("getFieldValue:HOST", "HostA", fEventContent1.getFieldValue(Index.HOST));
        assertEquals("getFieldValue:LOGGER", "LoggerA", fEventContent1.getFieldValue(Index.LOGGER));
        assertEquals("getFieldValue:MESSAGE", "MessageA", fEventContent1.getFieldValue(Index.MESSAGE));
        assertNull(fEventContent1.getFieldValue(4));
    }

    @Test
    public void testGetFieldValueWithName() {
        assertEquals("getFieldValue:TIMESTAMP", "Jan 1 01:01:01", fEventContent1.getFieldValue("Timestamp"));
        assertEquals("getFieldValue:HOST", "HostA", fEventContent1.getFieldValue("Host"));
        assertEquals("getFieldValue:LOGGER", "LoggerA", fEventContent1.getFieldValue("Logger"));
        assertEquals("getFieldValue:MESSAGE", "MessageA", fEventContent1.getFieldValue("Message"));
        assertNull(fEventContent1.getFieldValue("BlaBla"));
    }

    @Test
    public void testGetFieldNameWithIndex() {

        assertEquals("getFieldName:TIMESTAMP", SyslogEventType.LABELS[0], fEventContent1.getFieldName(Index.TIMESTAMP));
        assertEquals("getFieldName:HOST", SyslogEventType.LABELS[1], fEventContent1.getFieldName(Index.HOST));
        assertEquals("getFieldName:LOGGER", SyslogEventType.LABELS[2], fEventContent1.getFieldName(Index.LOGGER));
        assertEquals("getFieldName:MESSAGE", SyslogEventType.LABELS[3], fEventContent1.getFieldName(Index.MESSAGE));
        assertNull(fEventContent1.getFieldValue(4));
    }

    @Test
    public void testGetFields() {
        ITmfEventField[] fields = fEventContent1.getFields();
        assertEquals(4, fields.length);
        assertEquals("getFields:TIMESTAMP", SyslogEventType.LABELS[0], fields[Index.TIMESTAMP].getName());
        assertEquals("getFields:TIMESTAMP", "Jan 1 01:01:01", fields[Index.TIMESTAMP].getValue());
        assertEquals("getFields:HOST", SyslogEventType.LABELS[1], fields[Index.HOST].getName());
        assertEquals("getFields:HOST", "HostA", fields[Index.HOST].getValue());
        assertEquals("getFields:LOGGER", SyslogEventType.LABELS[2], fields[Index.LOGGER].getName());
        assertEquals("getFields:LOGGER", "LoggerA", fields[Index.LOGGER].getValue());
        assertEquals("getFields:MESSAGE", SyslogEventType.LABELS[3], fields[Index.MESSAGE].getName());
        assertEquals("getFields:MESSAGE", "MessageA", fields[Index.MESSAGE].getValue());
    }

    @Test
    public void testGetFieldWithName() {
        ITmfEventField field = fEventContent1.getField("Timestamp");
        assertEquals("getFieldName:TIMESTAMP", SyslogEventType.LABELS[0], field.getName());
        assertEquals("getFieldName:TIMESTAMP", "Jan 1 01:01:01", field.getValue());

        field = fEventContent1.getField(SyslogEventType.LABELS[1]);
        assertEquals("getFieldName:HOST", SyslogEventType.LABELS[1], field.getName());
        assertEquals("getFieldName:HOST", "HostA", field.getValue());

        field = fEventContent1.getField(SyslogEventType.LABELS[2]);
        assertEquals("getFieldName:LOGGER", SyslogEventType.LABELS[2], field.getName());
        assertEquals("getFieldName:LOGGER", "LoggerA", field.getValue());

        field = fEventContent1.getField(SyslogEventType.LABELS[3]);
        assertEquals("getFieldName:Message", SyslogEventType.LABELS[3], field.getName());
        assertEquals("getgetFieldName:Message", "MessageA", field.getValue());

        field = fEventContent1.getField("BlaBla");
        assertNull(field);
    }

    @Test
    public void testGetFieldWithIndex() {
        ITmfEventField field = fEventContent1.getField(0);
        assertEquals("getField:TIMESTAMP", SyslogEventType.LABELS[0], field.getName());
        assertEquals("getField:TIMESTAMP", "Jan 1 01:01:01", field.getValue());

        field = fEventContent1.getField(1);
        assertEquals("getField:HOST", SyslogEventType.LABELS[1], field.getName());
        assertEquals("getField:HOST", "HostA", field.getValue());

        field = fEventContent1.getField(2);
        assertEquals("getField:LOGGER", SyslogEventType.LABELS[2], field.getName());
        assertEquals("getField:LOGGER", "LoggerA", field.getValue());

        field = fEventContent1.getField(3);
        assertEquals("getField:MESSAGE", SyslogEventType.LABELS[3], field.getName());
        assertEquals("getField:MESSAGE", "MessageA", field.getValue());

        field = fEventContent1.getField(4);
        assertNull(field);
    }

    @Test
    public void testGetFormattedValue() {
        assertEquals("CONTENT", fEventContent1.getFormattedValue());
    }

    @Test
    public void testToString() {
        assertEquals("Timestamp=Jan 1 01:01:01, Host=HostA, Logger=LoggerA, Message=MessageA", fEventContent1.toString());
    }

    @Test
    public void testGetSubField() {
        String[] names = { "Timestamp"};

        ITmfEventField field = fEventContent1.getSubField(names);
        assertEquals("getSubField:TIMESTAMP", SyslogEventType.LABELS[0], field.getName());
        assertEquals("getSubField:TIMESTAMP", "Jan 1 01:01:01", field.getValue());

        String[] names2 = { "Timestamp", "subField" };
        field = fEventContent1.getSubField(names2);
        assertNull(field);
    }

    @Test
    public void testGetFieldNames() {
        String[] labels = {"Timestamp", "Host", "Logger", "Message"};
        String[] names = fEventContent1.getFieldNames();
        assertArrayEquals(labels, names);
    }

}
