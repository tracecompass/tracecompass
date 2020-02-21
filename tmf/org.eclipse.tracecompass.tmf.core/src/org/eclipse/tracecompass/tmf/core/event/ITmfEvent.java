/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * The generic event structure in TMF. In its canonical form, an event has:
 * <ul>
 * <li>a parent trace
 * <li>a rank (order within the trace)
 * <li>a timestamp
 * <li>a type
 * <li>a content (payload)
 * </ul>
 *
 * @author Francois Chouinard
 *
 * @see ITmfTimestamp
 * @see ITmfEventType
 * @see ITmfEventField
 * @see TmfEvent
 */
public interface ITmfEvent extends IAdaptable {

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the trace that 'owns' the event
     */
    @NonNull ITmfTrace getTrace();

    /**
     * @return the event rank within the parent trace
     */
    long getRank();

    /**
     * @return the event timestamp
     */
    @NonNull ITmfTimestamp getTimestamp();

    /**
     * @return the event type
     */
    ITmfEventType getType();

    /**
     * @return the event content
     */
    ITmfEventField getContent();

    /**
     * Gets the name of the event
     *
     * @return the name of the event, same as getType().getName()
     * @since 1.0
     */
    @NonNull String getName();
}
