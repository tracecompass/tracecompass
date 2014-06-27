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
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.eclipse.linuxtools.pcap.core.packet.BadPacketException;
import org.eclipse.linuxtools.pcap.core.tests.shared.PcapTestTrace;
import org.eclipse.linuxtools.pcap.core.trace.BadPcapFileException;
import org.eclipse.linuxtools.pcap.core.trace.PcapFile;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.junit.Test;

/**
 * Tests for performance regressions of the pcap reader. It only tests the pcap
 * reader, not tmf. <br>
 * This test runs in 3 passes.
 * <ul>
 * <li>first it opens a trace</li>
 * <li>then it reads the trace completely</li>
 * <li>then it randomly (seeded) seeks NB_SEEKS locations in the trace and reads
 * one event at each position.</li>
 * </ul>
 * <li>Note: We should make more seeks, since the current number is just too
 * fast.</li>
 *
 * @author Vincent Perot
 */
public class PcapSeekBenchmark {

    private static final Random RND = new Random(1000);

    private static final int LOOP_COUNT = 25;
    private static final int NB_SEEKS = 500;
    private static final String TEST_SUITE_NAME = "Pcap Read & Seek Benchmark (" + NB_SEEKS + " seeks)";
    private static final String TEST_ID = "org.eclipse.linuxtools#" + TEST_SUITE_NAME;

    /**
     * Run the benchmark scenario for the pcap trace.
     */
    @Test
    public void testPcapTrace() {
        readAndSeekTrace(PcapTestTrace.MOSTLY_UDP, "trace-pcap", true);
    }

    private static void readAndSeekTrace(PcapTestTrace testTrace, String testName, boolean inGlobalSummary) {
        assumeTrue(testTrace.exists());

        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + '#' + testName);
        perf.tagAsSummary(pm, TEST_SUITE_NAME + ':' + testName, Dimension.CPU_TIME);

        if (inGlobalSummary) {
            perf.tagAsGlobalSummary(pm, TEST_SUITE_NAME + ':' + testName, Dimension.CPU_TIME);
        }

        for (int loop = 0; loop < LOOP_COUNT; loop++) {
            try (PcapFile trace = testTrace.getTrace()) {
                trace.seekPacket(0);

                /* Read the whole trace to find out the number of packets */
                long nbPackets = trace.getTotalNbPackets();

                /* Generate the timestamps we will seek to */
                List<Long> seekTimestamps = new LinkedList<>();
                final long range = nbPackets;
                for (int i = 0; i < NB_SEEKS; i++) {
                    long rank = (RND.nextLong() % range);
                    if (rank < 0) {
                        // This is needed since modulus can return a negative
                        // number.
                        rank += range;
                    }
                    seekTimestamps.add(rank);
                }

                /* Benchmark seeking to the generated timestamps */
                pm.start();
                for (Long rank : seekTimestamps) {
                    trace.seekPacket(rank);
                    trace.parseNextPacket();
                }
                pm.stop();

            } catch (IOException | BadPcapFileException | BadPacketException e) {
                /* Should not happen if assumeTrue() passed above */
                fail("Test failed at iteration " + loop + ':' + e.getMessage());
            }
        }
        pm.commit();
    }
}
