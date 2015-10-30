/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;

/**
 * Set Helper for sets of ITmfTraceType
 *
 * TODO Remove once Java 8 is used (replace with Streams)
 *
 * @author Matthew Khouzam
 */
public final class TmfEventTypeCollectionHelper {

    private TmfEventTypeCollectionHelper() {
    }

    /**
     * Gets the event names from a collection of event types
     *
     * @param eventTypes
     *            an iterable collection of ITmfEventTypes
     * @return a set of the names of these events, if some names are clashing
     *         they will only appear once
     */
    public static Set<@NonNull String> getEventNames(Iterable<@NonNull ? extends ITmfEventType> eventTypes) {
        Set<@NonNull String> retSet = new HashSet<>();
        for (ITmfEventType eventType : eventTypes) {
            retSet.add(eventType.getName());
        }
        return retSet;
    }
}
