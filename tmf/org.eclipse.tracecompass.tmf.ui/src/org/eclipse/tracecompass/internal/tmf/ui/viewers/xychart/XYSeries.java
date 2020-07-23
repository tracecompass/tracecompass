
/*******************************************************************************
 * Copyright (c) 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.viewers.xychart;

import java.util.stream.StreamSupport;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swtchart.IBarSeries;
import org.eclipse.swtchart.ILineSeries;
import org.eclipse.swtchart.ISeries;
import org.eclipse.swtchart.model.CartesianSeriesModel;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.IXYSeries;

/**
 * Series wrapper
 *
 * @author Matthew Khouzam
 */
public class XYSeries implements IXYSeries {

    private final ISeries<Integer> fSeries;

    /**
     * Builder
     *
     * @param series
     *            the series
     * @return an XYSeries
     */
    public static @Nullable XYSeries create(Object series) {
        if (series instanceof ISeries) {
            return new XYSeries((ISeries<Integer>) series);
        }
        return null;
    }

    /**
     * Constructor
     *
     * @param series
     *            the series to wrap
     */
    private XYSeries(ISeries<Integer> series) {
        fSeries = series;
    }

    @Override
    public String getId() {
        return fSeries.getId();
    }

    @Override
    public boolean isVisible() {
        return fSeries.isVisible();
    }

    @Override
    public double[] getXSeries() {
        CartesianSeriesModel<Integer> dataModel = fSeries.getDataModel();
        if (dataModel == null) {
            return new double[0];
        }
        return StreamSupport.stream(dataModel.spliterator(), false).filter(t -> dataModel.getX(t) != null).mapToDouble(value -> dataModel.getX(value).doubleValue()).toArray();
    }

    @Override
    public double[] getYSeries() {
        CartesianSeriesModel<Integer> dataModel = fSeries.getDataModel();
        if (dataModel == null) {
            return new double[0];
        }
        return StreamSupport.stream(dataModel.spliterator(), false).filter(t -> dataModel.getY(t) != null).mapToDouble(value -> dataModel.getY(value).doubleValue()).toArray();
    }

    @Override
    public @Nullable Color getColor() {
        if (fSeries instanceof IBarSeries) {
            return ((IBarSeries<?>) fSeries).getBarColor();
        } else if (fSeries instanceof ILineSeries) {
            return ((ILineSeries<?>) fSeries).getLineColor();
        }
        return null;
    }
}
