/*******************************************************************************
 * Copyright (c) 2012-2013 Ericsson
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assume.assumeTrue;

import java.util.Set;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfIterator;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEventFactory;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTraces;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The class <code>CtfTmfEventTest</code> contains tests for the class
 * <code>{@link CtfTmfEvent}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class CtfTmfEventTest {

    private static final int TRACE_INDEX = 0;

    private static CtfTmfEvent nullEvent;
    private CtfTmfEvent fixture;

    /**
     * Test class initialization
     */
    @BeforeClass
    public static void initialize() {
        nullEvent = CtfTmfEventFactory.getNullEvent();
    }

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        assumeTrue(CtfTmfTestTraces.tracesExist());
        CtfTmfTrace trace = CtfTmfTestTraces.getTestTrace(TRACE_INDEX);
        CtfIterator tr = new CtfIterator(trace);
        tr.advance();
        fixture = tr.getCurrentEvent();
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
        int result = nullEvent.getCPU();
        assertEquals(-1, result);
    }

    /**
     * Run the String getEventName() method test.
     */
    @Test
    public void testGetEventName() {
        String result = nullEvent.getEventName();
        assertEquals("Empty CTF event", result);
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
        String fieldName = "pid";
        ITmfEventField result = fixture.getContent().getField(fieldName);

        assertNotNull(result);
        assertNotNull(result.getValue());
    }

    /**
     * Run the HashMap<String, CTFEventField> getFields() method test.
     */
    @Test
    public void testGetFields() {
        ITmfEventField[] fields = nullEvent.getContent().getFields();
        ITmfEventField[] fields2 = new ITmfEventField[0];
        assertArrayEquals(fields2, fields);
    }

    /**
     * Run the long getID() method test.
     */
    @Test
    public void testGetID() {
        long result = nullEvent.getID();
        assertEquals(-1L, result);
    }

    /**
     * Run the long getTimestamp() method test.
     */
    @Test
    public void testGetTimestamp() {
        long result = nullEvent.getTimestamp().getValue();
        assertEquals(-1L, result);
    }

    /**
     * Test the getters for the reference, source and type.
     */
    @Test
    public void testGetters() {
        long rank = fixture.getRank();
        CtfTmfTrace trace = fixture.getTrace();
        String reference = fixture.getReference();
        String source = fixture.getSource();
        ITmfEventType type = fixture.getType();
        assertEquals(ITmfContext.UNKNOWN_RANK, rank);
        assertEquals("test", trace.getName());
        assertEquals("channel0_1", reference);
        assertEquals("1", source);
        assertEquals("lttng_statedump_vm_map", type.toString());
    }

    /**
     * Test the custom CTF attributes methods. The test trace doesn't have any,
     * so the list of attributes should be empty.
     */
    @Test
    public void testCustomAttributes() {
        Set<String> attributes = fixture.listCustomAttributes();
        assertEquals(0, attributes.size());

        String attrib = fixture.getCustomAttribute("bozo");
        assertNull(attrib);
    }

    /**
     * Test the toString() method
     */
    @Test
    public void testToString() {
        String s = fixture.getContent().toString();
        assertEquals("pid=1922, start=0xb73ea000, end=0xb73ec000, flags=0x8000075, inode=917738, pgoff=0", s);
    }

    /**
     * Test the {@link CtfTmfEventFactory#getNullEvent()} method, and the
     * nullEvent's values.
     */
    @Test
    public void testNullEvent() {
        CtfTmfEvent nullEvent2 = CtfTmfEventFactory.getNullEvent();
        assertSame(nullEvent2, nullEvent);
        assertNotNull(nullEvent);
        assertEquals(-1, nullEvent.getCPU());
        assertEquals("Empty CTF event", nullEvent.getEventName());
        assertEquals("No stream", nullEvent.getReference());
        assertArrayEquals(new ITmfEventField[0], nullEvent.getContent().getFields());
        assertEquals(-1L, nullEvent.getID());
        assertEquals(-1L, nullEvent.getTimestamp().getValue());
    }
}
