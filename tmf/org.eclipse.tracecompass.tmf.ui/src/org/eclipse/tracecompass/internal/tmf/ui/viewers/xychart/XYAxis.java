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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swtchart.Range;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.AxisRange;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.IAxis;

/**
 * API Wrapper for IAxis
 *
 * @author Matthew Khouzam
 *
 */
public class XYAxis implements IAxis {
    private final org.eclipse.swtchart.IAxis fAxis;

    /**
     * Builder
     *
     * @param series
     *            the series
     * @return an XYSeries
     */
    public static @Nullable XYAxis create(Object series) {
        if (series instanceof org.eclipse.swtchart.IAxis) {
            return new XYAxis((org.eclipse.swtchart.IAxis) series);
        }
        return null;
    }

    /**
     * Constructor
     *
     * @param axis
     *            the axis to wrap
     */
    private XYAxis(org.eclipse.swtchart.IAxis axis) {
        fAxis = axis;
    }

    @Override
    public AxisRange getRange() {
        Range range = fAxis.getRange();
        return new AxisRange(range.lower, range.upper);
    }

    @Override
    public void setRange(AxisRange range) {
        fAxis.setRange(new Range(range.getLower(), range.getUpper()));
    }

    @Override
    public int getPixelCoordinate(double position) {
        return fAxis.getPixelCoordinate(position);
    }

    @Override
    public double getDataCoordinate(int pixelCoordinate) {
        return fAxis.getDataCoordinate(pixelCoordinate);
    }
}
