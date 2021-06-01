/**********************************************************************
 * Copyright (c) 2018, 2021 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.tmf.core.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * Element Resolver, can be used to convert an element to a set of key value
 * pairs.
 * <p>
 * Possible key strings to use in the map are provider by this interface
 * <p>
 * Candidates to use this are
 * <ul>
 * <li>States</li>
 * <li>Events</li>
 * <li>Colors</li>
 * <li>...</li>
 * </ul>
 *
 * @author Jean-Christian Kouame
 * @author Bernd Hufmann
 * @since 7.0
 */
public interface ICoreElementResolver {

    /**
     * Get the metadata for this data model. The keys are the names of the
     * metadata field or aspect. A field may have multiple values associated
     * with it.
     *
     * @return A map of field names to values
     * @since 5.0
     */
    Multimap<@NonNull String, @NonNull Object> getMetadata();

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
    static boolean commonIntersect(Multimap<String, Object> data1, Multimap<String, Object> data2) {
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