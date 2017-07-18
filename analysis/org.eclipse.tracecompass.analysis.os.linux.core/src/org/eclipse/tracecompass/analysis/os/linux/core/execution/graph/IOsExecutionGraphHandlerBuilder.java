/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.execution.graph;

import org.eclipse.tracecompass.analysis.graph.core.building.ITraceEventHandler;

/**
 * Interface that should be implemented for each graph handler. These classes
 * will be initialized with a default constructor by the extension points and
 * will create the actual handlers with the graph provider.
 *
 * @author Geneviève Bastien
 * @since 2.4
 */
public interface IOsExecutionGraphHandlerBuilder {

    /**
     * Create an actual event handler for the given provider
     *
     * @param provider
     *            The execution graph provider
     * @param priority
     *            The priority level that this level should have
     * @return The event handler
     */
    ITraceEventHandler createHandler(OsExecutionGraphProvider provider, int priority);
}
