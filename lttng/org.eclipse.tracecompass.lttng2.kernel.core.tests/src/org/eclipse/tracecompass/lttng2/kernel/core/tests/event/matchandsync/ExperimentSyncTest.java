/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.event.matchandsync;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.eclipse.tracecompass.internal.lttng2.kernel.core.event.matching.TcpEventMatching;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.event.matching.TcpLttngEventMatching;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventMatching;
import org.eclipse.tracecompass.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.tracecompass.tmf.core.synchronization.SynchronizationAlgorithm;
import org.eclipse.tracecompass.tmf.core.synchronization.TimestampTransformFactory;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

/**
 * Tests for experiment syncing
 *
 * @author Geneviève Bastien
 */
@SuppressWarnings("nls")
public class ExperimentSyncTest {

    /** Timeout the tests after 2 minutes */
    @Rule
    public TestRule timeoutRule = new Timeout(2, TimeUnit.MINUTES);

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
        CtfTmfTrace trace1 = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.SYNC_SRC);
        CtfTmfTrace trace2 = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.SYNC_DEST);

        ITmfTrace[] traces = { trace1, trace2 };
        TmfExperiment experiment = new TmfExperiment(traces[0].getEventType(), EXPERIMENT, traces, BLOCK_SIZE, null);

        SynchronizationAlgorithm syncAlgo = experiment.synchronizeTraces(true);

        ITmfTimestampTransform tt1 = syncAlgo.getTimestampTransform(trace1);
        ITmfTimestampTransform tt2 = syncAlgo.getTimestampTransform(trace2);

        trace1.setTimestampTransform(tt1);
        trace2.setTimestampTransform(tt2);

        assertEquals("TmfTimestampTransformLinearFast [ slope = 0.9999413783703139011056845831168394, offset = 79796507913179.33347660124688298171 ]", tt1.toString());
        assertEquals(TimestampTransformFactory.getDefaultTransform(), tt2);

        assertEquals(syncAlgo.getTimestampTransform(trace1.getHostId()), trace1.getTimestampTransform());
        assertEquals(syncAlgo.getTimestampTransform(trace2.getHostId()), trace2.getTimestampTransform());

        experiment.dispose();
    }

    /**
     * Testing synchronization with 3 traces, one of which synchronizes with
     * both other
     */
    @Test
    public void testDjangoExperimentSync() {
        CtfTmfTrace trace1 = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.DJANGO_CLIENT);
        CtfTmfTrace trace2 = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.DJANGO_DB);
        CtfTmfTrace trace3 = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.DJANGO_HTTPD);

        ITmfTrace[] traces = { trace1, trace2, trace3 };
        TmfExperiment experiment = new TmfExperiment(traces[0].getEventType(), EXPERIMENT, traces, BLOCK_SIZE, null);

        SynchronizationAlgorithm syncAlgo = experiment.synchronizeTraces(true);

        ITmfTimestampTransform tt1 = syncAlgo.getTimestampTransform(trace1);
        ITmfTimestampTransform tt2 = syncAlgo.getTimestampTransform(trace2);
        ITmfTimestampTransform tt3 = syncAlgo.getTimestampTransform(trace3);

        trace1.setTimestampTransform(tt1);
        trace2.setTimestampTransform(tt2);
        trace3.setTimestampTransform(tt3);

        assertEquals(TimestampTransformFactory.getDefaultTransform(), tt1);
        assertEquals("TmfTimestampTransformLinearFast [ slope = 0.9999996313017589597204633828681240, offset = 498490309972.0038068817738527724192 ]", tt2.toString());
        assertEquals("TmfTimestampTransformLinearFast [ slope = 1.000000119014882262265342419815932, offset = -166652893534.6189900382736187431134 ]", tt3.toString());

        experiment.dispose();
    }
}
