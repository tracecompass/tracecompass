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
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtEvent;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTrace;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test the events parsed by a custom txt trace
 *
 * @author Geneviève Bastien
 */
@RunWith(Parameterized.class)
public class CustomTxtTraceDataTest extends AbstractCustomTraceDataTest {

    private static final String TRACE_PATH = TRACE_DIRECTORY + File.separator + "test.txt";
    private static final String DEFINITION_PATH = "testfiles" + File.separator + "txt" + File.separator + "testTxtDefinition.xml";

    /**
     * Constructor
     *
     * @param name The name of the test
     * @param data The test data
     */
    public CustomTxtTraceDataTest(String name, @NonNull ICustomTestData data) {
        super(data);
    }


    private static CustomTxtTraceDefinition getDefinition(int index) {
        CustomTxtTraceDefinition[] definitions = CustomTxtTraceDefinition.loadAll(new File(DEFINITION_PATH).toString());
        return definitions[index];
    }

    private static final ICustomTestData CUSTOM_TXT = new ICustomTestData() {

        private static final int NB_EVENTS = 10;
        private CustomTxtTraceDefinition fDefinition;

        @Override
        public ITmfTrace getTrace() throws IOException, TmfTraceException {
            fDefinition = getDefinition(0);
            final File file = new File(TRACE_PATH);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file));) {
                for (int i = 0; i < NB_EVENTS; ++i) {
                    String eventStr = i + " hello world\n";
                    writer.write(eventStr);
                    int extra = i % 3;
                    for (int j = 0; j < extra; j++) {
                        writer.write("extra line\n");
                    }
                }
            }
            return new CustomTxtTrace(null, fDefinition, file.getPath(), BLOCK_SIZE);
        }

        @Override
        public void validateEvent(ITmfEvent event) {
            assertTrue(event instanceof CustomTxtEvent);
            String name = fDefinition.definitionName;
            assertEquals("Event name", name, event.getName());
            assertEquals("Event name and type", event.getType().getName(), event.getName());
        }

        @Override
        public void validateEventCount(int eventCount) {
            assertEquals("Event count", NB_EVENTS, eventCount);
        }

    };

    private static final ICustomTestData CUSTOM_TXT_EVENT_NAME = new ICustomTestData() {

        private static final int NB_EVENTS = 10;
        private static final String DEFAULT_EVENT = "DefaultName";
        private static final String ODD_EVENT = "OddName";
        private static final String EVEN_EVENT = "EvenName";
        private CustomTxtTraceDefinition fDefinition;

        @Override
        public ITmfTrace getTrace() throws IOException, TmfTraceException {
            fDefinition = getDefinition(1);
            final File file = new File(TRACE_PATH);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file));) {
                for (int i = 1; i <= NB_EVENTS; ++i) {
                    String evName = (i % 5) == 0 ? DEFAULT_EVENT : ((i % 2) == 0) ? EVEN_EVENT : ODD_EVENT;
                    String eventStr = i + " " + evName + "\n";
                    writer.write(eventStr);
                    int extra = i % 3;
                    for (int j = 0; j < extra; j++) {
                        writer.write("extra line\n");
                    }
                }
            }
            return new CustomTxtTrace(null, fDefinition, file.getPath(), BLOCK_SIZE);
        }

        @Override
        public void validateEvent(ITmfEvent event) {
            assertTrue(event instanceof CustomTxtEvent);
            long ts = event.getTimestamp().getValue();
            if (ts % 5 == 0) {
                assertEquals("Event name", DEFAULT_EVENT, event.getName());
            } else if (ts % 2 == 0) {
                assertEquals("Event name", EVEN_EVENT, event.getName());
            } else {
                assertEquals("Event name", ODD_EVENT, event.getName());
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
                { "Base parser", CUSTOM_TXT },
                { "Parse with event name", CUSTOM_TXT_EVENT_NAME }
        });
    }

}
