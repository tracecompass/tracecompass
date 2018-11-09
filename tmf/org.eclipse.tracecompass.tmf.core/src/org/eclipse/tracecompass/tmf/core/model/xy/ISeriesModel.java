/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.model.xy;

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
     * @since 4.3
     */
    @SuppressWarnings("nls")
    default TmfXYAxis getXAxisDescription() {
        return new TmfXYAxis("X Axis", "");
    }

    /**
     * Get the Y axis description
     *
     * @return Y Axis description
     * @since 4.3
     */
    @SuppressWarnings("nls")
    default TmfXYAxis getYAxisDescription() {
        return new TmfXYAxis("Y Axis", "");
    }

    /**
     * Get the X values
     *
     * @return The x values
     * @since 4.3
     */
    default long[] getXValues() {
        return getXAxis();
    }

    /**
     * Get the y values
     *
     * @return An array of y values
     * @since 4.3
     */
    default double[] getYValues() {
        return getData();
    }

    /**
     * Get the X values
     *
     * @return The x values
     * @deprecated Use getXValues instead
     */
    @Deprecated
    long[] getXAxis();

    /**
     * Get the y values
     *
     * @return An array of y values
     * @deprecated Use getYValues instead
     */
    @Deprecated
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
