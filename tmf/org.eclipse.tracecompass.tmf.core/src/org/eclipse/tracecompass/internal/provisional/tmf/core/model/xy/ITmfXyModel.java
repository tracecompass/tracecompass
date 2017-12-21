/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy;

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
 */
public interface ITmfXyModel {

    /**
     * Get chart title
     *
     * @return The title
     */
    @Nullable String getTitle();

    /**
     * Get the collection of {@link ISeriesModel}
     *
     * @return the collection of series data.
     */
    Map<String, ISeriesModel> getData();

}
