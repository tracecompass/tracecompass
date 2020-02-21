/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.analysis.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.graph.core.base.IGraphWorker;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge.EdgeType;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex;
import org.eclipse.tracecompass.analysis.graph.core.building.TmfGraphBuilderModule;
import org.eclipse.tracecompass.analysis.graph.core.criticalpath.CriticalPathModule;
import org.eclipse.tracecompass.analysis.graph.core.tests.stubs.GraphOps;
import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsExecutionGraph;
import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsWorker;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.trace.TmfXmlKernelTraceStub;
import org.eclipse.tracecompass.lttng2.kernel.core.tests.Activator;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.junit.Test;

/**
 * Test the distributed critical path, when traced machines communicate with one
 * another through the network
 *
 * @author Geneviève Bastien
 */
public class DistributedCriticalPathTest {

    private static final String EXPERIMENT = "CritPathExperiment";
    private static int BLOCK_SIZE = 1000;
    private static final @NonNull String TEST_ANALYSIS_ID = OsExecutionGraph.ANALYSIS_ID;

    /**
     * Setup the experiment for the tests
     *
     * @param traceFiles
     *            File names relative to this plugin for the trace files to load
     * @return The experiment with its graph module executed
     * @throws TmfTraceException
     */
    private ITmfTrace setUpExperiment(String... traceFiles) throws TmfTraceException {
        ITmfTrace[] traces = new ITmfTrace[traceFiles.length];
        int i = 0;
        for (String traceFile : traceFiles) {
            TmfXmlKernelTraceStub trace = new TmfXmlKernelTraceStub();
            IPath filePath = Activator.getAbsoluteFilePath(traceFile);
            IStatus status = trace.validate(null, filePath.toOSString());
            if (!status.isOK()) {
                fail(status.getException().getMessage());
            }
            trace.initTrace(null, filePath.toOSString(), ITmfEvent.class);
            traces[i++] = trace;
        }

        TmfExperiment experiment = new TmfExperiment(ITmfEvent.class, EXPERIMENT, traces, BLOCK_SIZE, null);
        experiment.traceOpened(new TmfTraceOpenedSignal(this, experiment, null));

        IAnalysisModule module = null;
        for (IAnalysisModule mod : TmfTraceUtils.getAnalysisModulesOfClass(experiment, TmfGraphBuilderModule.class)) {
            module = mod;
        }
        assertNotNull(module);
        module.schedule();
        assertTrue(module.waitForCompletion());
        return experiment;
    }

    /**
     * Test the graph building of a network exchange where one machine receives
     * in a softirq and the other receives in a threaded IRQ, with new network
     * reception events. The 2 traces include events to wrap IRQ and packet
     * reception contexts
     *
     * @throws TmfTraceException
     *             Exception thrown by opening experiment
     * @throws TmfAnalysisException
     *             Exception thrown by analyses
     */
    @Test
    public void testNetworkExchangeWithWifi() throws TmfTraceException, TmfAnalysisException {
        ITmfTrace experiment = setUpExperiment("testfiles/graph/network_exchange_eth.xml", "testfiles/graph/network_exchange_wifi.xml");
        assertNotNull(experiment);
        try {
            internalTestNetworkExchangeWithWifi(experiment);
        } finally {
            experiment.dispose();
        }
    }

    private static void internalTestNetworkExchangeWithWifi(@NonNull ITmfTrace experiment) throws TmfAnalysisException {
        TmfGraphBuilderModule module = TmfTraceUtils.getAnalysisModuleOfClass(experiment, TmfGraphBuilderModule.class, TEST_ANALYSIS_ID);
        assertNotNull(module);

        TmfGraph graph = module.getGraph();
        assertNotNull(graph);

        Set<IGraphWorker> workers = graph.getWorkers();
        assertEquals(6, workers.size());

        // Prepare a worker map
        final int irqThread = 50;
        final int clientThread = 200;
        final int otherClient = 201;
        final int serverThread = 100;
        final int otherServer = 101;
        final int kernelThread = -1;
        Map<Integer, IGraphWorker> workerMap = new HashMap<>();
        for (IGraphWorker worker : workers) {
            workerMap.put(((OsWorker) worker).getHostThread().getTid(), worker);
        }
        // Build the expected graph
        TmfGraph expected = new TmfGraph();

        // other thread on client side
        IGraphWorker worker = workerMap.get(otherClient);
        assertNotNull(worker);
        expected.add(worker, new TmfVertex(10));
        expected.append(worker, new TmfVertex(15), EdgeType.PREEMPTED);
        expected.append(worker, new TmfVertex(60), EdgeType.RUNNING);

        // client thread
        worker = workerMap.get(clientThread);
        assertNotNull(worker);
        expected.add(worker, new TmfVertex(10));
        TmfVertex packet1Sent = new TmfVertex(13);
        expected.append(worker, packet1Sent, EdgeType.RUNNING);
        expected.append(worker, new TmfVertex(15), EdgeType.RUNNING);
        TmfVertex packet2Received = new TmfVertex(70);
        expected.append(worker, packet2Received, EdgeType.NETWORK, "irq/30-handler");
        expected.append(worker, new TmfVertex(75), EdgeType.PREEMPTED);

        // irq thread
        worker = workerMap.get(irqThread);
        assertNotNull(worker);
        expected.add(worker, new TmfVertex(55));
        expected.append(worker, new TmfVertex(60), EdgeType.PREEMPTED);
        expected.append(worker, new TmfVertex(65), EdgeType.RUNNING);
        expected.append(worker, new TmfVertex(75), EdgeType.RUNNING);

        // Other thread on server side
        worker = workerMap.get(otherServer);
        assertNotNull(worker);
        expected.add(worker, new TmfVertex(5));
        expected.append(worker, new TmfVertex(40), EdgeType.RUNNING);
        expected.append(worker, new TmfVertex(55), EdgeType.PREEMPTED);

        // Server thread
        worker = workerMap.get(serverThread);
        assertNotNull(worker);
        expected.add(worker, new TmfVertex(5));
        TmfVertex packet1Received = new TmfVertex(35);
        expected.append(worker, packet1Received, EdgeType.NETWORK);
        expected.append(worker, new TmfVertex(40), EdgeType.PREEMPTED);
        TmfVertex packet2Sent = new TmfVertex(45);
        expected.append(worker, packet2Sent, EdgeType.RUNNING);
        expected.append(worker, new TmfVertex(55), EdgeType.RUNNING);

        // Create the vertical links
        TmfEdge link = packet1Sent.linkVertical(packet1Received);
        link.setType(EdgeType.NETWORK);
        link = packet2Sent.linkVertical(packet2Received);
        link.setType(EdgeType.NETWORK);

        // kernel worker on server side
        worker = workerMap.get(kernelThread);
        assertNotNull(worker);
        expected.add(worker, new TmfVertex(30));
        expected.append(worker, new TmfVertex(33), EdgeType.RUNNING);

        GraphOps.checkEquality(expected, graph);

        /* Test the critical path */

        // Build the expected critical path
        expected = new TmfGraph();
        // Client worker
        worker = workerMap.get(clientThread);
        assertNotNull(worker);
        expected.add(worker, new TmfVertex(10));
        expected.append(worker, new TmfVertex(13), EdgeType.RUNNING);
        packet1Sent = new TmfVertex(15);
        expected.append(worker, packet1Sent, EdgeType.RUNNING);
        packet2Received = new TmfVertex(70);
        expected.add(worker, packet2Received);
        expected.append(worker, new TmfVertex(75), EdgeType.PREEMPTED);

        // Server worker
        worker = workerMap.get(serverThread);
        assertNotNull(worker);
        packet1Received = new TmfVertex(35);
        expected.add(worker, packet1Received);
        expected.append(worker, new TmfVertex(40), EdgeType.PREEMPTED);
        packet2Sent = new TmfVertex(45);
        expected.append(worker, packet2Sent, EdgeType.RUNNING);

        link = packet1Sent.linkVertical(packet1Received);
        link.setType(EdgeType.NETWORK);
        link = packet2Sent.linkVertical(packet2Received);
        link.setType(EdgeType.NETWORK);

        // Execute the critical path module and compare equality
        CriticalPathModule critPathModule = new CriticalPathModule(module);
        try {
            critPathModule.setTrace(experiment);
            critPathModule.setParameter(CriticalPathModule.PARAM_WORKER, workerMap.get(clientThread));
            critPathModule.schedule();
            assertTrue(critPathModule.waitForCompletion());

            TmfGraph criticalPath = critPathModule.getCriticalPath();
            assertNotNull(criticalPath);

            GraphOps.checkEquality(expected, criticalPath);
        } finally {
            critPathModule.dispose();
        }
    }

    /**
     * Test the graph building of a simple network exchange where both machines
     * receive in softirqs, without packet reception and complete IRQ contexts
     *
     * @throws TmfTraceException
     *             Exception thrown by opening experiment
     * @throws TmfAnalysisException
     *             Exception thrown by analyses
     */
    @Test
    public void testNetworkExchange() throws TmfTraceException, TmfAnalysisException {
        ITmfTrace experiment = setUpExperiment("testfiles/graph/simple_network_server.xml", "testfiles/graph/simple_network_client.xml");
        assertNotNull(experiment);
        try {
            internalTestNetworkExchange(experiment);
        } finally {
            experiment.dispose();
        }
    }

    private static void internalTestNetworkExchange(@NonNull ITmfTrace experiment) throws TmfAnalysisException {
        TmfGraphBuilderModule module = TmfTraceUtils.getAnalysisModuleOfClass(experiment, TmfGraphBuilderModule.class, TEST_ANALYSIS_ID);
        assertNotNull(module);

        TmfGraph graph = module.getGraph();
        assertNotNull(graph);

        Set<IGraphWorker> workers = graph.getWorkers();
        assertEquals(7, workers.size());

        // Prepare a worker map
        final int clientThread = 200;
        final int otherClient = 201;
        final int depClient = 202;
        final int serverThread = 100;
        final int otherServer = 101;
        OsWorker clientWorker = null;
        OsWorker serverWorker = null;
        Map<Integer, IGraphWorker> workerMap = new HashMap<>();
        for (IGraphWorker worker : workers) {
            OsWorker osWorker = (OsWorker) worker;
            if (osWorker.getHostThread().getTid() < 0) {
                if (osWorker.getHostId().equals("simple_network_server.xml")) {
                    serverWorker = osWorker;
                } else {
                    clientWorker = osWorker;
                }
            }
            workerMap.put(osWorker.getHostThread().getTid(), worker);
        }
        // Make the expected graph
        TmfGraph expected = new TmfGraph();

        // other thread on client side
        IGraphWorker worker = workerMap.get(otherClient);
        assertNotNull(worker);
        expected.add(worker, new TmfVertex(7));
        expected.append(worker, new TmfVertex(10), EdgeType.RUNNING);
        expected.append(worker, new TmfVertex(15), EdgeType.PREEMPTED);
        expected.append(worker, new TmfVertex(75), EdgeType.RUNNING);

        // client thread
        worker = workerMap.get(clientThread);
        assertNotNull(worker);
        expected.add(worker, new TmfVertex(10));
        TmfVertex packet1Sent = new TmfVertex(13);
        expected.append(worker, packet1Sent, EdgeType.RUNNING);
        expected.append(worker, new TmfVertex(15), EdgeType.RUNNING);
        TmfVertex packet2Received = new TmfVertex(70);
        expected.append(worker, packet2Received, EdgeType.NETWORK);
        expected.append(worker, new TmfVertex(75), EdgeType.PREEMPTED);
        TmfVertex wakeupSource = new TmfVertex(90);
        expected.append(worker, wakeupSource, EdgeType.RUNNING);
        expected.append(worker, new TmfVertex(95), EdgeType.RUNNING);

        // client kernel worker
        worker = clientWorker;
        assertNotNull(worker);
        expected.add(worker, new TmfVertex(60));
        expected.append(worker, new TmfVertex(65), EdgeType.RUNNING);

        // thread on client waiting for client process
        worker = workerMap.get(depClient);
        assertNotNull(worker);
        expected.add(worker, new TmfVertex(7));
        TmfVertex wakeupTarget = new TmfVertex(90);
        expected.append(worker, wakeupTarget, EdgeType.BLOCKED);
        expected.append(worker, new TmfVertex(95), EdgeType.PREEMPTED);
        wakeupSource.linkVertical(wakeupTarget);

        // Other thread on server side
        worker = workerMap.get(otherServer);
        assertNotNull(worker);
        expected.add(worker, new TmfVertex(5));
        expected.append(worker, new TmfVertex(40), EdgeType.RUNNING);
        expected.append(worker, new TmfVertex(55), EdgeType.PREEMPTED);

        // Server thread
        worker = workerMap.get(serverThread);
        assertNotNull(worker);
        expected.add(worker, new TmfVertex(5));
        TmfVertex packet1Received = new TmfVertex(35);
        expected.append(worker, packet1Received, EdgeType.NETWORK);
        expected.append(worker, new TmfVertex(40), EdgeType.PREEMPTED);
        TmfVertex packet2Sent = new TmfVertex(45);
        expected.append(worker, packet2Sent, EdgeType.RUNNING);
        expected.append(worker, new TmfVertex(55), EdgeType.RUNNING);

        // Create the vertical links
        TmfEdge link = packet1Sent.linkVertical(packet1Received);
        link.setType(EdgeType.NETWORK);
        link = packet2Sent.linkVertical(packet2Received);
        link.setType(EdgeType.NETWORK);

        // kernel worker on server side
        worker = serverWorker;
        assertNotNull(worker);
        expected.add(worker, new TmfVertex(30));
        expected.append(worker, new TmfVertex(33), EdgeType.RUNNING);

        GraphOps.checkEquality(expected, graph);

        /* Test the critical path */

        // Build the expected critical path
        expected = new TmfGraph();

        // Client worker
        worker = workerMap.get(clientThread);
        assertNotNull(worker);
        expected.add(worker, new TmfVertex(10));
        expected.append(worker, new TmfVertex(13), EdgeType.RUNNING);
        packet1Sent = new TmfVertex(15);
        expected.append(worker, packet1Sent, EdgeType.RUNNING);
        packet2Received = new TmfVertex(70);
        expected.add(worker, packet2Received);
        expected.append(worker, new TmfVertex(75), EdgeType.PREEMPTED);
        expected.append(worker, new TmfVertex(90), EdgeType.RUNNING);
        expected.append(worker, new TmfVertex(95), EdgeType.RUNNING);

        // Server worker
        worker = workerMap.get(serverThread);
        assertNotNull(worker);
        packet1Received = new TmfVertex(35);
        expected.add(worker, packet1Received);
        expected.append(worker, new TmfVertex(40), EdgeType.PREEMPTED);
        packet2Sent = new TmfVertex(45);
        expected.append(worker, packet2Sent, EdgeType.RUNNING);

        link = packet1Sent.linkVertical(packet1Received);
        link.setType(EdgeType.NETWORK);
        link = packet2Sent.linkVertical(packet2Received);
        link.setType(EdgeType.NETWORK);

        // Execute the critical path module and compare equality
        CriticalPathModule critPathModule = new CriticalPathModule(module);
        try {
            critPathModule.setTrace(experiment);
            critPathModule.setParameter(CriticalPathModule.PARAM_WORKER, workerMap.get(clientThread));
            critPathModule.schedule();
            assertTrue(critPathModule.waitForCompletion());

            TmfGraph criticalPath = critPathModule.getCriticalPath();
            assertNotNull(criticalPath);

            GraphOps.checkEquality(expected, criticalPath);
        } finally {
            critPathModule.dispose();
        }
    }

    /**
     * Test the graph building of a simple network exchange but without the
     * other machine's trace. The process should be blocked by network
     *
     * @throws TmfTraceException
     *             Exception thrown by opening experiment
     * @throws TmfAnalysisException
     *             Exception thrown by analyses
     */
    @Test
    public void testNetworkExchangeOneTrace() throws TmfTraceException, TmfAnalysisException {
        ITmfTrace experiment = setUpExperiment("testfiles/graph/network_exchange_wifi.xml");
        assertNotNull(experiment);
        try {
            internalTestNetworkExchangeOneTrace(experiment);
        } finally {
            experiment.dispose();
        }
    }

    private static void internalTestNetworkExchangeOneTrace(@NonNull ITmfTrace experiment) throws TmfAnalysisException {
        TmfGraphBuilderModule module = TmfTraceUtils.getAnalysisModuleOfClass(experiment, TmfGraphBuilderModule.class, TEST_ANALYSIS_ID);
        assertNotNull(module);

        TmfGraph graph = module.getGraph();
        assertNotNull(graph);

        Set<IGraphWorker> workers = graph.getWorkers();
        assertEquals(3, workers.size());

        // Prepare a worker map
        final int irqThread = 50;
        final int clientThread = 200;
        final int otherClient = 201;
        Map<Integer, IGraphWorker> workerMap = new HashMap<>();
        for (IGraphWorker worker : workers) {
            workerMap.put(((OsWorker) worker).getHostThread().getTid(), worker);
        }
        // Make the expected graph
        TmfGraph expected = new TmfGraph();

        // other thread on client side
        IGraphWorker worker = workerMap.get(otherClient);
        assertNotNull(worker);
        expected.add(worker, new TmfVertex(10));
        expected.append(worker, new TmfVertex(15), EdgeType.PREEMPTED);
        expected.append(worker, new TmfVertex(60), EdgeType.RUNNING);

        // client thread
        worker = workerMap.get(clientThread);
        assertNotNull(worker);
        expected.add(worker, new TmfVertex(10));
        TmfVertex packet1Sent = new TmfVertex(13);
        expected.append(worker, packet1Sent, EdgeType.RUNNING);
        expected.append(worker, new TmfVertex(15), EdgeType.RUNNING);
        TmfVertex packet2Received = new TmfVertex(70);
        expected.append(worker, packet2Received, EdgeType.NETWORK, "irq/30-handler");
        expected.append(worker, new TmfVertex(75), EdgeType.PREEMPTED);

        // irq thread
        worker = workerMap.get(irqThread);
        assertNotNull(worker);
        expected.add(worker, new TmfVertex(55));
        expected.append(worker, new TmfVertex(60), EdgeType.PREEMPTED);
        expected.append(worker, new TmfVertex(65), EdgeType.RUNNING);
        expected.append(worker, new TmfVertex(75), EdgeType.RUNNING);

        GraphOps.checkEquality(expected, graph);

        /* Test the critical path */
        // Build the expected graph: it should be the client thread only
        expected = new TmfGraph();

        worker = workerMap.get(clientThread);
        assertNotNull(worker);

        for (TmfVertex vertex : graph.getNodesOf(worker)) {
            expected.add(worker, vertex);
        }

        // Execute the critical path module and compare equality
        CriticalPathModule critPathModule = new CriticalPathModule(module);
        try {
            critPathModule.setTrace(experiment);
            critPathModule.setParameter(CriticalPathModule.PARAM_WORKER, workerMap.get(clientThread));
            critPathModule.schedule();
            assertTrue(critPathModule.waitForCompletion());

            TmfGraph criticalPath = critPathModule.getCriticalPath();
            assertNotNull(criticalPath);

            GraphOps.checkEquality(expected, criticalPath);
        } finally {
            critPathModule.dispose();
        }
    }

    /**
     * Test the graph building of a simple network exchange but without the
     * other machine's trace. The process should be blocked by network
     *
     * @throws TmfTraceException
     *             Exception thrown by opening experiment
     * @throws TmfAnalysisException
     *             Exception thrown by analyses
     */
    @Test
    public void testNetworkExchangeOneTraceSoftirq() throws TmfTraceException, TmfAnalysisException {
        ITmfTrace experiment = setUpExperiment("testfiles/graph/simple_network_client.xml");
        assertNotNull(experiment);
        try {
            internalTestNetworkExchangeOneTraceSoftirq(experiment);
        } finally {
            experiment.dispose();
        }
    }

    private static void internalTestNetworkExchangeOneTraceSoftirq(@NonNull ITmfTrace experiment) throws TmfAnalysisException {
        TmfGraphBuilderModule module = TmfTraceUtils.getAnalysisModuleOfClass(experiment, TmfGraphBuilderModule.class, TEST_ANALYSIS_ID);
        assertNotNull(module);

        TmfGraph graph = module.getGraph();
        assertNotNull(graph);

        Set<IGraphWorker> workers = graph.getWorkers();
        assertEquals(4, workers.size());

        // Prepare a worker map
        final int clientThread = 200;
        final int otherClient = 201;
        final int depClient = 202;
        OsWorker clientWorker = null;
        Map<Integer, IGraphWorker> workerMap = new HashMap<>();
        for (IGraphWorker worker : workers) {
            OsWorker osWorker = (OsWorker) worker;
            if (osWorker.getHostThread().getTid() < 0) {
                clientWorker = osWorker;
            }
            workerMap.put(osWorker.getHostThread().getTid(), worker);
        }
        // Make the expected graph
        TmfGraph expected = new TmfGraph();

        // other thread on client side
        IGraphWorker worker = workerMap.get(otherClient);
        assertNotNull(worker);
        expected.add(worker, new TmfVertex(7));
        expected.append(worker, new TmfVertex(10), EdgeType.RUNNING);
        expected.append(worker, new TmfVertex(15), EdgeType.PREEMPTED);
        expected.append(worker, new TmfVertex(75), EdgeType.RUNNING);

        // client thread
        worker = workerMap.get(clientThread);
        assertNotNull(worker);
        expected.add(worker, new TmfVertex(10));
        TmfVertex packet1Sent = new TmfVertex(13);
        expected.append(worker, packet1Sent, EdgeType.RUNNING);
        expected.append(worker, new TmfVertex(15), EdgeType.RUNNING);
        TmfVertex packet2Received = new TmfVertex(70);
        expected.append(worker, packet2Received, EdgeType.NETWORK);
        expected.append(worker, new TmfVertex(75), EdgeType.PREEMPTED);
        TmfVertex wakeupSource = new TmfVertex(90);
        expected.append(worker, wakeupSource, EdgeType.RUNNING);
        expected.append(worker, new TmfVertex(95), EdgeType.RUNNING);

        // client kernel worker
        worker = clientWorker;
        assertNotNull(worker);
        expected.add(worker, new TmfVertex(60));
        expected.append(worker, new TmfVertex(65), EdgeType.RUNNING);

        // thread on client waiting for client process
        worker = workerMap.get(depClient);
        assertNotNull(worker);
        expected.add(worker, new TmfVertex(7));
        TmfVertex wakeupTarget = new TmfVertex(90);
        expected.append(worker, wakeupTarget, EdgeType.BLOCKED);
        expected.append(worker, new TmfVertex(95), EdgeType.PREEMPTED);
        wakeupSource.linkVertical(wakeupTarget);

        GraphOps.checkEquality(expected, graph);

        /* Test the critical path */

        // Build the expected critical path
        expected = new TmfGraph();

        // Client worker
        IGraphWorker cWorker = workerMap.get(clientThread);
        assertNotNull(cWorker);
        expected.add(cWorker, new TmfVertex(10));
        expected.append(cWorker, new TmfVertex(13), EdgeType.RUNNING);
        packet1Sent = new TmfVertex(15);
        expected.append(cWorker, packet1Sent, EdgeType.RUNNING);
        packet2Received = new TmfVertex(70);
        expected.append(cWorker, packet2Received, EdgeType.NETWORK);
        expected.append(cWorker, new TmfVertex(75), EdgeType.PREEMPTED);
        wakeupSource = new TmfVertex(90);
        expected.append(cWorker, wakeupSource, EdgeType.RUNNING);
        expected.append(cWorker, new TmfVertex(95), EdgeType.RUNNING);

        // Execute the critical path module and compare equality
        CriticalPathModule critPathModule = new CriticalPathModule(module);
        try {
            critPathModule.setTrace(experiment);
            critPathModule.setParameter(CriticalPathModule.PARAM_WORKER, cWorker);
            critPathModule.schedule();
            assertTrue(critPathModule.waitForCompletion());

            TmfGraph criticalPath = critPathModule.getCriticalPath();
            assertNotNull(criticalPath);

            GraphOps.checkEquality(expected, criticalPath);

            // Test the critical path for the dependent thread
            // Critical path for the dependent worker
            expected = new TmfGraph();
            worker = workerMap.get(depClient);
            assertNotNull(worker);
            TmfVertex begin = new TmfVertex(7);
            expected.add(worker, begin);
            wakeupTarget = new TmfVertex(90);
            expected.add(worker, wakeupTarget);
            expected.append(worker, new TmfVertex(95), EdgeType.PREEMPTED);

            // Copy the critical path of the client worker
            TmfVertex start = new TmfVertex(7);
            expected.add(cWorker, start);
            expected.append(cWorker, new TmfVertex(10), EdgeType.UNKNOWN);
            expected.append(cWorker, new TmfVertex(13), EdgeType.RUNNING);
            packet1Sent = new TmfVertex(15);
            expected.append(cWorker, packet1Sent, EdgeType.RUNNING);
            packet2Received = new TmfVertex(70);
            expected.append(cWorker, packet2Received, EdgeType.NETWORK);
            expected.append(cWorker, new TmfVertex(75), EdgeType.PREEMPTED);
            wakeupSource = new TmfVertex(90);
            expected.append(cWorker, wakeupSource, EdgeType.RUNNING);

            // Add the links
            begin.linkVertical(start);
            wakeupSource.linkVertical(wakeupTarget);

            critPathModule.setParameter(CriticalPathModule.PARAM_WORKER, worker);
            critPathModule.schedule();
            assertTrue(critPathModule.waitForCompletion());

            criticalPath = critPathModule.getCriticalPath();
            assertNotNull(criticalPath);

            GraphOps.checkEquality(expected, criticalPath);
        } finally {
            critPathModule.dispose();
        }
    }

}
