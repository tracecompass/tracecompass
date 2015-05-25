/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.building;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.tracecompass.analysis.graph.core.base.IGraphWorker;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex.EdgeDirection;
import org.eclipse.tracecompass.analysis.graph.core.building.AbstractTmfGraphProvider;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.handlers.EventContextHandler;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.handlers.TraceEventHandlerExecutionGraph;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.handlers.TraceEventHandlerSched;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.handlers.TraceEventHandlerStatedump;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.model.LttngSystemModel;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.model.LttngWorker;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout.LttngEventLayout;
import org.eclipse.tracecompass.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * The graph provider builds an execution graph from a kernel trace. The
 * execution graph is a 2d-mesh model of the system, where vertices represent
 * events, horizontal edges represent states of tasks, and where vertical edges
 * represent relations between tasks (currently local wake-up and pairs of
 * network packets).
 *
 * Event handling is split into smaller sub-handlers. One event request is done,
 * and each event is passed to the handlers in the order in the list, such as
 * this pseudo code:
 *
 * <pre>
 * for each event:
 *   for each handler:
 *     handler.handleEvent(event)
 * </pre>
 *
 * @author Geneviève Bastien
 * @author Francis Giraldeau
 */
public class LttngKernelExecGraphProvider extends AbstractTmfGraphProvider {

    private final LttngEventLayout fEventLayout;
    private final LttngSystemModel fSystem;

    /**
     * Represents an interrupt context
     */
    public enum Context {
        /** Not in an interrupt */
        NONE,
        /** The interrupt is a soft IRQ */
        SOFTIRQ,
        /** The interrupt is an IRQ */
        IRQ,
        /** The interrupt is a timer */
        HRTIMER
    }

    /**
     * A list of status a thread can be in
     */
    public enum ProcessStatus {
        /** Unknown process status */
        UNKNOWN(0),
        /** Waiting for a fork */
        WAIT_FORK(1),
        /** Waiting for the CPU */
        WAIT_CPU(2),
        /** The thread has exited, but is not dead yet */
        EXIT(3),
        /** The thread is a zombie thread */
        ZOMBIE(4),
        /** The thread is blocked */
        WAIT_BLOCKED(5),
        /** The thread is running */
        RUN(6),
        /** The thread is dead */
        DEAD(7);
        private final int fValue;

        private ProcessStatus(int value) {
            fValue = value;
        }

        private int value() {
            return fValue;
        }

        /**
         * Get the ProcessStatus associated with a long value
         *
         * @param val
         *            The long value corresponding to a status
         * @return The {@link ProcessStatus} enum value
         */
        static public ProcessStatus getStatus(long val) {
            for (ProcessStatus e : ProcessStatus.values()) {
                if (e.value() == val) {
                    return e;
                }
            }
            return UNKNOWN;
        }
    }

    /**
     * Constructor
     *
     * @param trace
     *            The trace on which to build graph
     */
    public LttngKernelExecGraphProvider(ITmfTrace trace) {
        super(trace, "LTTng Kernel"); //$NON-NLS-1$
        fSystem = new LttngSystemModel();

        /*
         * TODO: factorize this code because it is duplicated everywhere to
         * access layout
         */
        if (trace instanceof LttngKernelTrace) {

            fEventLayout = (LttngEventLayout) ((LttngKernelTrace) trace).getKernelEventLayout();
        } else {
            /* Fall-back to the base LttngEventLayout */
            fEventLayout = (LttngEventLayout) LttngEventLayout.getInstance();
        }

        registerHandler(new TraceEventHandlerStatedump(this));
        registerHandler(new TraceEventHandlerSched(this));
        registerHandler(new EventContextHandler(this));
        registerHandler(new TraceEventHandlerExecutionGraph(this));
    }

    /**
     * Simplify graph after construction
     */
    @Override
    public void done() {
        TmfGraph graph = getAssignedGraph();
        if (graph == null) {
            throw new NullPointerException();
        }
        Set<IGraphWorker> keys = graph.getWorkers();
        List<LttngWorker> kernelWorker = new ArrayList<>();
        /* build the set of worker to eliminate */
        for (Object k : keys) {
            if (k instanceof LttngWorker) {
                LttngWorker w = (LttngWorker) k;
                if (w.getHostThread().getTid() == -1) {
                    kernelWorker.add(w);
                }
            }
        }
        for (LttngWorker k : kernelWorker) {
            List<TmfVertex> nodes = graph.getNodesOf(k);
            for (TmfVertex node : nodes) {
                /*
                 * send -> recv, it removes the vertex between the real source
                 * and destination
                 */
                TmfEdge nextH = node.getEdge(EdgeDirection.OUTGOING_HORIZONTAL_EDGE);
                TmfEdge inV = node.getEdge(EdgeDirection.INCOMING_VERTICAL_EDGE);
                if (inV != null && nextH != null) {

                    TmfVertex next = nextH.getVertexTo();
                    TmfEdge nextV = next.getEdge(EdgeDirection.OUTGOING_VERTICAL_EDGE);
                    if (nextV != null) {
                        TmfVertex src = inV.getVertexFrom();
                        TmfVertex dst = nextV.getVertexTo();

                        /* unlink */
                        node.removeEdge(EdgeDirection.INCOMING_VERTICAL_EDGE);
                        next.removeEdge(EdgeDirection.OUTGOING_VERTICAL_EDGE);

                        /* simplified link */
                        src.linkVertical(dst).setType(inV.getType());
                    }
                }
            }
        }
    }

    /**
     * Returns the event layout for the current trace
     *
     * @return the eventLayout
     */
    public LttngEventLayout getEventLayout() {
        return fEventLayout;
    }

    /**
     * Returns the system model
     *
     * @return the system
     */
    public LttngSystemModel getSystem() {
        return fSystem;
    }

}
