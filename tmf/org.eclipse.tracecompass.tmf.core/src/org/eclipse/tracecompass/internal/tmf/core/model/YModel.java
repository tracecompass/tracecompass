/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.model;

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
}
