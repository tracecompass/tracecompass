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
import static org.junit.Assume.assumeTrue;

import org.eclipse.linuxtools.lttng2.kernel.core.event.matching.TcpEventMatching;
import org.eclipse.linuxtools.lttng2.kernel.core.event.matching.TcpLttngEventMatching;
import org.eclipse.linuxtools.tmf.core.event.matching.TmfEventMatching;
import org.eclipse.linuxtools.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.linuxtools.tmf.core.synchronization.SynchronizationAlgorithm;
import org.eclipse.linuxtools.tmf.core.synchronization.TimestampTransformFactory;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.ctf.core.tests.shared.CtfTmfTestTrace;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for experiment syncing
 *
 * @author Geneviève Bastien
 */
@SuppressWarnings("nls")
public class ExperimentSyncTest {

    private static final String EXPERIMENT = "MyExperiment";
    private static int BLOCK_SIZE = 1000;

    /**
     * Initialize some data
     */
    @BeforeClass
    public static void setUp() {
        TmfEventMatching.registerMatchObject(new TcpEventMatching());
        TmfEventMatching.registerMatchObject(new TcpLttngEventMatching());
    }

    /**
     * Testing experiment synchronization
     */
    @Test
    public void testExperimentSync() {
        assumeTrue(CtfTmfTestTrace.SYNC_SRC.exists());
        assumeTrue(CtfTmfTestTrace.SYNC_DEST.exists());
        try (CtfTmfTrace trace1 = CtfTmfTestTrace.SYNC_SRC.getTrace();
                CtfTmfTrace trace2 = CtfTmfTestTrace.SYNC_DEST.getTrace();) {

            ITmfTrace[] traces = { trace1, trace2 };
            TmfExperiment experiment = new TmfExperiment(traces[0].getEventType(), EXPERIMENT, traces, BLOCK_SIZE);

            SynchronizationAlgorithm syncAlgo = experiment.synchronizeTraces(true);

            ITmfTimestampTransform tt1 = syncAlgo.getTimestampTransform(trace1);
            ITmfTimestampTransform tt2 = syncAlgo.getTimestampTransform(trace2);

            trace1.setTimestampTransform(tt1);
            trace2.setTimestampTransform(tt2);

            assertEquals("TmfTimestampLinear [ slope = 0.9999413783703139011056845831168394, offset = 79796507913179.33347660124688298171 ]", tt1.toString());
            assertEquals(TimestampTransformFactory.getDefaultTransform(), tt2);

            assertEquals(syncAlgo.getTimestampTransform(trace1.getHostId()), trace1.getTimestampTransform());
            assertEquals(syncAlgo.getTimestampTransform(trace2.getHostId()), trace2.getTimestampTransform());

        }
    }

    /**
     * Testing synchronization with 3 traces, one of which synchronizes with
     * both other
     */
    @Test
    public void testDjangoExperimentSync() {
        assumeTrue(CtfTmfTestTrace.DJANGO_CLIENT.exists());
        assumeTrue(CtfTmfTestTrace.DJANGO_DB.exists());
        assumeTrue(CtfTmfTestTrace.DJANGO_HTTPD.exists());
        try (CtfTmfTrace trace1 = CtfTmfTestTrace.DJANGO_CLIENT.getTrace();
                CtfTmfTrace trace2 = CtfTmfTestTrace.DJANGO_DB.getTrace();
                CtfTmfTrace trace3 = CtfTmfTestTrace.DJANGO_HTTPD.getTrace();) {
            ITmfTrace[] traces = { trace1, trace2, trace3 };
            TmfExperiment experiment = new TmfExperiment(traces[0].getEventType(), EXPERIMENT, traces, BLOCK_SIZE);

            SynchronizationAlgorithm syncAlgo = experiment.synchronizeTraces(true);

            ITmfTimestampTransform tt1 = syncAlgo.getTimestampTransform(trace1);
            ITmfTimestampTransform tt2 = syncAlgo.getTimestampTransform(trace2);
            ITmfTimestampTransform tt3 = syncAlgo.getTimestampTransform(trace3);

            trace1.setTimestampTransform(tt1);
            trace2.setTimestampTransform(tt2);
            trace3.setTimestampTransform(tt3);

            assertEquals(TimestampTransformFactory.getDefaultTransform(), tt1);
            assertEquals("TmfTimestampLinear [ slope = 0.9999996313017589597204633828681240, offset = 498490309972.0038068817738527724192 ]", tt2.toString());
            assertEquals("TmfTimestampLinear [ slope = 1.000000119014882262265342419815932, offset = -166652893534.6189900382736187431134 ]", tt3.toString());

        }
    }
}
