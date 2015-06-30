/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.graph.core.building;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Base class for graph providers. It implements most methods common for all
 * graph builder.
 *
 * @author Geneviève Bastien
 * @author Francis Giraldeau
 */
public abstract class AbstractTmfGraphProvider implements ITmfGraphProvider {

    private final ITmfTrace fTrace;

    private final List<ITraceEventHandler> fHandlers;

    private boolean fGraphAssigned;

    /** Graph in which to insert the state changes */
    private @Nullable TmfGraph fGraph = null;

    /**
     * Instantiate a new graph builder plugin.
     *
     * @param trace
     *            The trace
     * @param id
     *            Name given to this state change input. Only used internally.
     */
    public AbstractTmfGraphProvider(ITmfTrace trace, String id) {
        this.fTrace = trace;
        fGraphAssigned = false;
        fHandlers = new ArrayList<>();
    }

    @Override
    public ITmfTrace getTrace() {
        return fTrace;
    }

    @Override
    public long getStartTime() {
        return fTrace.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
    }

    @Override
    public void assignTargetGraph(TmfGraph graph) {
        fGraph = graph;
        fGraphAssigned = true;
    }

    @Override
    public @Nullable TmfGraph getAssignedGraph() {
        return fGraph;
    }

    @Override
    public void processEvent(ITmfEvent event) {
        /* Make sure the target graph has been assigned */
        if (!fGraphAssigned) {
            return;
        }
        eventHandle(event);
    }

    @Override
    public void dispose() {
        fGraphAssigned = false;
        fGraph = null;
    }

    @Override
    public void done() {
    }

    /**
     * Internal event handler, using the phase's handlers
     *
     * @param event
     *            The event
     */
    protected void eventHandle(ITmfEvent event) {
        for (ITraceEventHandler handler : fHandlers) {
            handler.handleEvent(event);
        }
    }

    @Override
    public void handleCancel() {
    }

    /**
     * Register a handler to a series of events
     *
     * @param handler
     *            The trace event handler
     */
    public void registerHandler(ITraceEventHandler handler) {
        fHandlers.add(handler);
    }

}
