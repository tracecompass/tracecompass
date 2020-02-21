/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.graph.core.building;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

/**
 * Base class for graph providers. It implements most methods common for all
 * graph builder.
 *
 * @author Geneviève Bastien
 * @author Francis Giraldeau
 */
public abstract class AbstractTmfGraphProvider implements ITmfGraphProvider {

    private static final Comparator<ITraceEventHandler> HANDLER_COMPARATOR = (@Nullable ITraceEventHandler o1, @Nullable ITraceEventHandler o2) -> {
        if (o1 == null) {
            return 1;
        }
        if (o2 == null) {
            return -1;
        }
        int res = Integer.compare(o1.getPriority(), o2.getPriority());
        // If the handlers have the same priority, arbitrarily compare their names
        return (res != 0 ? res : o1.getClass().getName().compareTo(o2.getClass().getName()));
    };

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
        return fTrace.getStartTime().toNanos();
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
        int pos = Collections.binarySearch(fHandlers, handler, HANDLER_COMPARATOR);
        if (pos < 0) {
            fHandlers.add(-pos - 1, handler);
        }
        // If pos >= 0, the handler is already in the list
    }

    /**
     * Get the list of handlers for this class. Used for unit testing only.
     *
     * @return The list of handlers
     * @since 1.2
     */
    @VisibleForTesting
    protected List<ITraceEventHandler> getHandlers() {
        return ImmutableList.copyOf(fHandlers);
    }

}
