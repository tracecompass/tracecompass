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

/**
 * Base class for event handlers, implementing common behavior like cancellation
 *
 * @author Geneviève Bastien
 * @author Francis Giraldeau
 */
public abstract class AbstractTraceEventHandler implements ITraceEventHandler {

    private volatile boolean fHandlerCancelled = false;
    private final int fPriority;

    /**
     * Constructor with priority
     *
     * @param priority The priority of this handler
     * @since 1.2
     */
    public AbstractTraceEventHandler(int priority) {
        fPriority = priority;
    }

    @Override
    public boolean isCancelled() {
        return fHandlerCancelled;
    }

    @Override
    public void cancel() {
        fHandlerCancelled = true;
    }

    @Override
    public int getPriority() {
        return fPriority;
    }

}
