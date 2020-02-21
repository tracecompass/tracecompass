/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart;

import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;

/**
 * This class represents a plottable series. It contains a X data descriptor and
 * a Y data descriptor.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ChartSeries {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final IDataChartDescriptor<?, ?> fXDescriptor;
    private final IDataChartDescriptor<?, ?> fYDescriptor;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param descriptorX
     *            The X data descriptor
     * @param descriptorY
     *            The Y data descriptor
     */
    public ChartSeries(IDataChartDescriptor<?, ?> descriptorX, IDataChartDescriptor<?, ?> descriptorY) {
        fXDescriptor = descriptorX;
        fYDescriptor = descriptorY;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns the X data descriptor.
     *
     * @return The X data descriptor
     */
    public IDataChartDescriptor<?, ?> getX() {
        return fXDescriptor;
    }

    /**
     * Accessor that returns the Y data descriptor.
     *
     * @return The Y data descriptor
     */
    public IDataChartDescriptor<?, ?> getY() {
        return fYDescriptor;
    }

}
