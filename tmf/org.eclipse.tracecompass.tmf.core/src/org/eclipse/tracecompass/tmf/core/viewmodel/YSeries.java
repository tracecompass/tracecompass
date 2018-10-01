/*******************************************************************************
 * Copyright (c) 2017 Ericsson and others.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.viewmodel;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Y series to show the height values of a chart.
 *
 * @author Matthew Khouzam
 * @since 3.1
 */
public class YSeries implements IYSeries {
    @SerializedName("label")
    private final String fLabel;
    @SerializedName("yValues")
    private final double[] fYValues;
    @SerializedName("type")
    private final String fType;
    @SerializedName("color")
    private final @Nullable String fColor;
    @SerializedName("width")
    private final int fWidth;
    @SerializedName("style")
    private final String fStyle;

    /**
     * Constructor
     *
     * @param type
     *            Type of the series (Line, Bar, Scatte, etc.)
     * @param color
     *            Color of the series
     * @param width
     *            Thickness of the series.
     * @param lineStyle
     *            Style of the series (Dash, Dot, etc.)
     * @param seriesName
     *            Name of the series
     * @param yData
     *            Data for Y axis
     */
    public YSeries(String type, @Nullable String color, int width, @Nullable String lineStyle, String seriesName, double[] yData) {
        fType = type;
        fColor = color;
        fWidth = width;
        fLabel = seriesName;
        fYValues = yData;
        fStyle = lineStyle == null ? SOLID : lineStyle;
    }

    @Override
    public String getLabel() {
        return fLabel;
    }

    @Override
    public double[] getDatapoints() {
        return fYValues;
    }

    @Override
    public String getSeriesType() {
        return fType;
    }

    @Override
    public @Nullable String getColor() {
        return fColor;
    }

    @Override
    public int getWidth() {
        return fWidth;
    }

    @Override
    public @NonNull String getSeriesStyle() {
        return fStyle;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fLabel, fType, fColor, fWidth, fYValues, fStyle);
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
        YSeries other = (YSeries) obj;

        return Objects.equals(fLabel, other.fLabel) &&
                Arrays.equals(fYValues, other.fYValues) &&
                Objects.equals(fColor, other.fColor) &&
                Objects.equals(fType, other.fType) &&
                Objects.equals(fStyle, other.fStyle) &&
                fWidth == other.fWidth;
    }
}