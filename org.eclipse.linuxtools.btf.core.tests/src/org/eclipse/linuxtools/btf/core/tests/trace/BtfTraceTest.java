/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.btf.core.tests.trace;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map;

import org.eclipse.linuxtools.btf.core.tests.utils.BtfTestTrace;
import org.eclipse.linuxtools.btf.core.trace.BtfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TraceValidationStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Best trace format test cases
 *
 * @author Matthew Khouzam
 */
public class BtfTraceTest {

    private static final long START_TIME = 1392809960000000000L;

    private BtfTrace fixture;

    /**
     * Does the trace exist?
     */
    @Before
    public void setup() {
        fixture = BtfTestTrace.BTF_TEST.getTrace();
        assertNotNull(fixture);
    }

    /**
     * Cleanup
     */
    @After
    public void cleanup() {
        try {
            fixture.close();
        } catch (IOException e) {}
    }

    /**
     * Tests validation
     */
    @Test
    public void testValidate(){
        TraceValidationStatus status = (TraceValidationStatus) fixture.validate(null, fixture.getPath());
        assertNotNull(status);
        assertTrue(status.isOK());
        assertEquals(100, status.getConfidence());

    }

    /**
     * Seek the first event
     */
    @Test
    public void testSeek1stEvent() {
        ITmfContext ctx = fixture.seekEvent(0);
        assertNotNull(ctx);
        assertEquals(0, ctx.getRank());
        assertEquals(499L, ctx.getLocation().getLocationInfo());
    }

    /**
     * Read the first event
     */
    @Test
    public void testRead1stEvent() {
        ITmfContext ctx = fixture.seekEvent(0);
        ITmfEvent event = fixture.getNext(ctx);
        assertNotNull(event);
        assertEquals(START_TIME, event.getTimestamp().getValue());
    }

    /**
     * Read the tenth event
     */
    @Test
    public void testRead10thEvent1() {
        ITmfContext ctx = fixture.seekEvent(10);
        ITmfEvent event = fixture.getNext(ctx);
        assertNotNull(event);
        assertEquals(START_TIME, event.getTimestamp().getValue());
    }

    /**
     * Read the tenth event without seeking
     */
    @Test
    public void testRead10thEvent2() {
        ITmfContext ctx = fixture.seekEvent(0);
        ITmfEvent event = null;
        for (int i = 0; i < 10; i++) {
            event = fixture.getNext(ctx);
        }
        assertNotNull(event);
        assertEquals(START_TIME, event.getTimestamp().getValue());
    }

    /**
     * Read the trace properties
     */
    @Test
    public void testReadProperties() {
        Map<String, String> data = fixture.getTraceProperties();
        assertNotNull(data);
        assertEquals("ns", data.get("#timeScale"));
        assertEquals("2.1.0", data.get("#version"));
    }

    /**
     * Read two contexts
     */
    @Test
    public void testTwoContexts() {
        ITmfContext ctx0 = fixture.seekEvent(0);
        ITmfContext ctx1 = fixture.seekEvent(10);
        ITmfEvent event = null;
        for (int i = 0; i < 11; i++) {
            event = fixture.getNext(ctx0);
        }
        ITmfEvent event1 = fixture.getNext(ctx1);
        assertNotNull(event);
        assertNotNull(event1);
        assertEquals(event, event1);
    }
}
