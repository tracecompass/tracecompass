/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
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

package org.eclipse.linuxtools.tmf.ctf.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assume.assumeTrue;

import java.util.Collection;
import java.util.Set;

import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.ctf.core.CtfIterator;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfEventFactory;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.ctf.core.tests.shared.CtfTmfTestTrace;
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

    private static final CtfTmfTestTrace testTrace = CtfTmfTestTrace.KERNEL;

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
     *
     * @throws CTFReaderException
     *             error
     */
    @Before
    public void setUp() throws CTFReaderException {
        assumeTrue(testTrace.exists());
        try (CtfTmfTrace trace = testTrace.getTrace();
                CtfIterator tr = new CtfIterator(trace);) {
            tr.advance();
            fixture = tr.getCurrentEvent();
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
        assertNotNull(fixture.getContent().getSubField(names));

        /* First field exists, not the second */
        String[] names2 = { "pid", "abcd" };
        assertNull(fixture.getContent().getSubField(names2));

        /* Both field do not exist */
        String[] names3 = { "pfid", "abcd" };
        assertNull(fixture.getContent().getSubField(names3));

        /* TODO Missing case of embedded field, need event for it */
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
        try (CtfTmfTrace trace = fixture.getTrace();) {
            assertEquals("kernel", trace.getName());
        }
        String reference = fixture.getReference();
        String source = fixture.getSource();
        ITmfEventType type = fixture.getType();
        assertEquals(ITmfContext.UNKNOWN_RANK, rank);

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
        assertEquals("Empty CTF event", nullEvent.getType().getName());
        assertNull(nullEvent.getReference());
        assertEquals(0, nullEvent.getContent().getFields().size());
        assertEquals(-1L, nullEvent.getID());
        assertEquals(-1L, nullEvent.getTimestamp().getValue());
    }
}
