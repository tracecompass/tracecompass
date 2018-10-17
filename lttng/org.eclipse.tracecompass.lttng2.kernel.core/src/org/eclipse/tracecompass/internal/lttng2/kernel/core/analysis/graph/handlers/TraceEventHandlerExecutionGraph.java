/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.handlers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge.EdgeType;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex.EdgeDirection;
import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsExecutionGraphProvider;
import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsExecutionGraphProvider.Context;
import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsInterruptContext;
import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsSystemModel;
import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsWorker;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.LinuxValues;
import org.eclipse.tracecompass.analysis.os.linux.core.model.HostThread;
import org.eclipse.tracecompass.analysis.os.linux.core.model.ProcessStatus;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.TcpEventStrings;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.event.matching.IMatchProcessingUnit;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventDependency;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventDependency.DependencyEvent;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventMatching;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Event handler that actually builds the execution graph from the events
 *
 * @author Francis Giraldeau
 * @author Geneviève Bastien
 */
public class TraceEventHandlerExecutionGraph extends BaseHandler {

    /*
     * The following IRQ constants was found empirically.
     *
     * TODO: other IRQ values should be determined from the lttng_statedump_interrupt events.
     */
    private static final int IRQ_TIMER = 0;

    private static final NullProgressMonitor DEFAULT_PROGRESS_MONITOR = new NullProgressMonitor();

    private final Table<String, Integer, OsWorker> fKernel;
    private final IMatchProcessingUnit fMatchProcessing;
    private Map<DependencyEvent, TmfVertex> fTcpNodes;
    private TmfEventMatching fTcpMatching;

    /**
     * Constructor
     *
     * @param provider
     *            The parent graph provider
     * @param priority
     *            The priority of this handler. It will determine when it will be
     *            executed
     */
    public TraceEventHandlerExecutionGraph(OsExecutionGraphProvider provider, int priority) {
        super(provider, priority);
        fKernel = HashBasedTable.create();

        fTcpNodes = new HashMap<>();
        fMatchProcessing = new IMatchProcessingUnit() {

            @Override
            public void matchingEnded() {
            }

            @Override
            public int countMatches() {
                return 0;
            }

            @Override
            public void addMatch(@Nullable TmfEventDependency match) {
                if (match == null) {
                    return;
                }
                TmfVertex output = fTcpNodes.remove(match.getSource());
                TmfVertex input = fTcpNodes.remove(match.getDestination());
                if (output != null && input != null) {
                    output.linkVertical(input).setType(EdgeType.NETWORK);
                }
            }

            @Override
            public void init(Collection<ITmfTrace> fTraces) {

            }

        };

        ITmfTrace trace = provider.getTrace();
        fTcpMatching = new TmfEventMatching(Collections.singleton(trace), fMatchProcessing);
        fTcpMatching.initMatching();
    }

    private OsWorker getOrCreateKernelWorker(ITmfEvent event, Integer cpu) {
        String host = event.getTrace().getHostId();
        OsWorker worker = fKernel.get(host, cpu);
        if (worker == null) {
            HostThread ht = new HostThread(host, -1);
            worker = new OsWorker(ht, "kernel/" + cpu, event.getTimestamp().getValue()); //$NON-NLS-1$
            worker.setStatus(ProcessStatus.RUN);

            fKernel.put(host, cpu, worker);
        }
        return worker;
    }

    @Override
    public void handleEvent(ITmfEvent ev) {
        String eventName = ev.getName();
        IKernelAnalysisEventLayout eventLayout = getProvider().getEventLayout(ev.getTrace());

        if (eventName.equals(eventLayout.eventSchedSwitch())) {
            handleSchedSwitch(ev);
        } else if (eventName.equals(eventLayout.eventSoftIrqEntry())) {
            handleSoftirqEntry(ev);
        } else if (eventLayout.eventsNetworkReceive().contains(eventName) ||
                eventName.equals(TcpEventStrings.INET_SOCK_LOCAL_IN)) {
            handleInetSockLocalIn(ev);
        } else if (eventLayout.eventsNetworkSend().contains(eventName) ||
                eventName.equals(TcpEventStrings.INET_SOCK_LOCAL_OUT)) {
            handleInetSockLocalOut(ev);
        } else if (isWakeupEvent(ev)) {
            handleSchedWakeup(ev);
        }
    }

    private TmfVertex stateExtend(OsWorker task, long ts) {
        TmfGraph graph = NonNullUtils.checkNotNull(getProvider().getAssignedGraph());
        TmfVertex node = new TmfVertex(ts);
        ProcessStatus status = task.getStatus();
        graph.append(task, node, resolveProcessStatus(status));
        return node;
    }

    private TmfVertex stateChange(OsWorker task, long ts) {
        TmfGraph graph = NonNullUtils.checkNotNull(getProvider().getAssignedGraph());
        TmfVertex node = new TmfVertex(ts);
        ProcessStatus status = task.getOldStatus();
        graph.append(task, node, resolveProcessStatus(status));
        return node;
    }

    private static EdgeType resolveProcessStatus(ProcessStatus status) {
        EdgeType ret = EdgeType.UNKNOWN;
        switch (status) {
        case NOT_ALIVE:
            break;
        case EXIT:
        case RUN:
        case RUN_SYTEMCALL:
        case INTERRUPTED:
            ret = EdgeType.RUNNING;
            break;
        case UNKNOWN:
            ret = EdgeType.UNKNOWN;
            break;
        case WAIT_BLOCKED:
            ret = EdgeType.BLOCKED;
            break;
        case WAIT_CPU:
        case WAIT_FORK:
        case WAIT_UNKNOWN:
            ret = EdgeType.PREEMPTED;
            break;
        case ZOMBIE:
            ret = EdgeType.UNKNOWN;
            break;
        default:
            break;
        }
        return ret;
    }

    private void handleSchedSwitch(ITmfEvent event) {
        String host = event.getTrace().getHostId();
        long ts = event.getTimestamp().getValue();
        IKernelAnalysisEventLayout eventLayout = getProvider().getEventLayout(event.getTrace());
        OsSystemModel system = getProvider().getSystem();
        ITmfEventField content = event.getContent();

        Integer next = content.getFieldValue(Integer.class, eventLayout.fieldNextTid());
        Integer prev =content.getFieldValue(Integer.class, eventLayout.fieldPrevTid());

        if (next == null || prev == null) {
            return;
        }

        OsWorker nextTask = system.findWorker(new HostThread(host, next));
        OsWorker prevTask = system.findWorker(new HostThread(host, prev));

        if (prevTask == null || nextTask == null) {
            return;
        }
        stateChange(prevTask, ts);
        stateChange(nextTask, ts);
    }

    private void handleSchedWakeup(ITmfEvent event) {
        TmfGraph graph = NonNullUtils.checkNotNull(getProvider().getAssignedGraph());
        String host = event.getTrace().getHostId();
        Integer cpu = NonNullUtils.checkNotNull(TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event));
        IKernelAnalysisEventLayout eventLayout = getProvider().getEventLayout(event.getTrace());
        OsSystemModel system = getProvider().getSystem();

        long ts = event.getTimestamp().getValue();
        Integer tid = event.getContent().getFieldValue(Integer.class, eventLayout.fieldTid());
        if (tid == null) {
            return;
        }

        OsWorker target = system.findWorker(new HostThread(host, tid));
        OsWorker current = system.getWorkerOnCpu(host, cpu);
        if (target == null) {
            return;
        }

        ProcessStatus status = target.getOldStatus();
        switch (status) {
        case WAIT_FORK:
            waitFork(graph, ts, target, current);
            break;
        case WAIT_BLOCKED:
            waitBlocked(event, graph, host, cpu, eventLayout, system, ts, target, current);
            break;
        case NOT_ALIVE:
        case EXIT:
        case RUN:
        case UNKNOWN:
        case WAIT_CPU:
        case ZOMBIE:
        case INTERRUPTED:
        case RUN_SYTEMCALL:
        case WAIT_UNKNOWN:
        default:
            break;
        }
    }

    private void waitBlocked(ITmfEvent event, TmfGraph graph, String host, Integer cpu, IKernelAnalysisEventLayout eventLayout, OsSystemModel system, long ts, OsWorker target, @Nullable OsWorker current) {
        OsInterruptContext context = system.peekContextStack(host, cpu);
        switch (context.getContext()) {
        case HRTIMER:
            // shortcut of appendTaskNode: resolve blocking source in situ
            graph.append(target, new TmfVertex(ts), EdgeType.TIMER);
            break;
        case IRQ:
        case COMPLETE_IRQ:
            irq(graph, eventLayout, ts, target, context);
            break;
        case SOFTIRQ:
            softIrq(event, graph, cpu, eventLayout, ts, target, context);
            break;
        case IPI:
            graph.append(target, new TmfVertex(ts), EdgeType.IPI);
            break;
        case NONE:
            none(ts, target, current);
            break;
        case PACKET_RECEPTION:
            receivingFromNetwork(event, host, cpu, system, graph, ts, target, current);
            break;
        default:
            break;
        }
    }

    private void receivingFromNetwork(ITmfEvent event, String host, Integer cpu, OsSystemModel system, TmfGraph graph, long ts, OsWorker target, @Nullable OsWorker current) {
        // Look at the inner context to see if the current worker is the
        // receptor or if we are in irq context
        Context innerCtx = peekInnerContext(host, cpu, system);
        OsWorker source = current;
        if (innerCtx == Context.SOFTIRQ || innerCtx == Context.IRQ) {
            source = getOrCreateKernelWorker(event, cpu);
        }

        // Append a vertex to the target and set the edge type to network
        TmfVertex wupTarget = new TmfVertex(ts);
        TmfEdge netLink = graph.append(target, wupTarget);
        if (netLink != null) {
            if (source != null && innerCtx != Context.SOFTIRQ && innerCtx != Context.IRQ) {
                netLink.setType(EdgeType.NETWORK, source.getName());
            } else {
                netLink.setType(EdgeType.NETWORK);
            }
        }

        if (source == null) {
            return;
        }
        // See if we can directly link from the packet reception.
        TmfVertex tail = graph.getTail(source);
        if (tail != null) {
            replaceIncomingNetworkEdge(tail, wupTarget);
        }
    }

    /**
     * If the last tail vertex has an incoming vertical edge of network type, it
     * would be the packet reception that caused the wakeup. So here, we have
     * that incoming edge point to the target instead of passing through the
     * current worker and being woken up later. Because we know from context
     * that it's the packet who caused the wakeup and not the current process
     *
     * @param tail
     *            The last vertex of the wakeup source
     * @param wupTarget
     *            The wakeup target
     * @return <code>true</code> if the edge was replaced, <code>false</code>
     *         otherwise. That last response would indicate the source of the
     *         packet couldn't be identified, so proper wakeup link should be
     *         done
     */
    private static boolean replaceIncomingNetworkEdge(TmfVertex tail, TmfVertex wupTarget) {
        TmfEdge edge = tail.getEdge(EdgeDirection.INCOMING_VERTICAL_EDGE);

        if (edge == null || edge.getType() != EdgeType.NETWORK) {
            return false;
        }
        TmfEdge newLink = edge.getVertexFrom().linkVertical(wupTarget);
        newLink.setType(EdgeType.NETWORK);
        tail.removeEdge(EdgeDirection.INCOMING_VERTICAL_EDGE);
        return true;
    }

    /* Extend the source worker to ts, and add a vertical link to the target vertex */
    private void extendAndLink(OsWorker worker, long ts, TmfVertex targetVertex) {
        // Extend the source worker to the timestamp
        TmfVertex wupSource = stateExtend(worker, ts);
        // Add a vertical link to target
        wupSource.linkVertical(targetVertex);
    }

    private void softIrq(ITmfEvent event, TmfGraph graph, Integer cpu, IKernelAnalysisEventLayout eventLayout, long ts, OsWorker target, OsInterruptContext context) {
        TmfVertex wupTarget = new TmfVertex(ts);
        TmfEdge hLink = graph.append(target, wupTarget);
        ITmfEventField content = context.getEvent().getContent();
        if (hLink != null) {
            // Try to resolve the type of the target edge to the softirq's source
            Integer vec = content.getFieldValue(Integer.class, eventLayout.fieldVec());
            hLink.setType(resolveSoftirq(vec));
        }
        /*
         * special case for network related softirq. This code supports network
         * drivers that receive packets within softirq. The newer
         * Packet_Reception context replaces this if the right events are
         * enabled.
         */
        Long vec = content.getFieldValue(Long.class, eventLayout.fieldVec());
        if (vec == LinuxValues.SOFTIRQ_NET_RX || vec == LinuxValues.SOFTIRQ_NET_TX) {
            // create edge if wake up is caused by incoming packet
            OsWorker k = getOrCreateKernelWorker(event, cpu);
            TmfVertex tail = graph.getTail(k);
            if (tail != null) {
                replaceIncomingNetworkEdge(tail, wupTarget);
            }
        }
    }

    private void none(long ts, OsWorker target, @Nullable OsWorker current) {
        // task context wakeup
        if (current != null) {
            TmfVertex n1 = stateChange(target, ts);
            extendAndLink(current, ts, n1);
        } else {
            stateChange(target, ts);
        }
    }

    private static void irq(TmfGraph graph, IKernelAnalysisEventLayout eventLayout, long ts, OsWorker target, OsInterruptContext context) {
        TmfVertex wup = new TmfVertex(ts);
        TmfEdge link = graph.append(target, wup);
        if (link != null) {
            Integer vec = context.getEvent().getContent().getFieldValue(Integer.class, eventLayout.fieldIrq());
            link.setType(resolveIRQ(vec));
        }
    }

    private void waitFork(TmfGraph graph, long ts, OsWorker target, @Nullable OsWorker current) {
        if (current != null) {
            TmfVertex n0 = stateExtend(current, ts);
            TmfVertex n1 = stateChange(target, ts);
            graph.link(n0, n1);
        } else {
            stateChange(target, ts);
        }
    }

    private static EdgeType resolveIRQ(@Nullable Integer vec) {
        EdgeType ret = EdgeType.UNKNOWN;
        if (vec == null) {
            return ret;
        }
        switch (vec) {
        case IRQ_TIMER:
            ret = EdgeType.INTERRUPTED;
            break;
        default:
            ret = EdgeType.UNKNOWN;
            break;
        }
        return ret;
    }

    private static EdgeType resolveSoftirq(@Nullable Integer vec) {
        EdgeType ret = EdgeType.UNKNOWN;
        if (vec == null) {
            return ret;
        }
        switch (vec) {
        case LinuxValues.SOFTIRQ_HRTIMER:
        case LinuxValues.SOFTIRQ_TIMER:
            ret = EdgeType.TIMER;
            break;
        case LinuxValues.SOFTIRQ_BLOCK:
        case LinuxValues.SOFTIRQ_BLOCK_IOPOLL:
            ret = EdgeType.BLOCK_DEVICE;
            break;
        case LinuxValues.SOFTIRQ_NET_RX:
        case LinuxValues.SOFTIRQ_NET_TX:
            ret = EdgeType.NETWORK;
            break;
        case LinuxValues.SOFTIRQ_SCHED:
            ret = EdgeType.INTERRUPTED;
            break;
        default:
            ret = EdgeType.UNKNOWN;
            break;
        }
        return ret;
    }

    private static Context peekInnerContext(String host, Integer cpu, OsSystemModel system) {
        // Get the inner context: pop, peek then push back context
        OsInterruptContext lastCtx = system.popContextStack(host, cpu);
        OsInterruptContext innerCtx = system.peekContextStack(host, cpu);
        if (lastCtx != null) {
            system.pushContextStack(host, cpu, lastCtx);
        }
        return innerCtx.getContext();
    }

    private void handleInetSockLocalIn(ITmfEvent event) {
        Integer cpu = NonNullUtils.checkNotNull(TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event));
        String host = event.getTrace().getHostId();
        OsSystemModel system = getProvider().getSystem();

        OsInterruptContext intCtx = system.peekContextStack(host, cpu);
        Context context = intCtx.getContext();
        if (context == Context.PACKET_RECEPTION) {
            context = peekInnerContext(host, cpu, system);
        }
        OsWorker receiver = null;
        if (context == Context.SOFTIRQ || context == Context.IRQ) {
            receiver = getOrCreateKernelWorker(event, cpu);
        } else {
            receiver = system.getWorkerOnCpu(event.getTrace().getHostId(), cpu);
        }
        if (receiver == null) {
            return;
        }
        TmfVertex endpoint;
        endpoint = stateExtend(receiver, event.getTimestamp().getValue());
        fTcpNodes.put(new DependencyEvent(event), endpoint);
        fTcpMatching.matchEvent(event, event.getTrace(), DEFAULT_PROGRESS_MONITOR);
    }

    private void handleInetSockLocalOut(ITmfEvent event) {
        Integer cpu = NonNullUtils.checkNotNull(TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event));
        String host = event.getTrace().getHostId();
        OsSystemModel system = getProvider().getSystem();

        OsInterruptContext intCtx = system.peekContextStack(host, cpu);
        Context context = intCtx.getContext();
        if (context == Context.PACKET_RECEPTION) {
            context = peekInnerContext(host, cpu, system);
        }

        OsWorker sender = null;
        if (context == Context.NONE) {
            sender = system.getWorkerOnCpu(event.getTrace().getHostId(), cpu);
        } else if (context == Context.SOFTIRQ) {
            sender = getOrCreateKernelWorker(event, cpu);
        }
        if (sender == null) {
            return;
        }
        TmfVertex endpoint = stateExtend(sender, event.getTimestamp().getValue());
        fTcpNodes.put(new DependencyEvent(event), endpoint);
        // TODO, add actual progress monitor
        fTcpMatching.matchEvent(event, event.getTrace(), DEFAULT_PROGRESS_MONITOR);
    }

    private void handleSoftirqEntry(ITmfEvent event) {
        IKernelAnalysisEventLayout eventLayout = getProvider().getEventLayout(event.getTrace());
        TmfGraph graph = NonNullUtils.checkNotNull(getProvider().getAssignedGraph());
        Long vec = event.getContent().getFieldValue(Long.class, eventLayout.fieldVec());
        if (vec == LinuxValues.SOFTIRQ_NET_RX || vec == LinuxValues.SOFTIRQ_NET_TX) {
            Integer cpu = NonNullUtils.checkNotNull(TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event));
            OsWorker k = getOrCreateKernelWorker(event, cpu);
            graph.add(k, new TmfVertex(event.getTimestamp().getValue()));
        }
    }

}
