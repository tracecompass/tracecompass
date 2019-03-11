/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.tests.shared.LttngTraceGenerator;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader;
import org.eclipse.tracecompass.ctf.core.trace.Metadata;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for streaming support
 *
 * @author Matthew Khouzam
 *
 */
public class CTFTraceGrowingTest {

    private static final String METADATA = "metadata";
    private final String fPathName = LttngTraceGenerator.getPath();
    private final CTFTrace fixture = new CTFTrace();

    /**
     * Init
     *
     * @throws IOException
     *             an IO error
     * @throws FileNotFoundException
     *             file's not there
     * @throws CTFException
     *             error in metadata
     */
    @Before
    public void init() throws FileNotFoundException, IOException, CTFException {
        Metadata md = new Metadata(fixture);
        File metadata = new File(fPathName + File.separator + METADATA);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(metadata)))) {

            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String result = sb.toString();
            md.parseText(result);
        }
    }

    /**
     * Empty trace test
     *
     * @throws CTFException
     *             will not happen
     */
    @Test
    public void testEmptyStream() throws CTFException {
        try (CTFTraceReader reader = new CTFTraceReader(fixture);) {
            assertNull(reader.getCurrentEventDef());
        }
    }

    /**
     * Add a stream
     *
     * @throws CTFException
     *             should not happen
     */
    @Test
    public void testAddStream() throws CTFException {
        File stream = new File(fPathName + File.separator + "channel1");
        try (CTFTraceReader reader = new CTFTraceReader(fixture);) {
            fixture.addStreamFile(stream);
            reader.update();
            assertTrue(reader.advance());
            assertNotNull(reader.getCurrentEventDef());
        }
    }

    /**
     * Adds two a stream
     *
     * @throws CTFException
     *             should not happen
     */
    @Test
    public void testAddTwoStreams1() throws CTFException {
        File stream = new File(fPathName + File.separator + "channel1");
        try (CTFTraceReader reader = new CTFTraceReader(fixture);) {
            fixture.addStreamFile(stream);
            stream = new File(fPathName + File.separator + "channel2");
            fixture.addStreamFile(stream);
            reader.update();
            assertTrue(reader.advance());
            IEventDefinition currentEventDef = reader.getCurrentEventDef();
            assertNotNull(reader.getCurrentEventDef());
            assertEquals(16524l, currentEventDef.getTimestamp());
        }
    }

    /**
     * Adds two a stream
     *
     * @throws CTFException
     *             should not happen
     */
    @Test
    public void testAddTwoStreams2() throws CTFException {
        File stream = new File(fPathName + File.separator + "channel1");
        try (CTFTraceReader reader = new CTFTraceReader(fixture);) {
            fixture.addStreamFile(stream);
            stream = new File(fPathName + File.separator + "channel2");
            reader.update();
            assertTrue(reader.advance());
            fixture.addStreamFile(stream);
            reader.update();
            assertTrue(reader.advance());
            IEventDefinition currentEventDef = reader.getCurrentEventDef();
            assertNotNull(currentEventDef);
            assertEquals(223096L, currentEventDef.getTimestamp());
        }
    }

    /**
     * Tests that update does not change the position
     *
     * @throws CTFException
     *             should not happen
     */
    @Test
    public void testAddTwoStreams3() throws CTFException {
        File stream = new File(fPathName + File.separator + "channel1");
        try (CTFTraceReader reader = new CTFTraceReader(fixture);) {
            fixture.addStreamFile(stream);
            stream = new File(fPathName + File.separator + "channel2");
            reader.update();
            reader.update();
            reader.update();
            assertTrue(reader.advance());
            fixture.addStreamFile(stream);
            reader.update();
            reader.update();
            reader.update();
            reader.update();
            assertTrue(reader.advance());
            IEventDefinition currentEventDef = reader.getCurrentEventDef();
            assertNotNull(currentEventDef);
            assertEquals(223096L, currentEventDef.getTimestamp());
        }
    }

    /**
     * Test adding a bad stream
     *
     * @throws CTFException
     *             should not happen
     */
    @Test
    public void testAddStreamFail() throws CTFException {
        File stream = new File(fPathName + File.separator + METADATA);
        try (CTFTraceReader reader = new CTFTraceReader(fixture);) {
            fixture.addStreamFile(stream);
            assertNull(reader.getCurrentEventDef());
        }
    }

}
