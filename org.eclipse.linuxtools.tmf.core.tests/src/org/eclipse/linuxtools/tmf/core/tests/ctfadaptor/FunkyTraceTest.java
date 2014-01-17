/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfEnumPair;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

/**
 * More advanced CTF tests using "funky_trace", a trace generated with the
 * Babeltrace CTF writer API, which has lots of fun things like different
 * integer/float sizes and non-standard struct alignments.
 *
 * @author Alexandre Montplaisir
 */
public class FunkyTraceTest {

    /** Time-out tests after 20 seconds */
    @Rule
    public TestRule globalTimeout= new Timeout(20000);

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static final CtfTmfTestTrace testTrace = CtfTmfTestTrace.FUNKY_TRACE;
    private static final double DELTA = 0.0000001;

    private static CtfTmfTrace fTrace;

    // ------------------------------------------------------------------------
    // Setup
    // ------------------------------------------------------------------------

    /**
     * Test setup
     */
    @BeforeClass
    public static void setupClass() {
        assumeTrue(testTrace.exists());
        fTrace = testTrace.getTrace();
        fTrace.indexTrace(true);
    }

    /**
     * Clean-up
     */
    @AfterClass
    public static void tearDownClass() {
        fTrace.dispose();
    }

    // ------------------------------------------------------------------------
    // Test methods
    // ------------------------------------------------------------------------

    /**
     * Verify the contents of the first event
     */
    @Test
    public void testFirstEvent() {
        CtfTmfEvent event = getEvent(0);
        assertEquals("Simple Event", event.getType().getName());
        assertEquals(1234567, event.getTimestamp().getValue());
        assertEquals(42, ((Long) event.getContent().getField("integer_field").getValue()).intValue());
        assertEquals(3.1415, ((Double) event.getContent().getField("float_field").getValue()).doubleValue(), DELTA);
    }

    /**
     * Verify the contents of the second event (the first "spammy event")
     */
    @Test
    public void testSecondEvent() {
        CtfTmfEvent event = getEvent(1);
        assertEquals("Spammy_Event", event.getType().getName());
        assertEquals(1234568, event.getTimestamp().getValue());
        assertEquals(0, ((Long) event.getContent().getField("field_1").getValue()).intValue());
        assertEquals("This is a test", event.getContent().getField("a_string").getValue());
    }

    /**
     * Verify the contents of the last "spammy event"
     */
    @Test
    public void testSecondToLastEvent() {
        CtfTmfEvent event = getEvent(100000);
        assertEquals("Spammy_Event", event.getType().getName());
        assertEquals(1334567, event.getTimestamp().getValue());
        assertEquals(99999, ((Long) event.getContent().getField("field_1").getValue()).intValue());
        assertEquals("This is a test", event.getContent().getField("a_string").getValue());
    }

    /**
     * Verify the contents of the last, complex event
     */
    @Test
    public void testLastEvent() {
        /*
         * Last event as seen in Babeltrace:
         * [19:00:00.001334568] (+0.000000001) Complex Test Event: { }, {
         *     uint_35 = 0xDDF00D,
         *     int_16 = -12345,
         *     complex_structure = {
         *         variant_selector = ( INT16_TYPE : container = 1 ),
         *         a_string = "Test string",
         *         variant_value = { INT16_TYPE = -200 },
         *         inner_structure = {
         *             seq_len = 0xA,
         *             a_sequence = [ [0] = 4, [1] = 3, [2] = 2, [3] = 1, [4] = 0, [5] = -1, [6] = -2, [7] = -3, [8] = -4, [9] = -5 ]
         *         }
         *     }
         * }
         */

        CtfTmfEvent event = getEvent(100001);
        assertEquals("Complex Test Event", event.getType().getName());
        assertEquals(1334568, event.getTimestamp().getValue());
        assertEquals(0xddf00d, ((Long) event.getContent().getField("uint_35").getValue()).intValue());
        assertEquals(-12345, ((Long) event.getContent().getField("int_16").getValue()).intValue());

        ITmfEventField[] complexStruct =
                (ITmfEventField[]) event.getContent().getField("complex_structure").getValue();

        assertEquals("variant_selector", complexStruct[0].getName());
        CtfEnumPair variant1 = (CtfEnumPair) complexStruct[0].getValue();
        assertEquals("INT16_TYPE", variant1.getStringValue());
        assertEquals(Long.valueOf(1), variant1.getLongValue());

        assertEquals("a_string", complexStruct[1].getName());
        assertEquals("Test string", complexStruct[1].getValue());

        assertEquals("variant_value", complexStruct[2].getName());
        ITmfEventField variantField = (ITmfEventField) complexStruct[2].getValue();
        assertEquals("INT16_TYPE", variantField.getName());
        assertEquals(Long.valueOf(-200), variantField.getValue());

        ITmfEventField[] innerStruct = (ITmfEventField[]) complexStruct[3].getValue();

        assertEquals("seq_len", innerStruct[0].getName());
        assertEquals(Long.valueOf(10), innerStruct[0].getValue());

        assertEquals("a_sequence", innerStruct[1].getName());
        long[] seqValues = (long[]) innerStruct[1].getValue();
        long[] expectedValues = { 4, 3, 2, 1, 0, -1, -2, -3, -4, -5 };
        assertArrayEquals(expectedValues, seqValues);
    }

    // ------------------------------------------------------------------------
    // Private stuff
    // ------------------------------------------------------------------------

    private synchronized CtfTmfEvent getEvent(long index) {
        TestEventRequest req = new TestEventRequest(index);
        fTrace.sendRequest(req);
        try {
            req.waitForCompletion();
        } catch (InterruptedException e) {
            return null;
        }
        return req.getEvent();
    }

    private class TestEventRequest extends TmfEventRequest {

        private CtfTmfEvent fRetEvent = null;

        public TestEventRequest(long index) {
            super(CtfTmfEvent.class,
                    TmfTimeRange.ETERNITY,
                    index,
                    1,
                    ExecutionType.FOREGROUND);
        }

        @Override
        public void handleData(ITmfEvent event) {
            fRetEvent = (CtfTmfEvent) event;
        }

        public CtfTmfEvent getEvent() {
            return fRetEvent;
        }
    }

}
