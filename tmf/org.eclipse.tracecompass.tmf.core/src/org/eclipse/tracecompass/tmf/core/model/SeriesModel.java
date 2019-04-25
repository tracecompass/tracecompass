/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.model;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.model.timegraph.IFilterProperty;
import org.eclipse.tracecompass.tmf.core.model.xy.ISeriesModel;
import org.eclipse.tracecompass.tmf.core.model.xy.TmfXYAxis;

import com.google.gson.annotations.SerializedName;

/**
 * This is a basic implementation of {@link ISeriesModel}
 *
 * @author Geneviève Bastien
 * @since 4.0
 */
public class SeriesModel implements ISeriesModel {

    /**
     * Default name for X axis
     */
    private static final String DEFAULT_XAXIS_NAME = "X Axis"; //$NON-NLS-1$

    /**
     * Default unit type for X axis
     */
    private static final String DEFAULT_XAXIS_UNIT = "time"; //$NON-NLS-1$

    /**
     * Default name for Y axis
     */
    private static final String DEFAULT_YAXIS_NAME = "Y Axis"; //$NON-NLS-1$

    /**
     * Default unit type for y axis
     */
    private static final String DEFAULT_YAXIS_UNIT = "time"; //$NON-NLS-1$

    /**
     * transient to avoid serializing for tests, as IDs may not be the same from one
     * run to the other, due to how they are generated.
     */
    @SerializedName("id")
    private final transient long fId;

    @SerializedName("name")
    private final String fName;

    @SerializedName("xValues")
    private final long[] fXValues;

    @SerializedName("yValues")
    private final double[] fYValues;

    @SerializedName("xAxis")
    private final TmfXYAxis fXAxis;

    @SerializedName("yAxis")
    private final TmfXYAxis fYAxis;

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
     * @param yValues
     *            The y values of this series
     * @since 4.2
     */
    public SeriesModel(long id, String name, long[] xValues, double[] yValues) {
        this(id, name, xValues, yValues, new TmfXYAxis(DEFAULT_XAXIS_NAME, DEFAULT_XAXIS_UNIT), new TmfXYAxis(DEFAULT_YAXIS_NAME, DEFAULT_YAXIS_UNIT), new int[xValues.length]);
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
     * @param yValues
     *            The y values of this series
     * @param properties
     *            The properties values for this series. Some priority values
     *            are available in {@link IFilterProperty}
     * @since 4.2
     */
    public SeriesModel(long id, String name, long[] xValues, double[] yValues, int[] properties) {
        this(id, name, xValues, yValues, new TmfXYAxis(DEFAULT_XAXIS_NAME, DEFAULT_XAXIS_UNIT), new TmfXYAxis(DEFAULT_YAXIS_NAME, DEFAULT_YAXIS_UNIT), properties);
    }

    /**
     * Constructor with axis description
     *
     * @param id
     *            The unique ID of the associated entry
     * @param name
     *            The name of the series
     * @param xValues
     *            The x values of this series
     * @param yValues
     *            The y values of this series
     * @param xAxis
     *            X Axis description
     * @param yAxis
     *            Y Axis description
     * @param properties
     *            The properties values for this series. Some priority values
     *            are available in {@link IFilterProperty}
     * @since 5.0
     */
    public SeriesModel(long id, String name, long[] xValues, double[] yValues, TmfXYAxis xAxis, TmfXYAxis yAxis, int[] properties) {
        fId = id;
        fName = name;
        fXValues = xValues;
        fYValues = yValues;
        fXAxis = xAxis;
        fYAxis = yAxis;
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
    public long[] getXValues() {
        return fXValues;
    }

    @Override
    public double[] getYValues() {
        return fYValues;
    }

    @Override
    public TmfXYAxis getXAxisDescription() {
        return fXAxis;
    }

    @Override
    public TmfXYAxis getYAxisDescription() {
        return fYAxis;
    }

    @Override
    @Deprecated
    public long[] getXAxis() {
        return fXValues;
    }

    @Override
    @Deprecated
    public double[] getData() {
        return fYValues;
    }

    @Override
    public int[] getProperties() {
        return fProperties;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SeriesModel other = (SeriesModel) obj;
        return fName.equals(other.getName())
                && fId == other.getId()
                && Arrays.equals(fXValues, other.getXValues())
                && Arrays.equals(fYValues, other.getYValues())
                && fXAxis.equals(other.getXAxisDescription())
                && fYAxis.equals(other.getYAxisDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(fId, fName, fXValues, fYValues, fXAxis, fYAxis);
    }
}
