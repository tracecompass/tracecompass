/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.graph.core.building;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Interface that event handlers must implement. Event handlers are independent
 * actions that need to be performed for an analysis phase
 *
 * @author Francis Giraldeau
 * @author Geneviève Bastien
 */
public interface ITraceEventHandler {

    /**
     * Handles an event
     *
     * @param event
     *            The event to handle
     */
    void handleEvent(ITmfEvent event);

    /**
     * Indicate if this handler is cancelled
     *
     * @return true if this handler is cancelled
     */
    boolean isCancelled();

    /**
     * Cancels this event handler
     */
    void cancel();

    /**
     * Get a priority level associated with this handler. The lower the number, the
     * higher the priority.
     *
     * @return The priority level associated with this handler
     * @since 1.2
     */
    default int getPriority() {
        return 10;
    }

}
