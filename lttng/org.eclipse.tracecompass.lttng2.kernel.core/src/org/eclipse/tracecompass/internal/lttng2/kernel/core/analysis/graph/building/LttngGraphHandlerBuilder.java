/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.building;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.graph.core.building.ITraceEventHandler;
import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.IOsExecutionGraphHandlerBuilder;
import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsExecutionGraphProvider;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.handlers.EventContextHandler;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.handlers.TraceEventHandlerExecutionGraph;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.handlers.TraceEventHandlerSched;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.handlers.TraceEventHandlerStatedump;

/**
 * The handler builders for and LTTng kernel trace
 *
 * @author Geneviève Bastien
 */
public final class LttngGraphHandlerBuilder {

    private LttngGraphHandlerBuilder() {
        // Nothing to do
    }

    /**
     * The handler builder for the event context handler
     */
    public static class HandlerBuilderEventContext implements IOsExecutionGraphHandlerBuilder {

        @Override
        public ITraceEventHandler createHandler(@NonNull OsExecutionGraphProvider provider, int priority) {
            return new EventContextHandler(provider, priority);
        }

    }

    /**
     * The handler builder for the execution graph handler
     */
    public static class HandlerBuilderExecutionGraph implements IOsExecutionGraphHandlerBuilder {

        @Override
        public ITraceEventHandler createHandler(@NonNull OsExecutionGraphProvider provider, int priority) {
            return new TraceEventHandlerExecutionGraph(provider, priority);
        }

    }

    /**
     * The handler builder for the scheduler graph handler
     */
    public static class HandlerBuilderSched implements IOsExecutionGraphHandlerBuilder {

        @Override
        public ITraceEventHandler createHandler(@NonNull OsExecutionGraphProvider provider, int priority) {
            return new TraceEventHandlerSched(provider, priority);
        }

    }

    /**
     * The handler builder for the statedump event handler
     */
    public static class HandlerBuilderStatedump implements IOsExecutionGraphHandlerBuilder {

        @Override
        public ITraceEventHandler createHandler(@NonNull OsExecutionGraphProvider provider, int priority) {
            return new TraceEventHandlerStatedump(provider, priority);
        }

    }
}
