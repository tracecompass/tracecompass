/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.presentation;

import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;

/**
 * This is an interface responsible to retrieve presentation information for XY
 * charts. XY Viewers must use this provider, in order to apply style and color
 * to XY models computed by data providers.
 *
 * @author Yonni Chen
 * @since 4.0
 */
public interface IXYPresentationProvider {

    /**
     * Returns the {@link IYAppearance} to which the specified series name is
     * mapped. If no appearance is found, a new one will be created with given
     * parameters and added to this presentation provider
     *
     * @param seriesName
     *            The name of the series
     * @param type
     *            The series type
     * @param width
     *            The series width
     * @return The {@link IYAppearance} instance of the Y series.
     * @deprecated As of 6.0, use {@link #getSeriesStyle(Long, String, int)} instead
     */
    @Deprecated
    IYAppearance getAppearance(String seriesName, String type, int width);

    /**
     * Returns the {@link OutputElementStyle} to which the specified series ID is
     * mapped. If no appearance is found, a new one will be created with given
     * parameters and added to this presentation provider.
     *
     * @param seriesId
     *            The name of the series
     * @param type
     *            The series type
     * @param width
     *            The series width
     * @return The {@link IYAppearance} instance of the Y series.
     * @since 6.0
     */
    default OutputElementStyle getSeriesStyle(Long seriesId, String type, int width) {
        IYAppearance appearance = getAppearance(String.valueOf(seriesId), type, width);
        return appearance.toOutputElementStyle();
    }

    /**
     * Returns the {@link IYAppearance} to which the specified series ID is
     * mapped. If no appearance is found, a new one will be created with given
     * parameters and added to this presentation provider.
     *
     * TODO Support output element styles in XY too, so line types can be
     * defined by data provider
     *
     * @param seriesId
     *            The name of the series
     * @return The {@link IYAppearance} instance of the Y series.
     * @since 6.0
     */
    default OutputElementStyle getSeriesStyle(Long seriesId) {
        return getSeriesStyle(seriesId, StyleProperties.SeriesType.LINE, 1);
    }

    /**
     * Remove all {@link OutputElementStyle}
     */
    void clear();

}
