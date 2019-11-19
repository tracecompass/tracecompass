/**********************************************************************
 * Copyright (c) 2019 Draeger, Auriga
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.tmf.ui.viewers.xycharts;

/**
 * Axis range with minimum and maximum values.
 *
 * @author Ivan Grinenko
 * @since 5.2
 *
 */
public class AxisRange {

    private final double fLower;
    private final double fUpper;

    /**
     * Constructor
     *
     * @param start
     *            starting value of the range
     * @param end
     *            ending value of the range
     */
    public AxisRange(double start, double end) {
        fLower = (end > start) ? start : end;
        fUpper = (end > start) ? end : start;
    }

    /**
     * Gets the lower value of the range.
     *
     * @return The lower value of the range.
     */
    public double getLower() {
        return fLower;
    }

    /**
     * Gets the upper value of the range.
     *
     * @return The upper value of the range.
     */
    public double getUpper() {
        return fUpper;
    }

    @Override
    public String toString() {
        return String.format("AxisRange[%f, %f]", getLower(), getUpper()); //$NON-NLS-1$
    }

}
