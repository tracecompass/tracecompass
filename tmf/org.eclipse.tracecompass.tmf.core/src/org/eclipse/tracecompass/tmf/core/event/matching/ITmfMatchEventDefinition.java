/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.matching;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventMatching.Direction;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * This interface describe a concrete method to match events. Typically it
 * manages for a given matching type what events/fields are used to match events
 *
 * @author Geneviève Bastien
 */
public interface ITmfMatchEventDefinition {

    /**
     * Returns a key for an event that uniquely identifies this event and will
     * match the key from another event.
     *
     * @param event
     *            The event for which to compute the key
     * @return the unique key for this event
     */
    IEventMatchingKey getEventKey(ITmfEvent event);

    /**
     * Verifies whether a trace has all required events to match using this
     * class
     *
     * @param trace
     *            The trace
     * @return Whether the trace has all required information
     */
    boolean canMatchTrace(ITmfTrace trace);

    /**
     * Returns the direction of this event, whether 'send', 'receive' or null if
     * event is neither
     *
     * @param event
     *            The event to check
     * @return The direction of this event, null if uninteresting event
     * @since 1.0
     */
    Direction getDirection(ITmfEvent event);

}
