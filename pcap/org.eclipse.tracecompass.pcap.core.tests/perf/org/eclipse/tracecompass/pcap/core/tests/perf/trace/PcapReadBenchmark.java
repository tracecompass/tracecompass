/*******************************************************************************
 * Copyright (c) 2014, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.pcap.core.tests.perf.trace;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;

import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.internal.pcap.core.packet.BadPacketException;
import org.eclipse.tracecompass.internal.pcap.core.packet.Packet;
import org.eclipse.tracecompass.internal.pcap.core.trace.BadPcapFileException;
import org.eclipse.tracecompass.internal.pcap.core.trace.PcapFile;
import org.eclipse.tracecompass.pcap.core.tests.shared.PcapTestTrace;
import org.junit.Test;

/**
 * Benchmark of the Pcap parser for reading a trace. Note: We should get a
 * bigger trace. One that has WAYYYY more events since this current trace is
 * just parsed too fast.
 *
 * @author Vincent Perot
 */
public class PcapReadBenchmark {

    private static final String TEST_SUITE_NAME = "Pcap Read Benchmark";
    private static final String TEST_ID = "org.eclipse.linuxtools#" + TEST_SUITE_NAME;
    private static final int LOOP_COUNT = 25;
    private static final int RUN_BETWEEN_COMMIT_COUNT = 15;

    /**
     * Benchmark reading the pcap trace
     */
    @Test
    public void testPcapTrace() {
        readTrace(PcapTestTrace.BENCHMARK_TRACE, "trace-pcap", true);
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
                for (int i = 0; i < RUN_BETWEEN_COMMIT_COUNT; i++) {
                    trace.seekPacket(0);
                    while (true) {
                        Packet packet = trace.parseNextPacket();
                        if (packet == null) {
                            break;
                        }
                        /* Do something with the packet because we are awesome */
                        packet.getPayload();
                    }
                }
            } catch (IOException | BadPcapFileException | BadPacketException e) {
                fail("Test failed at iteration " + loop + ':' + e.getMessage());
            }
            pm.stop();
        }
        pm.commit();
    }
}
