/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.event;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.ctf.core.trace.iterator.CtfIterator;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests making sure sign extension sign extension of field values works
 * correctly.
 *
 * See: https://bugs.eclipse.org/bugs/show_bug.cgi?id=491382
 *
 * @author Alexandre Montplaisir
 */
public class CtfTmfEventFieldSignExtensionTest {

    private static final @NonNull CtfTestTrace DEBUG_INFO_TRACE = CtfTestTrace.DEBUG_INFO3;

    private CtfTmfTrace trace;

    /**
     * Test setup
     */
    @Before
    public void setUp() {
        trace = CtfTmfTestTraceUtils.getTrace(DEBUG_INFO_TRACE);
    }

    /**
     * Test teardown
     */
    @After
    public void teardown() {
        if (trace != null) {
            trace.dispose();
        }
    }

    /**
     * Test that signed 8-byte integers are printed correctly.
     */
    @Test
    public void testUnsignedByte() {
        /*
         * Third event of the trace is printed like this by Babeltrace:
         *
         * [16:25:03.003427176] (+0.000001578) sonoshee lttng_ust_statedump:build_id:
         *      { cpu_id = 0 }, { ip = 0x7F3BBEDDDE1E, vpid = 3520 },
         *      { baddr = 0x400000, _build_id_length = 20, build_id = [ [0] = 0x1, [1] = 0xC6, [2] = 0x5, [3] = 0xBC, [4] = 0xF3, [5] = 0x8D, [6] = 0x6, [7] = 0x8D, [8] = 0x77, [9] = 0xA6, [10] = 0xE0, [11] = 0xA0, [12] = 0x2C, [13] = 0xED, [14] = 0xE6, [15] = 0xA5, [16] = 0xC, [17] = 0x57, [18] = 0x50, [19] = 0xB5 ] }
         */
        long[] expectedValues = new long[] {
                0x1,
                0xC6,
                0x5,
                0xBC,
                0xF3,
                0x8D,
                0x6,
                0x8D,
                0x77,
                0xA6,
                0xE0,
                0xA0,
                0x2C,
                0xED,
                0xE6,
                0xA5,
                0xC,
                0x57,
                0x50,
                0xB5
        };

        String expectedToString = LongStream.of(expectedValues)
                .mapToObj(i -> "0x" + Long.toHexString(i))
                .collect(Collectors.joining(", ", "build_id=[", "]"));

        try (CtfIterator iter = (CtfIterator) trace.createIterator();) {
            /* Go to third event */
            iter.advance();
            iter.advance();

            /* Retrieve the event's field called "build_id" */
            CtfTmfEvent event = iter.getCurrentEvent();
            ITmfEventField field = event.getContent().getField("build_id");
            long[] values = (long[]) field.getValue();

            assertArrayEquals(expectedValues, values);
            assertEquals(expectedToString, field.toString());
        }
    }
}
