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

package org.eclipse.linuxtools.ctf.core.tests.trace;

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

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.tests.shared.CtfTestTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader;
import org.eclipse.linuxtools.ctf.core.trace.Metadata;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for streaming support
 *
 * @author Matthew Khouzam
 *
 */
public class CTFTraceGrowingTest {
    final private String fPathName = CtfTestTrace.SYNTHETIC_TRACE.getPath();

    final private CTFTrace fixture = new CTFTrace();

    /**
     * Init
     *
     * @throws IOException
     *             an IO error
     * @throws FileNotFoundException
     *             file's not there
     * @throws CTFReaderException
     *             error in metadata
     */
    @Before
    public void init() throws FileNotFoundException, IOException, CTFReaderException {
        Metadata md = new Metadata(fixture);
        File metadata = new File(fPathName + "/" + "metadata");

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
     * @throws CTFReaderException
     *             will not happen
     */
    @Test
    public void testEmptyStream() throws CTFReaderException {
        CTFTraceReader reader = new CTFTraceReader(fixture);
        assertNull(reader.getCurrentEventDef());
    }

    /**
     * Add a stream
     *
     * @throws CTFReaderException
     *             should not happen
     */
    @Test
    public void testAddStream() throws CTFReaderException {
        File stream = new File(fPathName + "/" + "channel1");
        CTFTraceReader reader = new CTFTraceReader(fixture);
        fixture.addStreamFile(stream);
        reader.update();
        assertTrue(reader.advance());
        assertNotNull(reader.getCurrentEventDef());
    }

    /**
     * Adds two a stream
     *
     * @throws CTFReaderException
     *             should not happen
     */
    @Test
    public void testAddTwoStreams1() throws CTFReaderException {
        File stream = new File(fPathName + "/" + "channel1");
        CTFTraceReader reader = new CTFTraceReader(fixture);
        fixture.addStreamFile(stream);
        stream = new File(fPathName + "/" + "channel2");
        fixture.addStreamFile(stream);
        reader.update();
        assertTrue(reader.advance());
        EventDefinition currentEventDef = reader.getCurrentEventDef();
        assertNotNull(reader.getCurrentEventDef());
        assertEquals(16518l, currentEventDef.getTimestamp());
    }

    /**
     * Adds two a stream
     *
     * @throws CTFReaderException
     *             should not happen
     */
    @Test
    public void testAddTwoStreams2() throws CTFReaderException {
        File stream = new File(fPathName + "/" + "channel1");
        CTFTraceReader reader = new CTFTraceReader(fixture);
        fixture.addStreamFile(stream);
        stream = new File(fPathName + "/" + "channel2");
        reader.update();
        assertTrue(reader.advance());
        fixture.addStreamFile(stream);
        reader.update();
        assertTrue(reader.advance());
        EventDefinition currentEventDef = reader.getCurrentEventDef();
        assertNotNull(currentEventDef);
        assertEquals(223007L, currentEventDef.getTimestamp());
    }

    /**
     * Tests that update does not change the position
     *
     * @throws CTFReaderException
     *             should not happen
     */
    @Test
    public void testAddTwoStreams3() throws CTFReaderException {
        File stream = new File(fPathName + "/" + "channel1");
        CTFTraceReader reader = new CTFTraceReader(fixture);
        fixture.addStreamFile(stream);
        stream = new File(fPathName + "/" + "channel2");
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
        EventDefinition currentEventDef = reader.getCurrentEventDef();
        assertNotNull(currentEventDef);
        assertEquals(223007L, currentEventDef.getTimestamp());
    }

    /**
     * Test adding a bad stream
     *
     * @throws CTFReaderException
     *             should happen
     */
    @Test(expected = CTFReaderException.class)
    public void testAddStreamFail() throws CTFReaderException {
        File stream = new File(fPathName + "/" + "metadata");
        CTFTraceReader reader = new CTFTraceReader(fixture);
        fixture.addStreamFile(stream);
        assertNull(reader.getCurrentEventDef());
    }

}
