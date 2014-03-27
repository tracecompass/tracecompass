/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.tests.event.matchandsync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.linuxtools.lttng2.kernel.core.event.matching.TcpEventMatching;
import org.eclipse.linuxtools.lttng2.kernel.core.event.matching.TcpLttngEventMatching;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.matching.TmfEventMatching;
import org.eclipse.linuxtools.tmf.core.event.matching.TmfNetworkEventMatching;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
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
        final String cr = System.getProperty("line.separator");
        assumeTrue(CtfTmfTestTrace.SYNC_SRC.exists());
        assumeTrue(CtfTmfTestTrace.SYNC_DEST.exists());
        CtfTmfTrace trace1 = CtfTmfTestTrace.SYNC_SRC.getTrace();
        CtfTmfTrace trace2 = CtfTmfTestTrace.SYNC_DEST.getTrace();

        List<ITmfTrace> tracearr = new LinkedList<>();
        tracearr.add(trace1);
        tracearr.add(trace2);

        TmfEventMatching.registerMatchObject(new TcpEventMatching());
        TmfEventMatching.registerMatchObject(new TcpLttngEventMatching());

        TmfNetworkEventMatching twoTraceMatch = new TmfNetworkEventMatching(tracearr);
        assertTrue(twoTraceMatch.matchEvents());

        String stats = twoTraceMatch.toString();
        assertEquals("TmfEventMatches [ Number of matches found: 46 ]" +
                "Trace 0:" + cr +
                "  3 unmatched incoming events" + cr +
                "  2 unmatched outgoing events" + cr +
                "Trace 1:" + cr +
                "  2 unmatched incoming events" + cr +
                "  1 unmatched outgoing events" + cr, stats);

        CtfTmfTestTrace.SYNC_SRC.dispose();
        CtfTmfTestTrace.SYNC_DEST.dispose();
    }

}
