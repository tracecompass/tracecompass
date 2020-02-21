/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.tracecompass.tmf.core.model.timegraph.IElementResolver;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * This interface can be implemented by classes whose model elements have
 * additional metadata that can be used to filter them
 *
 * @since 4.2
 * @deprecated This interface is now totally included in
 *             {@link IElementResolver}. One may just change the implemented
 *             interface, the methods have the same names.
 */
@Deprecated
public interface IFilterableDataModel {

    /**
     * Get the metadata for this data model. The keys are the names of the
     * metadata field or aspect. A field may have multiple values associated
     * with it.
     *
     * @return A map of field names to values
     */
    Multimap<String, String> getMetadata();

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
