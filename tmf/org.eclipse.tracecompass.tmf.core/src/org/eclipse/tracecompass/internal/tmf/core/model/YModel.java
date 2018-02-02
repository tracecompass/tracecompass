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
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.IYModel;

import com.google.gson.annotations.SerializedName;

/**
 * This is a basic implementation of {@link IYModel}
 *
 * @author Yonni Chen
 * @since 3.0
 */
public class YModel implements IYModel {

    @SerializedName("label")
    private final String fName;

    @SerializedName("yValues")
    private final double[] fYValues;

    /**
     * Constructor
     *
     * @param name
     *            The name of the series
     * @param yValues
     *            The y series values
     */
    public YModel(String name, double[] yValues) {
        fName = name;
        fYValues = yValues;
    }

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public double[] getData() {
        return fYValues;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fName, Arrays.hashCode(fYValues));
    }

    @Override
    public boolean equals(@Nullable Object arg0) {
        if (arg0 == this) {
            return true;
        }
        if (arg0 == null || !(arg0 instanceof IYModel)) {
            return false;
        }
        IYModel otherSeries = (IYModel) arg0;
        return fName.equals(otherSeries.getName()) &&
                Arrays.equals(fYValues, otherSeries.getData());
    }

    @Override
    public String toString() {
        return fName + ':' + Arrays.toString(fYValues);
    }
}
