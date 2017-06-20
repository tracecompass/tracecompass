/*******************************************************************************
 * Copyright (c) 2017 Ericsson and others.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.viewmodel;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * View Model of Common X Line Chart interface
 *
 * @author Matthew Khouzam
 * @since 3.1
 */
public interface ICommonXAxisModel {

    /**
     * Get the X values
     *
     * @return the common X values
     */
    double[] getXAxis();

    /**
     * Get chart title
     *
     * @return the title
     */
    @Nullable String getTitle();

    /**
     * Get the collection of Y values, each one is a line
     *
     * @return the collection of Y values.
     */
    Map<String, IYSeries> getSeries();

    /**
     * Looks up a series by name
     *
     * @param seriesName
     *            the name to lookup
     * @return the series or null if not found
     */
    default @Nullable IYSeries findSeries(String seriesName) {
        return getSeries().get(seriesName);
    }
}