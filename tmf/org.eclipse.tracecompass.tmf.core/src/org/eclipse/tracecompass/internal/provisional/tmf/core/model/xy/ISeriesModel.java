/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy;

/**
 * This represents a model for a series in a XY chart. I should be used to
 * describe a series of points. There should be the same number of values in the
 * X and Y axes and their orders should match, for instance x value at position
 * 1 goes with the y value at position 1
 *
 * @author Geneviève Bastien
 */
public interface ISeriesModel {
    /**
     * Get the name of the series, AKA, the name of that series to display
     *
     * @return The name
     */
    String getName();

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
}
