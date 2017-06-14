/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.graph.core.criticalpath;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tracecompass.analysis.graph.core.base.IGraphWorker;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex;
import org.eclipse.tracecompass.analysis.graph.core.building.TmfGraphBuilderModule;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.graph.core.Activator;
import org.eclipse.tracecompass.internal.analysis.graph.core.criticalpath.CriticalPathAlgorithmBounded;
import org.eclipse.tracecompass.internal.analysis.graph.core.criticalpath.Messages;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * Class to implement the critical path analysis
 *
 * @author Francis Giraldeau
 * @author Geneviève Bastien
 */
public class CriticalPathModule extends TmfAbstractAnalysisModule implements ICriticalPathProvider {

    /**
     * Analysis ID for this module
     */
    public static final String ANALYSIS_ID = "org.eclipse.tracecompass.analysis.graph.core.criticalpath"; //$NON-NLS-1$

    /** Graph parameter name */
    public static final String PARAM_GRAPH = "graph"; //$NON-NLS-1$

    /** Worker_id parameter name */
    public static final String PARAM_WORKER = "workerid"; //$NON-NLS-1$

    private @Nullable TmfGraphBuilderModule fGraphModule;

    private volatile @Nullable TmfGraph fCriticalPath;

    /**
     * Default constructor
     */
    public CriticalPathModule() {
        super();
        addParameter(PARAM_GRAPH);
        addParameter(PARAM_WORKER);
        setId(ANALYSIS_ID);
    }

    /**
     * Default constructor
     *
     * @param graph
     *            The graph module that will be used to calculate the critical
     *            path on
     * @since 1.1
     */
    public CriticalPathModule(TmfGraphBuilderModule graph) {
        super();
        addParameter(PARAM_GRAPH);
        addParameter(PARAM_WORKER);
        setId(ANALYSIS_ID);
        fGraphModule = graph;
    }

    @Override
    protected boolean executeAnalysis(final IProgressMonitor monitor) throws TmfAnalysisException {
        /* Get the graph */
        TmfGraphBuilderModule graphModule = getGraph();
        if (graphModule == null) {
            Activator.getInstance().logWarning("No graph was found to execute the critical path on"); //$NON-NLS-1$
            return true;
        }
        graphModule.schedule();

        monitor.setTaskName(NLS.bind(Messages.CriticalPathModule_waitingForGraph, graphModule.getName()));
        if (!graphModule.waitForCompletion(monitor)) {
            Activator.getInstance().logInfo("Critical path execution: graph building was cancelled.  Results may not be accurate."); //$NON-NLS-1$
            return false;
        }
        TmfGraph graph = graphModule.getGraph();
        if (graph == null) {
            throw new TmfAnalysisException("Critical Path analysis: graph " + graphModule.getName() + " is null"); //$NON-NLS-1$//$NON-NLS-2$
        }

        /* Get the worker id */
        Object workerObj = getParameter(PARAM_WORKER);
        if (workerObj == null) {
            return false;
        }
        if (!(workerObj instanceof IGraphWorker)) {
            throw new IllegalStateException("Worker parameter must be an IGraphWorker"); //$NON-NLS-1$
        }
        IGraphWorker worker = (IGraphWorker) workerObj;

        TmfVertex head = graph.getHead(worker);
        if (head == null) {
            /* Nothing happens with this worker, return an empty graph */
            fCriticalPath = new TmfGraph();
            return true;
        }

        ICriticalPathAlgorithm cp = getAlgorithm(graph);
        try {
            fCriticalPath = cp.compute(head, null);
            return true;
        } catch (CriticalPathAlgorithmException e) {
            Activator.getInstance().logError(NonNullUtils.nullToEmptyString(e.getMessage()), e);
        }
        return false;
    }

    @Override
    protected void canceling() {

    }

    @Override
    public @Nullable synchronized Object getParameter(String name) {
        if (name.equals(PARAM_GRAPH)) {
            return getGraph();
        }
        return super.getParameter(name);
    }

    @Override
    public synchronized void setParameter(String name, @Nullable Object value) throws RuntimeException {
        if (name.equals(PARAM_GRAPH) && (value instanceof String)) {
            setGraph((String) value);
        }
        super.setParameter(name, value);
    }

    @Override
    protected void parameterChanged(String name) {
        fCriticalPath = null;
        cancel();
        resetAnalysis();
        schedule();
    }

    /**
     * The value of graph should be the id of the analysis module that builds
     * the required graph
     *
     * @param graphName
     *            Id of the graph
     */
    private void setGraph(String graphName) {
        ITmfTrace trace = getTrace();
        if (trace == null) {
            return;
        }
        IAnalysisModule module = trace.getAnalysisModule(graphName);
        if (module instanceof TmfGraphBuilderModule) {
            fGraphModule = (TmfGraphBuilderModule) module;
        }
    }

    private @Nullable TmfGraphBuilderModule getGraph() {
        /* The graph module is null, take the first available graph if any */
        TmfGraphBuilderModule module = fGraphModule;
        if (module == null) {
            ITmfTrace trace = getTrace();
            if (trace == null) {
                return fGraphModule;
            }
            for (TmfGraphBuilderModule mod : TmfTraceUtils.getAnalysisModulesOfClass(trace, TmfGraphBuilderModule.class)) {
                module = mod;
                break;
            }
            if (module != null) {
                fGraphModule = module;
            }
        }
        return module;
    }

    private static ICriticalPathAlgorithm getAlgorithm(TmfGraph graph) {
        return new CriticalPathAlgorithmBounded(graph);
    }

    @Override
    public boolean canExecute(@NonNull ITmfTrace trace) {
        /*
         * TODO: The critical path executes on a graph, so at least a graph must
         * be available for this trace
         */
        return true;
    }

    /**
     * Gets the graph for the critical path
     *
     * @return The critical path graph
     */
    @Override
    public @Nullable TmfGraph getCriticalPath() {
        return fCriticalPath;
    }

    @Override
    protected @NonNull String getFullHelpText() {
        return NonNullUtils.nullToEmptyString(Messages.CriticalPathModule_fullHelpText);
    }

    @Override
    protected @NonNull String getShortHelpText(ITmfTrace trace) {
        return getFullHelpText();
    }

    @Override
    protected @NonNull String getTraceCannotExecuteHelpText(@NonNull ITmfTrace trace) {
        return NonNullUtils.nullToEmptyString(Messages.CriticalPathModule_cantExecute);
    }

}
