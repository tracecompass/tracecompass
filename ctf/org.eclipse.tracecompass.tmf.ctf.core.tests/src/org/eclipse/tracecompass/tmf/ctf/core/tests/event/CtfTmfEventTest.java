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

import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.ctf.core.trace.iterator.CtfIterator;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEventFactory;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
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

    private static final String VALID_FIELD = "ret";

    private static final @NonNull CtfTestTrace testTrace = CtfTestTrace.KERNEL;

    /**
     * <pre>
     * babeltrace output :
     * [11:24:42.440133097] (+?.?????????) sys_socketcall: { cpu_id = 1 }, { call = 17, args = 0xB7555F30 }
     * [11:24:42.440137077] (+0.000003980) exit_syscall: { cpu_id = 1 }, { ret = 4132 }
     * </pre>
     */

    private static CtfTmfEvent nullEvent;
    private static CtfTmfEvent fixture;

    /**
     * Perform pre-test initialization.
     */
    @BeforeClass
    public static void setUp() {
        CtfTmfTrace trace = CtfTmfTestTraceUtils.getTrace(testTrace);
        try (CtfIterator tr = (CtfIterator) trace.createIterator();) {
            tr.advance();
            fixture = tr.getCurrentEvent();
            nullEvent = CtfTmfEventFactory.getNullEvent(trace);
        }
        trace.dispose();
    }

    /**
     * Test the timestamps
     */
    @Test
    public void testTimestamp() {
        assertEquals("Offsetted scaled timestamp", 1332170682440137077L, fixture.getTimestamp().toNanos());
        assertEquals("Raw timestamp", 4277198423090L, fixture.getUnscaledTime());
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
        ITmfEventField result = fixture.getContent().getField(VALID_FIELD);

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
        String[] names = { VALID_FIELD };
        assertNotNull(fixture.getContent().getField(names));

        /* First field exists, not the second */
        String[] names2 = { VALID_FIELD, "abcd" };
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
        CtfTmfTrace trace = fixture.getTrace();
        assertEquals("kernel", trace.getName());

        String reference = fixture.getChannel();
        int cpu = fixture.getCPU();
        ITmfEventType type = fixture.getType();
        assertEquals(ITmfContext.UNKNOWN_RANK, rank);

        assertEquals("channel0_1", reference);
        assertEquals(1, cpu);
        assertEquals("exit_syscall", type.toString());
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
        assertEquals("ret=4132", s);
    }

    /**
     * Test the {@link CtfTmfEventFactory#getNullEvent} method, and the
     * nullEvent's values.
     */
    @Test
    public void testNullEvent() {
        CtfTmfEvent nullEvent2 = CtfTmfEventFactory.getNullEvent(fixture.getTrace());
        assertEquals(nullEvent2, nullEvent);
        assertNotNull(nullEvent);
        assertEquals(-1, nullEvent.getCPU());
        assertEquals("Empty CTF event", nullEvent.getType().getName());
        assertEquals("", nullEvent.getChannel());
        assertEquals(0, nullEvent.getContent().getFields().size());
        assertEquals(-1L, nullEvent.getTimestamp().getValue());
    }
}
