/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.pcap.core.tests.perf.trace;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;

import org.eclipse.linuxtools.pcap.core.packet.BadPacketException;
import org.eclipse.linuxtools.pcap.core.packet.Packet;
import org.eclipse.linuxtools.pcap.core.tests.shared.PcapTestTrace;
import org.eclipse.linuxtools.pcap.core.trace.BadPcapFileException;
import org.eclipse.linuxtools.pcap.core.trace.PcapFile;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.junit.Test;


/**
 * Benchmark of the Pcap parser for reading a trace. Note: We should get a bigger trace. One
 * that has WAYYYY more events since this current trace is just parsed too fast.
 *
 * @author Vincent Perot
 */
public class PcapReadBenchmark {

    private static final String TEST_SUITE_NAME = "Pcap Read Benchmark";
    private static final String TEST_ID = "org.eclipse.linuxtools#" + TEST_SUITE_NAME;
    private static final int LOOP_COUNT = 25;

    /**
     * Benchmark reading the pcap trace
     */
    @Test
    public void testPcapTrace() {
        readTrace(PcapTestTrace.MOSTLY_UDP, "trace-pcap", true);
    }

    private static void readTrace(PcapTestTrace testTrace, String testName, boolean inGlobalSummary) {
        assumeTrue(testTrace.exists());

        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + '#' + testName);
        perf.tagAsSummary(pm, TEST_SUITE_NAME + ':' + testName, Dimension.CPU_TIME);

        if (inGlobalSummary) {
            perf.tagAsGlobalSummary(pm, TEST_SUITE_NAME + ':' + testName, Dimension.CPU_TIME);
        }

        for (int loop = 0; loop < LOOP_COUNT; loop++) {
            pm.start();
            try (PcapFile trace = testTrace.getTrace();) {
                trace.seekPacket(0);
                while (trace.hasNextPacket()) {
                    Packet packet = trace.parseNextPacket();
                    if (packet == null) {
                        fail("Test failed at iteration " + loop + " packet " + trace.getCurrentRank());
                        return;
                    }
                    /* Do something with the packet because we are awesome */
                    packet.getPayload();
                }

            } catch (IOException | BadPcapFileException | BadPacketException e) {
                fail("Test failed at iteration " + loop + ':' + e.getMessage());
            }
            pm.stop();
        }
        pm.commit();
    }
}
