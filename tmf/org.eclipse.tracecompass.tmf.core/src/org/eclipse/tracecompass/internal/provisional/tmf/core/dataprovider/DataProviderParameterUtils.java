/**********************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.dataprovider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.model.filters.FilterTimeQueryFilter;

/**
 * @author Simon Delisle
 */
public class DataProviderParameterUtils {

    /**
     * Time requested key
     */
    public static final String TIME_REQUESTED_KEY = "timeRequested"; //$NON-NLS-1$

    /**
     * Selected items key
     */
    public static final String SELECTED_ITEMS_KEY = "items"; //$NON-NLS-1$

    /**
     * Key to extract isFiltered from parameters map
     */
    public static final String FILTERED_PARAMETER_KEY = "isFiltered"; //$NON-NLS-1$

    /**
     * Virtual table count key
     */
    public static final String TABLE_COUNT = "count"; //$NON-NLS-1$

    /**
     * Virtual table starting index key
     */
    public static final String TABLE_INDEX = "index"; //$NON-NLS-1$

    /**
     * Table column IDs key
     */
    public static final String COLUMN_ID_KEY = "columnId"; //$NON-NLS-1$

    private DataProviderParameterUtils() {
        // Private constructor
    }

    /**
     * Extract time requested from a map of parameters
     *
     * @param parameters
     *            Map of parameters
     * @return List of times or null if no time requested in the map
     */
    public static @Nullable List<Long> extractTimeRequested(Map<String, Object> parameters) {
        Object timeRequestedObject = parameters.get(TIME_REQUESTED_KEY);
        if (timeRequestedObject instanceof List<?>) {
            return transformToLongList((List<?>) timeRequestedObject);
        }
        return null;
    }

    /**
     * Extract selected items from a map of parameters
     *
     * @param parameters
     *            Map of parameters
     * @return List of selected items or null if no selected items in the map
     */
    public static @Nullable List<Long> extractSelectedItems(Map<String, Object> parameters) {
        Object selectedItemObject = parameters.get(SELECTED_ITEMS_KEY);
        if (selectedItemObject instanceof List<?>) {
            return transformToLongList((List<?>) selectedItemObject);
        }
        return null;
    }

    /**
     * Extract isFiltered from a map of parameters (used in
     * {@link FilterTimeQueryFilter})
     *
     * @param parameters
     *            Map of parameters
     * @return True or false if it should be filtered or null if no isFiltered
     *         parameter
     */
    public static @Nullable Boolean extractIsFiltered(Map<String, Object> parameters) {
        Object isFilteredObject = parameters.get(FILTERED_PARAMETER_KEY);
        if (isFilteredObject instanceof Boolean) {
            return (Boolean) isFilteredObject;
        }
        return null;
    }

    /**
     * Transform a List<?> to a List<Long>, where ? is Integer or Long.
     *
     * @param listToTransform
     *            List to transform
     * @return List<Long> or null if the list can not be transformed
     */
    @SuppressWarnings("unchecked")
    public static @Nullable List<Long> transformToLongList(List<?> listToTransform) {
        if (!listToTransform.isEmpty()) {
            if (listToTransform.stream().allMatch(e -> e instanceof Long)) {
                return (List<Long>) listToTransform;
            } else if (listToTransform.stream().allMatch(e -> e instanceof Integer)) {
                List<Long> list = new ArrayList<>();
                for (Integer element : (List<Integer>) listToTransform) {
                    list.add(element.longValue());
                }
                return list;
            } else {
                List<Long> list = new ArrayList<>();
                for (Object element : listToTransform) {
                    if (!(element instanceof Number)) {
                        return null;
                    }
                    list.add(((Number) element).longValue());
                }
            }
        }
        return Collections.emptyList();
    }
}
