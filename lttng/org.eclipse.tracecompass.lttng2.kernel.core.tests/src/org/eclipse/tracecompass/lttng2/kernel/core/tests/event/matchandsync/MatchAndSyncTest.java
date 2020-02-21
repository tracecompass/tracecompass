/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.event.matchandsync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.event.matching.TcpEventMatching;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.event.matching.TcpLttngEventMatching;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.event.matching.IMatchProcessingUnit;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventMatching;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.Test;

/**
 * Tests for {@link TcpEventMatching}
 *
 * @author Geneviève Bastien
 */
@SuppressWarnings("nls")
public class MatchAndSyncTest {

    /**
     * Testing the packet matching
     */
    @Test
    public void testMatching() {
        CtfTmfTrace trace1 = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.SYNC_SRC);
        CtfTmfTrace trace2 = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.SYNC_DEST);

        List<@NonNull ITmfTrace> tracearr = new LinkedList<>();
        tracearr.add(trace1);
        tracearr.add(trace2);

        TmfEventMatching.registerMatchObject(new TcpEventMatching());
        TmfEventMatching.registerMatchObject(new TcpLttngEventMatching());

        TmfEventMatching twoTraceMatch = new TmfEventMatching(tracearr);
        assertTrue(twoTraceMatch.matchEvents());

        /* Set method and fields accessible to make sure the counts are ok */
        try {
            /* Verify number of matches */
            Method method = TmfEventMatching.class.getDeclaredMethod("getProcessingUnit");
            method.setAccessible(true);
            IMatchProcessingUnit procUnit = (IMatchProcessingUnit) method.invoke(twoTraceMatch);
            assertEquals(46, procUnit.countMatches());

        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            fail(e.getMessage());
        } finally {
            trace1.dispose();
            trace2.dispose();
        }
    }

}
