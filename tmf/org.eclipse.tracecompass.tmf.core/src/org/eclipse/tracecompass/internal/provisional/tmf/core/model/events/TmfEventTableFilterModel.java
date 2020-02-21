/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.events;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.ITmfFilterModel;

/**
 * Implementation of {@link ITmfFilterModel} for the event table.
 *
 * @author Simon Delisle
 * @since 4.0
 */
public class TmfEventTableFilterModel implements ITmfFilterModel {
    private final @Nullable Map<Long, String> fTableFilter;
    private final @Nullable Map<Long, String> fTableFilterName;
    private final @Nullable List<String> fPresetFilter;
    private final @Nullable Map<String, String> fPresetFilterName;
    private final boolean fCollapseFilterEnabled;

    /**
     * Constructor
     *
     * @param tableFilter
     *            Filter that comes from the table
     * @param tableFilterName
     *            Map column ID to the name of the filter applied to this column
     * @param isCollapseFilterEnabled
     *            If a collapse filter should be applied
     */
    public TmfEventTableFilterModel(@Nullable Map<Long, String> tableFilter, @Nullable Map<Long, String> tableFilterName, boolean isCollapseFilterEnabled) {
        this(tableFilter, tableFilterName, null, null, isCollapseFilterEnabled);
    }

    /**
     * Constructor
     *
     * @param presetFilter
     *            List of preset filter
     * @param presetFilterName
     *            Map preset filter ID to the name of the filter
     * @param isCollapseFilterEnabled
     *            If a collapse filter should be applied
     */
    public TmfEventTableFilterModel(@Nullable List<String> presetFilter, @Nullable Map<String, String> presetFilterName, boolean isCollapseFilterEnabled) {
        this(null, null, presetFilter, presetFilterName, isCollapseFilterEnabled);
    }

    /**
     * Constructor
     *
     * @param isCollapseFilterEnabled
     *            If a collapse filter should be applied
     */
    public TmfEventTableFilterModel(boolean isCollapseFilterEnabled) {
        this(null, null, null, null, isCollapseFilterEnabled);
    }

    /**
     * Constructor
     *
     * @param tableFilter
     *            Filter that comes from the table
     * @param tableFilterName
     *            Map column ID to the name of the filter applied to this column
     * @param presetFilter
     *            List of preset filter
     * @param presetFilterName
     *            Map preset filter ID to the name of the filter
     * @param isCollapseFilterEnabled
     *            If a collapse filter should be applied
     */
    public TmfEventTableFilterModel(@Nullable Map<Long, String> tableFilter, @Nullable Map<Long, String> tableFilterName, @Nullable List<String> presetFilter, @Nullable Map<String, String> presetFilterName, boolean isCollapseFilterEnabled) {
        fTableFilter = tableFilter == null || tableFilter.isEmpty() ? null : tableFilter;
        fTableFilterName = tableFilterName;
        fPresetFilter = presetFilter == null || presetFilter.isEmpty() ? null : presetFilter;
        fPresetFilterName = presetFilterName;
        fCollapseFilterEnabled = isCollapseFilterEnabled;
    }

    @Override
    public @Nullable Map<Long, String> getTableFilter() {
        return fTableFilter;
    }

    @Override
    public @NonNull String getTableFilterName(Long filterId) {
        String filterName = null;
        if (fTableFilterName != null) {
            filterName = fTableFilterName.get(filterId);
        }

        if (filterName == null && fTableFilter != null) {
            filterName = fTableFilter.get(filterId);
        }

        return filterName == null ? "" : filterName; //$NON-NLS-1$
    }

    @Override
    public @Nullable List<String> getPresetFilter() {
        return fPresetFilter;
    }

    @Override
    public @NonNull String getPresetFilterName(String presetFilterId) {
        String presetFilterName = presetFilterId;
        if (fPresetFilterName != null) {
            presetFilterName = fPresetFilterName.get(presetFilterId);
        }

        return presetFilterName == null ? "" : presetFilterName; //$NON-NLS-1$
    }

    @Override
    public boolean isCollapseFilter() {
        return fCollapseFilterEnabled;
    }
}
