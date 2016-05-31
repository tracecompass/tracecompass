/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.parsers.custom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfBaseAspects;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlEvent;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTrace;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition.Tag;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

/**
 * Test the events parsed by a custom XML trace
 *
 * @author Geneviève Bastien
 */
@RunWith(Parameterized.class)
public class CustomXmlTraceDataTest extends AbstractCustomTraceDataTest {

    private static final String TRACE_PATH = TRACE_DIRECTORY + File.separator + "test.xml";
    private static final String DEFINITION_PATH = "testfiles" + File.separator + "xml" + File.separator + "testDefinition.xml";

    /**
     * Constructor
     *
     * @param name
     *            The name of this test
     * @param data
     *            The custom test data for this test case
     */
    public CustomXmlTraceDataTest(String name, @NonNull ICustomTestData data) {
        super(data);
    }

    private static CustomXmlTraceDefinition getDefinition(int index) {
        CustomXmlTraceDefinition[] definitions = CustomXmlTraceDefinition.loadAll(new File(DEFINITION_PATH).toString());
        return definitions[index];
    }

    private static final ICustomTestData CUSTOM_XML = new ICustomTestData() {

        private static final int NB_EVENTS = 10;
        private CustomXmlTraceDefinition fDefinition;
        private ITmfEventAspect<?> fTimestampAspect;

        @Override
        public ITmfTrace getTrace() throws IOException, TmfTraceException {
            fDefinition = getDefinition(0);
            final File file = new File(TRACE_PATH);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file));) {
                writer.write("<trace>");
                for (int i = 0; i < NB_EVENTS; ++i) {
                    SimpleDateFormat f = new SimpleDateFormat(TIMESTAMP_FORMAT);
                    String eventStr = "<element time=\"" + f.format(new Date(i)) + "\">message</element>\n";
                    writer.write(eventStr);
                }
                writer.write("</trace>");
            }
            ITmfTrace trace = new CustomXmlTrace(null, fDefinition, file.getPath(), BLOCK_SIZE);
            ArrayList<@NonNull ITmfEventAspect<?>> aspects = Lists.newArrayList(trace.getEventAspects());
            fTimestampAspect = aspects.stream().filter(aspect -> aspect.getName().equals("Timestamp")).findFirst().get();
            return trace;
        }

        @Override
        public void validateEvent(ITmfEvent event) {
            assertTrue(event instanceof CustomXmlEvent);
            String name = fDefinition.definitionName;
            assertEquals("Event name", name, event.getName());
            assertEquals("Event name and type", event.getType().getName(), event.getName());
            assertEquals("Timestamp", Long.toString(event.getTimestamp().toNanos()), fTimestampAspect.resolve(event));
        }

        @Override
        public void validateEventCount(int eventCount) {
            assertEquals("Event count", NB_EVENTS, eventCount);
        }

    };

    private static final ICustomTestData CUSTOM_XML_EVENT_NAME = new ICustomTestData() {

        private static final int NB_EVENTS = 10;
        private static final String DEFAULT_EVENT = "DefaultName";
        private static final String ATTRIBUTE_EVENT = "AttributeName";
        private static final String ELEMENT_EVENT = "ElementName";
        private CustomXmlTraceDefinition fDefinition;
        private ITmfEventAspect<?> fTimestampAspect;

        @Override
        public ITmfTrace getTrace() throws IOException, TmfTraceException {
            fDefinition = getDefinition(1);
            final File file = new File(TRACE_PATH);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file));) {
                writer.write("<trace>");
                for (int i = 0; i < NB_EVENTS; ++i) {
                    String attribute = (i % 5) != 0 ? String.format(" type=\"%s\"", ATTRIBUTE_EVENT) : "";
                    String element = (i % 5) != 0 && i % 2 != 0 ? String.format("<type>%s</type>", ELEMENT_EVENT) : "";
                    String eventStr = String.format("<element time=\"" + i + "\"%s>%s</element>\n", attribute, element);
                    writer.write(eventStr);
                }
                writer.write("</trace>");
            }
            ITmfTrace trace = new CustomXmlTrace(null, fDefinition, file.getPath(), BLOCK_SIZE);
            ArrayList<@NonNull ITmfEventAspect<?>> aspects = Lists.newArrayList(trace.getEventAspects());
            fTimestampAspect = aspects.stream().filter(aspect -> aspect.getName().equals("Timestamp")).findFirst().get();
            return trace;
        }

        @Override
        public void validateEvent(ITmfEvent event) {
            assertTrue(event instanceof CustomXmlEvent);
            long ts = event.getTimestamp().getValue();
            if (ts % 5 == 0) {
                assertEquals("Event name", DEFAULT_EVENT, event.getName());
            } else if (ts % 2 == 0) {
                assertEquals("Event name", ATTRIBUTE_EVENT, event.getName());
            } else {
                assertEquals("Event name", ELEMENT_EVENT, event.getName());
            }
            assertEquals("Event name and type", event.getType().getName(), event.getName());
            assertEquals("Timestamp", TmfBaseAspects.getTimestampAspect().resolve(event), fTimestampAspect.resolve(event));
        }

        @Override
        public void validateEventCount(int eventCount) {
            assertEquals("Event count", NB_EVENTS, eventCount);
        }

    };

    private static final ICustomTestData CUSTOM_XML_EXTRA_FIELDS = new ICustomTestData() {

        private static final int NB_EVENTS = 5;
        private static final String FOO = "foo";
        private static final String BAR = "bar";
        private static final String BAZ = "baz";
        private static final String MESSAGE = "message";
        private CustomXmlTraceDefinition fDefinition;

        @Override
        public ITmfTrace getTrace() throws IOException, TmfTraceException {
            fDefinition = getDefinition(2);
            final File file = new File(TRACE_PATH);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file));) {
                writer.write("<trace>");
                // Event with one field to set
                String eventStr = String.format("<event timestamp=\"%s\" name=\"%s\">\n", "1", MESSAGE);
                eventStr += String.format("<field name=\"%s\" value=\"%s\"/>\n", FOO, BAR);
                eventStr += "</event>\n";
                writer.write(eventStr);
                // Event with 2 different fields and different values
                eventStr = String.format("<event timestamp=\"%s\" name=\"%s\">\n", "2", MESSAGE);
                eventStr += String.format("<field name=\"%s\" value=\"%s\"/>\n", FOO, BAR);
                eventStr += String.format("<field name=\"%s\" value=\"%s\"/>\n", BAR, FOO);
                eventStr += "</event>\n";
                writer.write(eventStr);
                // Event with an extra field that conflicts with a built-in field
                eventStr = String.format("<event timestamp=\"%s\" name=\"%s\">\n", "3", MESSAGE);
                eventStr += String.format("<field name=\"Message\" value=\"%s\"/>\n", FOO);
                eventStr += "</event>\n";
                writer.write(eventStr);
                // Event with 2 extra fields with same name where the values
                // should be appended
                eventStr = String.format("<event timestamp=\"%s\" name=\"%s\">\n", "4", MESSAGE);
                eventStr += String.format("<field name=\"%s\" value=\"%s\"/>\n", FOO, BAR);
                eventStr += String.format("<field name=\"%s\" value=\"%s\"/>\n", FOO, BAZ);
                eventStr += "</event>\n";
                writer.write(eventStr);
                // Event with 2 non matching number extra field names/values
                eventStr = String.format("<event timestamp=\"%s\" name=\"%s\">\n", "5", MESSAGE);
                eventStr += String.format("<fieldName value=\"%s\"/>\n", FOO);
                eventStr += String.format("<fieldValue value=\"%s\"/>\n", BAR);
                eventStr += String.format("<fieldValue value=\"%s\"/>\n", BAZ);
                eventStr += "</event>\n";
                writer.write(eventStr);
                writer.write("</trace>");
            }
            return new CustomXmlTrace(null, fDefinition, file.getPath(), BLOCK_SIZE);
        }

        @Override
        public void validateEvent(ITmfEvent event) {
            assertTrue(event instanceof CustomXmlEvent);
            long ts = event.getTimestamp().getValue();
            switch ((int) ts) {
            case 1:
                assertNotNull(event.getContent().getField(FOO));
                assertEquals(BAR, event.getContent().getField(FOO).getValue());
                assertNull(event.getContent().getField(BAR));
                break;
            case 2:
                assertNotNull(event.getContent().getField(FOO));
                assertEquals(BAR, event.getContent().getField(FOO).getValue());
                assertNotNull(event.getContent().getField(BAR));
                assertEquals(FOO, event.getContent().getField(BAR).getValue());
                break;
            case 3:
                assertNotNull(event.getContent().getField(Tag.MESSAGE.toString()));
                assertEquals(MESSAGE, event.getContent().getField(Tag.MESSAGE.toString()).getValue());
                break;
            case 4:
                assertNotNull(event.getContent().getField(FOO));
                assertEquals(BAR + CustomTraceDefinition.SEPARATOR + BAZ, event.getContent().getField(FOO).getValue());
                assertNull(event.getContent().getField(BAR));
                break;
            case 5:
                assertNotNull(event.getContent().getField(FOO));
                assertEquals(BAZ, event.getContent().getField(FOO).getValue());
                assertNull(event.getContent().getField(BAR));
                break;
            default:
                fail("unknown timestamp " + ts);
            }
            assertEquals("Event name and type", event.getType().getName(), event.getName());
        }

        @Override
        public void validateEventCount(int eventCount) {
            assertEquals("Event count", NB_EVENTS, eventCount);
        }

    };

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { "Base parser", CUSTOM_XML },
                { "Parse with event name", CUSTOM_XML_EVENT_NAME },
                { "Parse with extra fields", CUSTOM_XML_EXTRA_FIELDS }
        });
    }

}
