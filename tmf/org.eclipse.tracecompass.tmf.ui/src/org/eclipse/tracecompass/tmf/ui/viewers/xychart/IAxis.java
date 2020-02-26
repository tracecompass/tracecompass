/**********************************************************************
 * Copyright (c) 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.xychart;

/**
 * Minimal axis interface, inspired by org.eclipse.swtchart.IAxis
 *
 * @author Matthew Khouzam
 * @since 6.0
 */
public interface IAxis {
    /**
     * Gets the axis range.
     *
     * @return the axis range
     */
    AxisRange getRange();

    /**
     * Sets the axis range.
     *
     * @param range
     *            the axis range
     */
    void setRange(AxisRange range);

    /**
     * Gets the pixel coordinate corresponding to the given data coordinate.
     *
     * @param dataCoordinate
     *            the data coordinate
     * @return the pixel coordinate on plot area
     */
    int getPixelCoordinate(double dataCoordinate);

    /**
     * Gets the data coordinate corresponding to the given pixel coordinate on
     * plot area.
     *
     * @param pixelCoordinate
     *            the pixel coordinate on plot area
     * @return the data coordinate
     */
    double getDataCoordinate(int pixelCoordinate);

}
