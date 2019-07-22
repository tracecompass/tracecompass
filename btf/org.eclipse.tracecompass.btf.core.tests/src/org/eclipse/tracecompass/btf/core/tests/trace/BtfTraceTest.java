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
package org.eclipse.tracecompass.btf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.tracecompass.btf.core.event.BtfEvent;
import org.eclipse.tracecompass.btf.core.tests.utils.BtfTestTrace;
import org.eclipse.tracecompass.btf.core.trace.BtfColumnNames;
import org.eclipse.tracecompass.btf.core.trace.BtfTrace;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TraceValidationStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Best trace format test cases
 *
 * @author Matthew Khouzam
 */
public class BtfTraceTest {

    // Semaphore Event with notes
    private static final long START_TIME = 1392809960000000000L;
    private static final String[] ECXPECT_CONTENT_EVENT_0 = { String.valueOf(START_TIME), "SEM_DataElement1", "0", "SEM_DataElement1", "0", "Semaphore", "ready", "0" };

    // Scheduler event with notes
    private static final int EVENT_RANK_10 = 10;
    private static final String[] ECXPECT_CONTENT_EVENT_10 = { String.valueOf(START_TIME), "TASK_10MS_DL2", "0", "SCHED_Tasks_C1", "-1", "Scheduler", "processactivate", "some note" };

    // Task event without notes
    private static final int EVENT_RANK_COMMENT_TEST = 98;
    private static final long EVENT_COMMENT_TIME = 1392809960000956750L;
    private static final String[] ECXPECT_CONTENT_COMMENT_EVENT = { String.valueOf(EVENT_COMMENT_TIME), "Core_2", "0", "TASK_5MS", "0", "Task", "terminate", "" };

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
        fixture.dispose();
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
        validateCommentEvent(ECXPECT_CONTENT_EVENT_0, fixture.getNext(ctx));
    }

    /**
     * Read the first event
     */
    @Test
    public void testRead1stEvent() {
        ITmfContext ctx = fixture.seekEvent(0);
        ITmfEvent event = fixture.getNext(ctx);
        assertNotNull(event);
        validateCommentEvent(ECXPECT_CONTENT_EVENT_0, event);
    }

    /**
     * Read the tenth event
     */
    @Test
    public void testRead10thEvent1() {
        ITmfContext ctx = fixture.seekEvent(EVENT_RANK_10);
        ITmfEvent event = fixture.getNext(ctx);
        assertNotNull(event);
        validateCommentEvent(ECXPECT_CONTENT_EVENT_10, event);
    }

    /**
     * Read the tenth event without seeking
     */
    @Test
    public void testRead10thEvent2() {
        ITmfContext ctx = fixture.seekEvent(0);
        ITmfEvent event = fixture.getNext(ctx);
        for (int i = 0; i < EVENT_RANK_10; i++) {
            event = fixture.getNext(ctx);
        }
        assertNotNull(event);
        validateCommentEvent(ECXPECT_CONTENT_EVENT_10, event);
    }

    /**
     * Read the event after comment line
     */
    @Test
    public void testReadCommentLineEvent1() {
        ITmfContext ctx = fixture.seekEvent(EVENT_RANK_COMMENT_TEST);
        ITmfEvent event = fixture.getNext(ctx);
        assertNotNull(event);
        validateCommentEvent(ECXPECT_CONTENT_COMMENT_EVENT, event);
    }

    /**
     * Read the event after comment line without seeking
     */
    @Test
    public void testReadCommentLineEvent2() {
        ITmfContext ctx = fixture.seekEvent(0);
        ITmfEvent event = fixture.getNext(ctx);
        for (int i = 0; i < EVENT_RANK_COMMENT_TEST; i++) {
            event = fixture.getNext(ctx);
        }
        assertNotNull(event);
        validateCommentEvent(ECXPECT_CONTENT_COMMENT_EVENT, event);
    }

    /**
     * Read the trace properties
     */
    @Test
    public void testReadProperties() {
        Map<String, String> data = fixture.getProperties();
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

    private static void validateCommentEvent(String[] expected, ITmfEvent event) {
        assertTrue(event instanceof BtfEvent);
        BtfEvent btfEvent = (BtfEvent) event;
        List<String> actual = new ArrayList<>();
        actual.add(String.valueOf(event.getTimestamp().getValue()));
        actual.add(btfEvent.getSource());
        actual.add(event.getContent().getField(BtfColumnNames.SOURCE_INSTANCE.toString()).getValue().toString());
        actual.add(btfEvent.getTarget());
        actual.add(event.getContent().getField(BtfColumnNames.TARGET_INSTANCE.toString()).getValue().toString());
        actual.add(event.getType().getName());
        actual.add(event.getContent().getField(BtfColumnNames.EVENT.toString()).getValue().toString());
        String notes = event.getContent().getFieldValue(String.class, BtfColumnNames.NOTES.toString());
        actual.add(notes == null ? "" : notes);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(String.valueOf(i),  expected[i], actual.get(i));
        }
    }

}
