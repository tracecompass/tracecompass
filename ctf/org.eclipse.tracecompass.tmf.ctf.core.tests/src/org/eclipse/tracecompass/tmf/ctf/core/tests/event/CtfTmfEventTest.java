/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial generation with CodePro tools
 *   Alexandre Montplaisir - Clean up, consolidate redundant tests
 *   Patrick Tasse - Remove getSubField
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assume.assumeTrue;

import java.util.Collection;
import java.util.Set;

import org.eclipse.tracecompass.internal.tmf.ctf.core.trace.iterator.CtfIterator;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEventFactory;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
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

    private static final CtfTmfTestTrace testTrace = CtfTmfTestTrace.KERNEL;

    private static CtfTmfEvent nullEvent;
    private CtfTmfEvent fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        assumeTrue(testTrace.exists());
        try (CtfTmfTrace trace = testTrace.getTrace();
                CtfIterator tr = (CtfIterator) trace.createIterator();) {
            tr.advance();
            fixture = tr.getCurrentEvent();
            nullEvent = CtfTmfEventFactory.getNullEvent(trace);
        }
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
        String result = nullEvent.getType().getName();
        assertEquals("Empty CTF event", result);
    }

    /**
     * Run the ArrayList<String> getFieldNames() method test.
     */
    @Test
    public void testGetFieldNames() {
        Collection<String> result = fixture.getContent().getFieldNames();
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
        Collection<? extends ITmfEventField> fields = nullEvent.getContent().getFields();
        assertEquals(0, fields.size());
    }

    /**
     * Run the ITmfEventField getSubFieldValue(String[]) method test.
     */
    @Test
    public void testGetSubFieldValue() {
        /* Field exists */
        String[] names = { "pid" };
        assertNotNull(fixture.getContent().getField(names));

        /* First field exists, not the second */
        String[] names2 = { "pid", "abcd" };
        assertNull(fixture.getContent().getField(names2));

        /* Both field do not exist */
        String[] names3 = { "pfid", "abcd" };
        assertNull(fixture.getContent().getField(names3));

        /* TODO Missing case of embedded field, need event for it */
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
        try (CtfTmfTrace trace = fixture.getTrace();) {
            assertEquals("kernel", trace.getName());
        }
        String reference = fixture.getChannel();
        int cpu = fixture.getCPU();
        ITmfEventType type = fixture.getType();
        assertEquals(ITmfContext.UNKNOWN_RANK, rank);

        assertEquals("channel0_1", reference);
        assertEquals(1, cpu);
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
     * Test the {@link CtfTmfEventFactory#getNullEvent(CtfTmfTrace)} method, and
     * the nullEvent's values.
     */
    @Test
    public void testNullEvent() {
        CtfTmfEvent nullEvent2 = CtfTmfEventFactory.getNullEvent(fixture.getTrace());
        assertSame(nullEvent2, nullEvent);
        assertNotNull(nullEvent);
        assertEquals(-1, nullEvent.getCPU());
        assertEquals("Empty CTF event", nullEvent.getType().getName());
        assertEquals("", nullEvent.getChannel());
        assertEquals(0, nullEvent.getContent().getFields().size());
        assertEquals(-1L, nullEvent.getTimestamp().getValue());
    }
}
