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

import org.eclipse.linuxtools.lttng2.kernel.core.event.matching.TcpEventMatching;
import org.eclipse.linuxtools.lttng2.kernel.core.event.matching.TcpLttngEventMatching;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.matching.TmfEventMatching;
import org.eclipse.linuxtools.tmf.core.event.matching.TmfNetworkEventMatching;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
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
        CtfTmfTrace trace1 = CtfTmfTestTrace.SYNC_SRC.getTrace();
        CtfTmfTrace trace2 = CtfTmfTestTrace.SYNC_DEST.getTrace();

        CtfTmfTrace[] tracearr = new CtfTmfTrace[2];
        tracearr[0] = trace1;
        tracearr[1] = trace2;

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
    }

}
