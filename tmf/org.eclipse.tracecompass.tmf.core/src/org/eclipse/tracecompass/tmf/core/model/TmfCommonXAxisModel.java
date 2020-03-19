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

package org.eclipse.tracecompass.tmf.core.model;

import java.util.Arrays;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.model.xy.ISeriesModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfCommonXAxisModel;
import org.eclipse.tracecompass.tmf.core.model.xy.IYModel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;

/**
 * This is a basic implementation of {@link ITmfCommonXAxisModel} when there are
 * common values for the X axis for all series
 *
 * @author Yonni Chen
 * @since 4.0
 */
public class TmfCommonXAxisModel implements ITmfCommonXAxisModel {

    @SerializedName("title")
    private final String fTitle;

    @SerializedName("xValues")
    private final long[] fXValues;

    @SerializedName("series")
    private final Map<String, IYModel> fYSeries;

    private final transient Map<String, ISeriesModel> fSeries;

    /**
     * Constructor
     *
     * @param title
     *            Chart title
     * @param xValues
     *            x values
     * @param ySeries
     *            A Map of YSeries
     */
    public TmfCommonXAxisModel(String title, long[] xValues, Map<String, IYModel> ySeries) {
        fTitle = title;
        fXValues = Arrays.copyOf(xValues, xValues.length);
        fYSeries = ImmutableMap.copyOf(ySeries);
        fSeries = Maps.transformValues(fYSeries, model -> new SeriesModel(model.getId(), model.getName(), fXValues, model.getData()));
    }

    @Override
    public long[] getXValues() {
        return fXValues;
    }

    @Override
    public @Nullable String getTitle() {
        return fTitle;
    }

    @Override
    public @NonNull Map<String, IYModel> getYData() {
        return fYSeries;
    }

    @Override
    public boolean hasCommonXAxis() {
        return true;
    }

    @Override
    public Map<String, ISeriesModel> getData() {
        return fSeries;
    }
}
