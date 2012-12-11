/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial generation with CodePro tools
 *   Alexandre Montplaisir - Clean up, consolidate redundant tests
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>CtfTmfEventTest</code> contains tests for the class
 * <code>{@link CtfTmfEvent}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class CtfTmfEventTest {

    private CtfTmfEvent fixture;

    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(CtfTmfEventTest.class);
    }

    /**
     * Perform pre-test initialization.
     *
     * @throws TmfTraceException
     *             If the test trace is not found
     */
    @Before
    public void setUp() throws TmfTraceException {
        CtfTmfTrace trace = TestParams.createTrace();
        CtfIterator tr = new CtfIterator(trace);
        tr.advance();
        fixture = tr.getCurrentEvent();
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the CTFEvent(EventDefinition,StreamInputReader) constructor test.
     */
    @Test
    public void testCTFEvent_read() {
        assertNotNull(fixture);
    }

    /**
     * Run the int getCPU() method test.
     */
    @Test
    public void testGetCPU() {
        CtfTmfEvent nullEvent = CtfTmfEvent.getNullEvent();
        int result = nullEvent.getCPU();

        assertEquals(-1, result);
    }

    /**
     * Run the String getChannelName() method test.
     */
    @Test
    public void testGetChannelName() {
        CtfTmfEvent nullEvent = CtfTmfEvent.getNullEvent();
        String result = nullEvent.getChannelName();

        assertEquals("No stream", result); //$NON-NLS-1$
    }

    /**
     * Run the String getEventName() method test.
     */
    @Test
    public void testGetEventName() {
        CtfTmfEvent nullEvent = CtfTmfEvent.getNullEvent();
        String result = nullEvent.getEventName();

        assertEquals("Empty CTF event", result); //$NON-NLS-1$
    }

    /**
     * Run the ArrayList<String> getFieldNames() method test.
     */
    @Test
    public void testGetFieldNames() {
        String[] result = fixture.getContent().getFieldNames();
        assertNotNull(result);
    }

    /**
     * Run the Object getFieldValue(String) method test.
     */
    @Test
    public void testGetFieldValue() {
        String fieldName = "pid"; //$NON-NLS-1$
        ITmfEventField result = fixture.getContent().getField(fieldName);

        assertNotNull(result);
        assertNotNull(result.getValue());
    }

    /**
     * Run the HashMap<String, CTFEventField> getFields() method test.
     */
    @Test
    public void testGetFields() {
        CtfTmfEvent nullEvent = CtfTmfEvent.getNullEvent();
        ITmfEventField[] fields = nullEvent.getContent().getFields();
        ITmfEventField[] fields2 = new ITmfEventField[0];
        assertArrayEquals(fields2, fields);
    }

    /**
     * Run the long getID() method test.
     */
    @Test
    public void testGetID() {
        CtfTmfEvent nullEvent = CtfTmfEvent.getNullEvent();
        long result = nullEvent.getID();

        assertEquals(-1L, result);
    }

    /**
     * Test the clone method
     */
    @Test
    public void testClone() {
        CtfTmfEvent other = CtfTmfEvent.getNullEvent().clone();
        assertNotNull(other);
    }

    /**
     * Run the CTFEvent getNullEvent() method test.
     */
    @Test
    public void testGetNullEvent() {
        CtfTmfEvent nullEvent = CtfTmfEvent.getNullEvent();

        assertNotNull(nullEvent);
        assertEquals(-1, nullEvent.getCPU());
        assertEquals("Empty CTF event", nullEvent.getEventName()); //$NON-NLS-1$
        assertEquals("No stream", nullEvent.getChannelName()); //$NON-NLS-1$
        assertArrayEquals(new ITmfEventField[0], nullEvent.getContent().getFields());
        assertEquals(-1L, nullEvent.getID());
        assertEquals(-1L, nullEvent.getTimestamp().getValue());
    }

    /**
     * Run the long getTimestamp() method test.
     *
     */
    @Test
    public void testGetTimestamp() {
        CtfTmfEvent nullEvent = CtfTmfEvent.getNullEvent();
        long result = nullEvent.getTimestamp().getValue();

        assertEquals(-1L, result);
    }

    /**
     * Test the getters for the channel name, reference, source and type.
     */
    @Test
    public void testGetters() {
        long rank = fixture.getRank();
        CtfTmfTrace trace = fixture.getTrace();
        String channelName = fixture.getChannelName();
        String reference = fixture.getReference();
        String source = fixture.getSource();
        ITmfEventType type = fixture.getType();
        assertEquals(rank, 0);
        assertEquals(trace.getName(), "test"); //$NON-NLS-1$
        assertEquals(channelName, "channel0_1"); //$NON-NLS-1$
        assertEquals(reference,"channel0_1"); //$NON-NLS-1$
        assertEquals(source, "1"); //$NON-NLS-1$
        assertEquals(type.toString(), "lttng_statedump_vm_map"); //$NON-NLS-1$
    }

    /**
     * Test the toString() method
     */
    @Test
    public void testToString() {
        String s = fixture.getContent().toString();
        assertEquals("pid=1922, inode=917738, flags=0x8000075, end=0xb73ec000, start=0xb73ea000, pgoff=0", s); //$NON-NLS-1$
    }
}
