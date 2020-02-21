/*******************************************************************************
 * Copyright (c) 2014, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

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

    /**
     * Gets a map from event name to a collection of field names from a
     * collection of event types
     *
     * @param eventTypes
     *            an iterable collection of ITmfEventTypes
     * @return a set of the names of these events, if some event names are
     *         clashing they will only appear once
     * @since 2.0
     */
    public static Multimap<@NonNull String, @NonNull String> getEventFieldNames(Iterable<@NonNull ? extends ITmfEventType> eventTypes) {
        Multimap<@NonNull String, @NonNull String> retMap = HashMultimap.create();
        eventTypes.forEach(eventType -> {
            Collection<String> collection = eventType.getFieldNames();
            if (collection != null) {
                collection.forEach(field -> {
                    if (field != null) {
                        retMap.put(eventType.getName(), field);
                    }
                });
            }
        });
        return retMap;
    }

}
