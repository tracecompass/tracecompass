/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.model;

import java.util.Arrays;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.IYModel;

import com.google.common.collect.ImmutableMap;
import com.google.gson.annotations.SerializedName;

/**
 * This is a basic implementation of {@link ITmfCommonXAxisModel}
 *
 * @author Yonni Chen
 * @since 3.0
 */
public class TmfCommonXAxisModel implements ITmfCommonXAxisModel {

    @SerializedName("title")
    private final String fTitle;

    @SerializedName("xValues")
    private final long[] fXValues;

    @SerializedName("series")
    private final Map<String, IYModel> fYSeries;

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
    }

    @Override
    public long[] getXAxis() {
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
}
