/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.model;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ISeriesModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfXyModel;

import com.google.common.collect.ImmutableMap;
import com.google.gson.annotations.SerializedName;

/**
 * This is a basic implementation of {@link ITmfXyModel}
 *
 * @author Geneviève Bastien
 */
public class TmfXyModel implements ITmfXyModel {

    @SerializedName("title")
    private final String fTitle;

    @SerializedName("series")
    private final Map<String, ISeriesModel> fSeries;

    /**
     * Constructor
     *
     * @param title
     *            Chart title
     * @param series
     *            A map of series
     */
    public TmfXyModel(String title, Map<String, ISeriesModel> series) {
        fTitle = title;
        fSeries = ImmutableMap.copyOf(series);
    }

    @Override
    public @Nullable String getTitle() {
        return fTitle;
    }

    @Override
    public Map<String, ISeriesModel> getData() {
        return fSeries;
    }

}
