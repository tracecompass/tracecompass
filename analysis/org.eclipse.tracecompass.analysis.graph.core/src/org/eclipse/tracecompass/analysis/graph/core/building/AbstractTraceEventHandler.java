/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
     * Default constructor
     * @deprecated Use the constructor with priority instead
     */
    @Deprecated
    public AbstractTraceEventHandler() {
        fPriority = 5;
    }

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
