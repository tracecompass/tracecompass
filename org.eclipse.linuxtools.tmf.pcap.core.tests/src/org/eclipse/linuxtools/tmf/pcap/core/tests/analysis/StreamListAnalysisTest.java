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
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.pcap.core.tests.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import org.eclipse.linuxtools.pcap.core.tests.shared.PcapTestTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.pcap.core.analysis.StreamListAnalysis;
import org.eclipse.linuxtools.tmf.pcap.core.event.TmfPacketStreamBuilder;
import org.eclipse.linuxtools.tmf.pcap.core.protocol.TmfProtocol;
import org.eclipse.linuxtools.tmf.pcap.core.trace.PcapTrace;
import org.junit.Test;

/**
 * JUnit that test the StreamListAnalysis class.
 *
 * @author Vincent Perot
 */
public class StreamListAnalysisTest {

    /**
     * Method that tests the constructor.
     */
    @Test
    public void constructorTest() {
        try (StreamListAnalysis analysis = new StreamListAnalysis();) {
            analysis.setId(StreamListAnalysis.ID);
            for (TmfProtocol protocol : TmfProtocol.getAllProtocols()) {
                if (protocol.supportsStream()) {
                    assertNotNull(analysis.getBuilder(protocol));
                }
            }
            assertFalse(analysis.isFinished());
        }
    }

    /**
     * Method that tests canExecute().
     *
     * @throws TmfTraceException
     *             Thrown when the trace cannot be initialized. Fails the test.
     */
    @Test
    public void canExecuteTest() throws TmfTraceException {
        PcapTestTrace trace = PcapTestTrace.MOSTLY_TCP;
        assumeTrue(trace.exists());
        String path = trace.getPath();
        try (PcapTrace pcapTrace = new PcapTrace();
                StreamListAnalysis analysis = new StreamListAnalysis();) {
            analysis.setId(StreamListAnalysis.ID);
            pcapTrace.initTrace(null, path, null);
            assertTrue(analysis.canExecute(pcapTrace));
        }
    }

    /**
     * Method that execute the analysis and verify the results.
     *
     * @throws TmfAnalysisException
     *             Thrown when an analysis error occurs during the setup or
     *             execution. Fails the test.
     * @throws TmfTraceException
     *             Thrown when the trace cannot be initialized. Fails the test.
     */
    @Test
    public void executeAnalysisTest() throws TmfAnalysisException, TmfTraceException {
        PcapTestTrace trace = PcapTestTrace.MOSTLY_TCP;
        assumeTrue(trace.exists());
        String path = trace.getPath();
        try (PcapTrace pcapTrace = new PcapTrace();
                StreamListAnalysis analysis = new StreamListAnalysis();) {
            pcapTrace.initTrace(null, path, null);
            analysis.setId(StreamListAnalysis.ID);
            analysis.setTrace(pcapTrace);
            analysis.schedule();
            analysis.waitForCompletion();

            // Verify that builders are not empty.
            TmfPacketStreamBuilder builder = analysis.getBuilder(TmfProtocol.ETHERNET_II);
            if (builder == null) {
                fail("The PacketStreamBuilder is null!");
                return;
            }
            assertEquals(1, builder.getNbStreams());

            builder = analysis.getBuilder(TmfProtocol.IPV4);
            if (builder == null) {
                fail("The PacketStreamBuilder is null!");
                return;
            }
            assertEquals(3, builder.getNbStreams());

            builder = analysis.getBuilder(TmfProtocol.TCP);
            if (builder == null) {
                fail("The PacketStreamBuilder is null!");
                return;
            }
            assertEquals(2, builder.getNbStreams());

            builder = analysis.getBuilder(TmfProtocol.UDP);
            if (builder == null) {
                fail("The PacketStreamBuilder is null!");
                return;
            }
            assertEquals(1, builder.getNbStreams());
        }
    }

}
