/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.model;

import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ISeriesModel;

import com.google.gson.annotations.SerializedName;

/**
 * This is a basic implementation of {@link ISeriesModel}
 *
 * @author Geneviève Bastien
 */
public class SeriesModel implements ISeriesModel {

    @SerializedName("label")
    private final String fName;

    @SerializedName("xValues")
    private final long[] fXValues;

    @SerializedName("yValues")
    private final double[] fYValues;

    /**
     * Constructor
     *
     * @param name
     *            The name of the series
     * @param xValues
     *            The x values of this series
     * @param data
     *            The y values of this series
     */
    public SeriesModel(String name, long[] xValues, double[] data) {
        fName = name;
        fXValues = xValues;
        fYValues = data;
    }

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public long[] getXAxis() {
        return fXValues;
    }

    @Override
    public double[] getData() {
        return fYValues;
    }

}
