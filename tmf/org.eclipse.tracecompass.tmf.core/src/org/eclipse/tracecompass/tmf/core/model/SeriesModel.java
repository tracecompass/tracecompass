/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.model;

import org.eclipse.tracecompass.tmf.core.model.timegraph.IFilterProperty;
import org.eclipse.tracecompass.tmf.core.model.xy.ISeriesModel;

import com.google.gson.annotations.SerializedName;

/**
 * This is a basic implementation of {@link ISeriesModel}
 *
 * @author Geneviève Bastien
 * @since 4.0
 */
public class SeriesModel implements ISeriesModel {

    /**
     * transient to avoid serializing for tests, as IDs may not be the same from one
     * run to the other, due to how they are generated.
     */
    @SerializedName("id")
    private final transient long fId;

    @SerializedName("label")
    private final String fName;

    @SerializedName("xValues")
    private final long[] fXValues;

    @SerializedName("yValues")
    private final double[] fYValues;

    @SerializedName("properties")
    private final int[] fProperties;

    /**
     * Constructor
     *
     * @param id
     *            The unique ID of the associated entry
     * @param name
     *            The name of the series
     * @param xValues
     *            The x values of this series
     * @param data
     *            The y values of this series
     */
    public SeriesModel(long id, String name, long[] xValues, double[] data) {
        this(id, name, xValues, data, new int[xValues.length]);
    }

    /**
     * Constructor
     *
     * @param id
     *            The unique ID of the associated entry
     * @param name
     *            The name of the series
     * @param xValues
     *            The x values of this series
     * @param data
     *            The y values of this series
     * @param properties
     *            The properties values for this series. Some priority values
     *            are available in {@link IFilterProperty}
     * @since 4.2
     */
    public SeriesModel(long id, String name, long[] xValues, double[] data, int[] properties) {
        fId = id;
        fName = name;
        fXValues = xValues;
        fYValues = data;
        fProperties = properties;
    }

    @Override
    public long getId() {
        return fId;
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

    @Override
    public int[] getProperties() {
        return fProperties;
    }
}
