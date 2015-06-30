/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francis Giraldeau - Initial implementation and API
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.graph.core.base;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.graph.core.base.IGraphWorker;
import org.eclipse.tracecompass.analysis.graph.core.base.ITmfGraphVisitor;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex;
import org.eclipse.tracecompass.common.core.NonNullUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Class that computes statistics on time spent in the elements (objects) of a
 * graph
 *
 * @author Francis Giraldeau
 * @author Geneviève Bastien
 */
public class TmfGraphStatistics implements ITmfGraphVisitor {

    private static final String STATS_TOTAL = "total"; //$NON-NLS-1$

    private final Multimap<Object, Long> fWorkerStats;
    private @Nullable TmfGraph fGraph;

    /**
     * Constructor
     */
    public TmfGraphStatistics() {
        fWorkerStats = NonNullUtils.checkNotNull(ArrayListMultimap.<Object, Long> create());
    }

    /**
     * Compute the statistics for a graph
     *
     * @param graph
     *            The graph on which to calculate statistics
     * @param current
     *            The element from which to start calculations
     */
    public void getGraphStatistics(TmfGraph graph, @Nullable IGraphWorker current) {
        if (current == null) {
            return;
        }
        fGraph = graph;
        fGraph.scanLineTraverse(fGraph.getHead(current), this);
    }

    @Override
    public void visitHead(TmfVertex node) {

    }

    @Override
    public void visit(TmfVertex node) {

    }

    @Override
    public void visit(TmfEdge edge, boolean horizontal) {
        // Add the duration of the link only if it is horizontal
        TmfGraph graph = fGraph;
        synchronized (fWorkerStats) {
            if (horizontal && graph != null) {
                fWorkerStats.put(graph.getParentOf(edge.getVertexFrom()),
                        edge.getVertexTo().getTs() - edge.getVertexFrom().getTs());
                fWorkerStats.put(STATS_TOTAL,
                        edge.getVertexTo().getTs() - edge.getVertexFrom().getTs());
            }
        }
    }

    /**
     * Get the total duration spent by one element of the graph
     *
     * @param worker
     *            The object to get the time spent for
     * @return The sum of all durations
     */
    public Long getSum(@Nullable Object worker) {
        long sum = 0L;
        synchronized (fWorkerStats) {
            for (long duration : fWorkerStats.get(worker)) {
                sum += duration;
            }
        }
        return sum;
    }

    /**
     * Get the total duration of the graph vertices
     *
     * @return The sum of all durations
     */
    public Long getSum() {
        return getSum(STATS_TOTAL);
    }

    /**
     * Get the percentage of time by one element of the graph
     *
     * @param worker
     *            The object to get the percentage for
     * @return The percentage time spent in this element
     */
    public Double getPercent(@Nullable Object worker) {
        if (getSum() == 0) {
            return 0D;
        }
        return (double) getSum(worker) / (double) getSum();
    }

}
