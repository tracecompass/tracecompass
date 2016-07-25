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
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlEvent;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTrace;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

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
            return new CustomXmlTrace(null, fDefinition, file.getPath(), BLOCK_SIZE);
        }

        @Override
        public void validateEvent(ITmfEvent event) {
            assertTrue(event instanceof CustomXmlEvent);
            String name = fDefinition.definitionName;
            assertEquals("Event name", name, event.getName());
            assertEquals("Event name and type", event.getType().getName(), event.getName());
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
            return new CustomXmlTrace(null, fDefinition, file.getPath(), BLOCK_SIZE);
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
                { "Parse with event name", CUSTOM_XML_EVENT_NAME }
        });
    }

}
