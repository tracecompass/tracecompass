/**********************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.dataprovider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Utility class to deal with data providers parameters. Provides method to
 * extract the most common parameters from a map.
 *
 * @author Simon Delisle
 * @since 5.0
 */
@NonNullByDefault
public class DataProviderParameterUtils {

    /**
     * Time requested key
     */
    public static final String REQUESTED_TIME_KEY = "requested_times"; //$NON-NLS-1$

    /**
     * Selected items key
     */
    public static final String REQUESTED_ITEMS_KEY = "requested_items"; //$NON-NLS-1$

    /**
     * Virtual table count key
     */
    public static final String REQUESTED_TABLE_COUNT_KEY = "requested_table_count"; //$NON-NLS-1$

    /**
     * Virtual table starting index key
     */
    public static final String REQUESTED_TABLE_INDEX_KEY = "requested_table_index"; //$NON-NLS-1$

    /**
     * Table column IDs key
     */
    public static final String REQUESTED_COLUMN_IDS_KEY = "requested_table_column_ids"; //$NON-NLS-1$

    /**
     * Requested element key
     *
     * @since 5.2
     */
    public static final String REQUESTED_ELEMENT_KEY = "requested_element"; //$NON-NLS-1$

    /**
     * Requested marker set key
     * @since 7.1
     */
    public static final String REQUESTED_MARKER_SET_KEY = "requested_marker_set"; //$NON-NLS-1$

    /**
     * Requested marker categories key
     * @since 7.1
     */
    public static final String REQUESTED_MARKER_CATEGORIES_KEY = "requested_marker_categories"; //$NON-NLS-1$

    /**
     * Requested trace key, uses the hostID, as the time is the same for every
     * trace with the same host id.
     *
     * @since 7.1
     */
    public static final String REQUESTED_TRACE_KEY = "requested_trace"; //$NON-NLS-1$

    /**
     * Key to extract isFiltered from parameters map
     */
    public static final String FILTERED_KEY = "isFiltered"; //$NON-NLS-1$

    /**
     * Regex filter key
     */
    public static final String REGEX_MAP_FILTERS_KEY = "regex_map_filters"; //$NON-NLS-1$

    /**
     * Key to indicate a full search. When set to {@link Boolean#TRUE}, the data
     * provider is expected to perform a full search of elements that occur
     * between the usual elements found at the requested times (e.g. in gaps).
     * If any element matches the specified regex filter, at least one such
     * matching element per gap should be returned. It is not expected to return
     * all matching elements found in a single gap, for performance reasons.
     *
     * @since 5.2
     */
    public static final String FULL_SEARCH_KEY = "full_search"; //$NON-NLS-1$

    private DataProviderParameterUtils() {
        // Private constructor
    }

    /**
     * Extract list of Long from a map of parameters
     *
     * @param parameters
     *            Map of parameters
     * @param key
     *            Parameter key for the value to extract
     * @return List of Long or null if it fails to extract
     */
    public static @Nullable List<Long> extractLongList(Map<String, Object> parameters, String key) {
        Object collectionObject = parameters.get(key);
        if (collectionObject instanceof Collection<?>) {
            return transformToLongList((Collection<?>) collectionObject);
        }
        return null;
    }

    /**
     * Extract boolean value from a map of parameters
     *
     * @param parameters
     *            Map of parameters
     * @param key
     *            Parameter key for the value to extract
     * @return boolean value for this key or null if it fails to extract
     */
    public static @Nullable Boolean extractBoolean(Map<String, Object> parameters, String key) {
        Object booleanObject = parameters.get(key);
        if (booleanObject instanceof Boolean) {
            return (Boolean) booleanObject;
        }
        return null;
    }

    /**
     * Helper to extract time requested from a map of parameters
     *
     * @param parameters
     *            Map of parameters
     * @return List of times or null if no time requested in the map
     */
    public static @Nullable List<Long> extractTimeRequested(Map<String, Object> parameters) {
        return extractLongList(parameters, REQUESTED_TIME_KEY);
    }

    /**
     * Helper to extract selected items from a map of parameters
     *
     * @param parameters
     *            Map of parameters
     * @return List of selected items or null if no selected items in the map
     */
    public static @Nullable List<Long> extractSelectedItems(Map<String, Object> parameters) {
        return extractLongList(parameters, REQUESTED_ITEMS_KEY);
    }

    /**
     * Helper to extract isFiltered from a map of parameters
     *
     * @param parameters
     *            Map of parameters
     * @return True or false if it should be filtered or null if no isFiltered
     *         parameter
     */
    public static @Nullable Boolean extractIsFiltered(Map<String, Object> parameters) {
        return extractBoolean(parameters, FILTERED_KEY);
    }

    /**
     * Helper to extract a Multimap of regexes from a map of parameters
     *
     * @param parameters
     *            Map of parameters
     * @return Multimap of regexes or null if there is no regex key in the map
     *         of parameters
     */
    public static @Nullable Multimap<Integer, String> extractRegexFilter(Map<String, Object> parameters) {
        Object regexesObject = parameters.get(REGEX_MAP_FILTERS_KEY);
        if (!(regexesObject instanceof Map<?, ?>)) {
            return null;
        }

        Multimap<Integer, String> regexes = HashMultimap.create();
        Map<Integer, Collection<String>> regexesMap = (Map<Integer, Collection<String>>) regexesObject;
        for (Entry<Integer, Collection<String>> entry : regexesMap.entrySet()) {
            regexes.putAll(entry.getKey(), entry.getValue());
        }

        return regexes;
    }

    /**
     * Extract selected marker categories from fetch parameters. If parameter is not in the map
     * then return null.
     *
     * @param fetchParams
     *            Map of parameters
     * @return set of selected categories or null if parameter is not in parameter map
     * @since 7.1
     */
    public static @Nullable Set<String> extractSelectedCategories(Map<String, Object> fetchParams) {
        Set<String> categories = null;
        Object fetched = fetchParams.get(DataProviderParameterUtils.REQUESTED_MARKER_CATEGORIES_KEY);
        if (fetched instanceof Iterable<?>) {
            categories = new HashSet<>();
            for (Object key : (Iterable<?>) fetched) {
                if (key != null) {
                    categories.add(String.valueOf(key));
                }
            }
        }
        return categories;
    }

    /**
     * Transform a Collection<?> to a List<Long>, where ? is Integer or Long.
     *
     * @param collectionToTransform
     *            Collection to transform
     * @return List<Long> or null if the collection can not be transformed
     */
    @SuppressWarnings("unchecked")
    private static @Nullable List<Long> transformToLongList(Collection<?> collectionToTransform) {
        if (!collectionToTransform.isEmpty()) {
            if (collectionToTransform instanceof List<?> && collectionToTransform.stream().allMatch(Long.class::isInstance)) {
                return (List<Long>) collectionToTransform;
            } else if (collectionToTransform.stream().allMatch(Integer.class::isInstance)) {
                List<Long> list = new ArrayList<>();
                for (Integer element : (List<Integer>) collectionToTransform) {
                    list.add(element.longValue());
                }
                return list;
            } else {
                List<Long> list = new ArrayList<>();
                for (Object element : collectionToTransform) {
                    if (!(element instanceof Number)) {
                        return null;
                    }
                    list.add(((Number) element).longValue());
                }
                return list;
            }
        }
        return Collections.emptyList();
    }
}
