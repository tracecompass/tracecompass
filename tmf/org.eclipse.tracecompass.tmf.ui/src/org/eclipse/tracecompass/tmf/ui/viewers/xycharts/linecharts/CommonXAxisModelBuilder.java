/*******************************************************************************
 * Copyright (c) 2017 Ericsson and others.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Builder for a model. Unsynchronized, developer needs to provide
 * synchronization methods if need be.
 *
 * @author Yonni Chen
 * @author Matthew Khouzam
 */
class CommonXAxisModelBuilder {

    private String fTitle;
    private double @NonNull [] fXValues = new double[0];
    private @NonNull Map<@NonNull String, @NonNull IYSeries> fYSeries = new LinkedHashMap<>();

    /**
     * Cache to save time rebuilding model if there are no changes.
     */
    private @Nullable ICommonXAxisModel fModel;

    /**
     * Set the title
     *
     * @param title
     *            the title to set
     * @return the builder
     */
    public CommonXAxisModelBuilder setTitle(String title) {
        fTitle = title;
        fModel = null;
        return this;
    }

    /**
     * Set the x axis values, should be called BEFORE the y Series, clears the Y
     * series
     *
     * @param xValues
     *            the xValues to set
     * @return The builder
     */
    public CommonXAxisModelBuilder setXValues(double @NonNull [] xValues) {
        fXValues = xValues;
        fYSeries.clear();
        fModel = null;
        return this;
    }

    /**
     * Add a Y Series
     *
     * @param ySeries
     *            the ySeries to add
     * @return The builder
     */
    public CommonXAxisModelBuilder addYSeries(IYSeries ySeries) {
        if (fXValues.length != ySeries.getDatapoints().length) {
            throw new IllegalStateException("All series in list must be of length : " + fXValues.length); //$NON-NLS-1$
        }
        deleteSeries(ySeries.getLabel());
        fYSeries.put(ySeries.getLabel(), ySeries);
        fModel = null;
        return this;
    }

    /**
     * Remove a series by name
     *
     * @param seriesName
     *            the series name
     * @return the builder
     */
    public CommonXAxisModelBuilder deleteSeries(String seriesName) {
        fYSeries.remove(seriesName);
        return this;
    }

    /**
     * Build the model
     *
     * @return model the model
     */
    public @NonNull ICommonXAxisModel build() {
        ICommonXAxisModel model = fModel;
        if (model == null) {
            model = new CommonXAxisSeriesModel(fTitle, fXValues, fYSeries);
            fModel = model;
        }
        return model;

    }
}
