/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.tmf.core.model.timegraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * Element Resolver, can be used to convert an element to a set of key value
 * pairs.
 *
 * Possible key strings to use in the map are provider by this interface
 *
 * Candidates to use this are
 * <ul>
 * <li>States</li>
 * <li>Events</li>
 * <li>Colors</li>
 * <li>...</li>
 * </ul>
 *
 * @author Jean-Christian Kouame
 * @since 4.0
 */
public interface IElementResolver {

    /**
     * The key to get the label
     */
    static final String LABEL_KEY= "label"; //$NON-NLS-1$

    /**
     * the key to get the entry name
     */
    static final String ENTRY_NAME_KEY = "entry"; //$NON-NLS-1$

    /**
     * Get available information from an item and return it into a key-value map
     *
     * @return The map of data
     * @deprecated Use the {@link #getMetadata()} instead
     */
    @Deprecated
    default Map<@NonNull String, @NonNull String> computeData() {
        Map<String, String> map = new HashMap<>();
        Multimap<String, String> metadata = getMetadata();
        for (Entry<String, String> entry : metadata.entries()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    /**
     * Get the metadata for this data model. The keys are the names of the
     * metadata field or aspect. A field may have multiple values associated
     * with it.
     *
     * @return A map of field names to values
     * @since 5.0
     */
    Multimap<@NonNull String, @NonNull String> getMetadata();

    /**
     * Compare 2 sets of metadata to see if the second intersects the first. 2
     * sets of metadata are said to intersect if they have at least one key in
     * common and for each key that they have in common, they have at least one
     * value in common.
     *
     * @param data1
     *            The first set of metadata to compare
     * @param data2
     *            The second set of metadata to compare
     * @return Whether the 2 metadata sets coincides
     * @since 5.0
     */
    static boolean commonIntersect(Multimap<String, String> data1, Multimap<String, String> data2) {
        Set<String> commonKeys = new HashSet<>(data1.keySet());
        commonKeys.retainAll(data2.keySet());
        if (commonKeys.isEmpty()) {
            return false;
        }
        for (String commonKey : commonKeys) {
            if (!Iterables.any(data1.get(commonKey), v -> data2.get(commonKey).contains(v))) {
                return false;
            }
        }
        return true;

    }
}