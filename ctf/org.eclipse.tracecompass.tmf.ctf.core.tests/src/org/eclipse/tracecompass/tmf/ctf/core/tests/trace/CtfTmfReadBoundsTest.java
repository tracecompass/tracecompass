/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.Test;

/**
 * Test the methods to read the start and end times from a CtfTmfTrace trace without reading
 * the entire trace or indexing it.
 *
 * @author Loic Prieur-Drevon
 *
 */
public class CtfTmfReadBoundsTest {

    /**
     * Test that the methods for reading the trace without indexing it return
     * the right values.
     */
    @Test
    public void testRapidBounds() {
        CtfTmfTrace trace = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.TRACE2);
        trace.init(trace.getPath());
        try {
            ITmfTimestamp end = trace.readEnd();
            assertNotNull("Failed to read CtfTmfTrace end time", end);
            assertEquals(1331668259053641544L, end.toNanos());
        } finally {
            trace.dispose();
        }
    }

}
