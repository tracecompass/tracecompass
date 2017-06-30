/*******************************************************************************
 * Copyright (c) 2017 Ericsson and others.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts;

import java.util.Arrays;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.viewmodel.ICommonXAxisModel;
import org.eclipse.tracecompass.tmf.core.viewmodel.IYSeries;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

/**
 * View Model of Common X Line Chart
 *
 * @author Matthew Khouzam
 */
class CommonXAxisSeriesModel implements ICommonXAxisModel {

    /**
     * FIXME: make a centralized JSON factory
     */
    private static Gson fGson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Deserialize with json string
     *
     * @param json
     *            the string
     * @return the returned model
     */
    public static @Nullable ICommonXAxisModel create(String json) {
        return fGson.fromJson(json, CommonXAxisSeriesModel.class);
    }

    @SerializedName("title")
    private final String fTitle;
    @SerializedName("xValues")
    private final double @NonNull [] fXValues;
    @SerializedName("series")
    private final @NonNull Map<String, IYSeries> fSeries;

    /**
     * Constructor with a title
     *
     * @param chartTitle
     *            the title
     * @param xValues
     *            x axis values
     * @param ySeries
     *            y series
     */
    public CommonXAxisSeriesModel(String chartTitle, double @NonNull [] xValues, @NonNull Map<@NonNull String, @NonNull IYSeries> ySeries) {
        fTitle = chartTitle;
        fXValues = Arrays.copyOf(xValues, xValues.length);
        fSeries = ImmutableMap.copyOf(ySeries);
    }

    @Override
    public double[] getXAxis() {
        return fXValues;
    }

    @Override
    public @Nullable String getTitle() {
        return fTitle;
    }

    @Override
    public Map<String, IYSeries> getSeries() {
        return fSeries;
    }

    /**
     * {@inheritDoc}
     *
     * Outputs a JSON formatted string
     */
    @Override
    public String toString() {
        return fGson.toJson(this);
    }
}
