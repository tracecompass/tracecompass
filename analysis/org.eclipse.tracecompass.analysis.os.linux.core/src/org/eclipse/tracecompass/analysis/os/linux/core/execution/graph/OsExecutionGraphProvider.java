/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.execution.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.tracecompass.analysis.graph.core.base.IGraphWorker;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex.EdgeDirection;
import org.eclipse.tracecompass.analysis.graph.core.building.AbstractTmfGraphProvider;
import org.eclipse.tracecompass.analysis.graph.core.building.ITraceEventHandler;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.DefaultEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.Activator;
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
 * @since 2.4
 */
public class OsExecutionGraphProvider extends AbstractTmfGraphProvider {

    /** Extension point ID */
    private static final String TMF_GRAPH_HANDLER_ID = "org.eclipse.tracecompass.analysis.os.linux.core.graph.handler"; //$NON-NLS-1$
    private static final String HANDLER = "handler"; //$NON-NLS-1$
    private static final String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$
    private static final String ATTRIBUTE_PRIORITY = "priority"; //$NON-NLS-1$
    private static final int DEFAULT_PRIORITY = 10;

    private final OsSystemModel fSystem;

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
        HRTIMER,
        /** The inter-processor interrupt */
        IPI,
    }

    /**
     * A list of status a thread can be in
     *
     * @deprecated Use {@link org.eclipse.tracecompass.analysis.os.linux.core.model.ProcessStatus} instead
     */
    @Deprecated
    public enum ProcessStatus {
        /** Unknown process status */
        UNKNOWN(0),
        /** Waiting for a fork */
        WAIT_FORK(1),
        /** Waiting for the CPU */
        WAIT_CPU(2),
        /** The thread has exited, but is not dead yet
         * @since 2.3*/
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
    public OsExecutionGraphProvider(ITmfTrace trace) {
        super(trace, "LTTng Kernel"); //$NON-NLS-1$
        fSystem = new OsSystemModel();

        IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(TMF_GRAPH_HANDLER_ID);
        for (IConfigurationElement ce : config) {
            String elementName = ce.getName();
            if (HANDLER.equals(elementName)) {
                IOsExecutionGraphHandlerBuilder builder;
                try {
                    builder = (IOsExecutionGraphHandlerBuilder) ce.createExecutableExtension(ATTRIBUTE_CLASS);
                } catch (CoreException e1) {
                    Activator.getDefault().logWarning("Error create execution graph handler builder", e1); //$NON-NLS-1$
                    continue;
                }
                String priorityStr = ce.getAttribute(ATTRIBUTE_PRIORITY);
                int priority = DEFAULT_PRIORITY;
                try {
                    priority = Integer.valueOf(priorityStr);
                } catch (NumberFormatException e) {
                    // Nothing to do, use default value
                }
                ITraceEventHandler handler = builder.createHandler(this, priority);
                registerHandler(handler);
            }
        }
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
        List<OsWorker> kernelWorker = new ArrayList<>();
        /* build the set of worker to eliminate */
        for (Object k : keys) {
            if (k instanceof OsWorker) {
                OsWorker w = (OsWorker) k;
                if (w.getHostThread().getTid() == -1) {
                    kernelWorker.add(w);
                }
            }
        }
        for (OsWorker k : kernelWorker) {
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
     * Returns the event layout for the given trace
     *
     * @param trace
     *            the trace
     *
     * @return the eventLayout
     */
    public IKernelAnalysisEventLayout getEventLayout(ITmfTrace trace) {
        if (trace instanceof IKernelTrace) {
            return ((IKernelTrace) trace).getKernelEventLayout();
        }
        return DefaultEventLayout.getInstance();
    }

    /**
     * Returns the system model
     *
     * @return the system
     */
    public OsSystemModel getSystem() {
        return fSystem;
    }

}
