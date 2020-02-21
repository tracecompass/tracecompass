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

import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.core.model.xy.Messages;
import org.eclipse.tracecompass.tmf.core.model.timegraph.IFilterProperty;

/**
 * This represents a model for a series in a XY chart. I should be used to
 * describe a series of points. There should be the same number of values in the
 * X and Y axes and their orders should match, for instance x value at position
 * 1 goes with the y value at position 1
 *
 * @author Geneviève Bastien
 * @since 4.0
 */
public interface ISeriesModel {

    /**
     * Series data type
     *
     * @author Simon Delisle
     * @since 5.0
     */
    public enum DisplayType {
        /**
         * Line
         */
        LINE,
        /**
         * Scatter
         */
        SCATTER,
        /**
         * Area
         */
        AREA
    }

    /**
     * Get the unique ID for the entry associated to this series.
     *
     * @return the unique ID.
     */
    long getId();

    /**
     * Get the name of the series, AKA, the name of that series to display
     *
     * @return The name
     */
    String getName();

    /**
     * Get the X axis description
     *
     * @return X Axis description
     * @since 5.0
     */
    default TmfXYAxisDescription getXAxisDescription() {
        return new TmfXYAxisDescription(NonNullUtils.nullToEmptyString(Messages.TmfCoreModelXy_xAxisLabel), ""); //$NON-NLS-1$
    }

    /**
     * Get the Y axis description
     *
     * @return Y Axis description
     * @since 5.0
     */
    default TmfXYAxisDescription getYAxisDescription() {
        return new TmfXYAxisDescription(NonNullUtils.nullToEmptyString(Messages.TmfCoreModelXy_yAxisLabel), ""); //$NON-NLS-1$
    }

    /**
     * Get the display type
     *
     * @return Type of display (eg. line, scatter, ...)
     * @since 5.0
     */
    default DisplayType getDisplayType() {
        return DisplayType.LINE;
    }

    /**
     * Get the X values
     *
     * @return The x values
     */
    long[] getXAxis();

    /**
     * Get the y values
     *
     * @return An array of y values
     */
    double[] getData();

    /**
     * Get the array of properties for each data point in the series. There
     * should be the same number of points in the properties as in the series.
     * See {@link IFilterProperty} for some values that the properties can take.
     *
     * @return The values of the properties for each data point
     * @since 4.2
     */
    default int[] getProperties() {
        return new int[getXAxis().length];
    }
}
