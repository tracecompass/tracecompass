/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.table;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Model for table filter
 *
 * @author Simon Delisle
 * @since 4.0
 */
public interface ITmfFilterModel {

    /**
     * Get the table filters
     *
     * @return Map of table filter (column ID, regex)
     */
    public @Nullable Map<Long, String> getTableFilter();

    /**
     * For a given filter, get the name to display
     *
     * @param filterId
     *            The filter ID, in this case the column ID
     * @return The filter name
     */
    public String getTableFilterName(Long filterId);

    /**
     * Get the preset filters
     *
     * @return List of preset filters ID
     */
    public @Nullable List<String> getPresetFilter();

    /**
     * For a given preset filter, get the name to display
     *
     * @param presetFilterId
     *            The preset filter ID
     * @return The preset filter name
     */
    public String getPresetFilterName(String presetFilterId);

    /**
     * If a collapse filter should be applied
     *
     * @return True if a collapse should be applied
     */
    public boolean isCollapseFilter();

}