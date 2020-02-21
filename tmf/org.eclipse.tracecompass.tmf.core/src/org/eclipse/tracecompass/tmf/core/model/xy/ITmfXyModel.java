/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.model.xy;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * This is the XY model interface returned by data providers. This model is
 * immutable and is used by viewers. In this model, there is no information
 * about color, style or chart type (bar, scatter, line, etc.). It contains only
 * data. <br/>
 * <br/>
 *
 * This interface returns a collection of {@link ISeriesModel}, each series
 * being a collection of points that do not have to share the same X axis
 * values.
 *
 * @author Geneviève Bastien
 * @since 4.0
 */
public interface ITmfXyModel {

    /**
     * Get chart title
     *
     * @return The title
     */
    @Nullable String getTitle();

    /**
     * True if the x values of the series are common
     *
     * @return True if X values are common
     * @since 5.0
     */
    default boolean hasCommonXAxis() {
        return false;
    }

    /**
     * Get the collection of {@link ISeriesModel}
     *
     * @return Map of series data where the key is the unique id of the
     *         associated series
     */
    Map<String, ISeriesModel> getData();

}
