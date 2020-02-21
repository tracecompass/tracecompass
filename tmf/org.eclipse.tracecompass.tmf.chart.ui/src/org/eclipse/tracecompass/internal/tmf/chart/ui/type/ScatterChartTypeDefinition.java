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

package org.eclipse.tracecompass.internal.tmf.chart.ui.type;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartType;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;

/**
 * Scatter chart implementation of the a chart type.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ScatterChartTypeDefinition implements IChartTypeDefinition {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Icons used in the chart maker
     */
    private static final String SCATTER_CHART_ICON = "icons/scatterchart.png"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public ChartType getType() {
        return ChartType.SCATTER_CHART;
    }

    @Override
    public ImageData getImageData() {
        return new ImageData(getClass().getClassLoader().getResourceAsStream(SCATTER_CHART_ICON));
    }

    @Override
    public boolean checkIfXDescriptorValid(IDataChartDescriptor<?, ?> desc, @Nullable IDataChartDescriptor<?, ?> filter) {
        // Only descriptors with the same type are accepted on the X axis
        return IChartTypeDefinition.filterSameDescriptor(desc, filter);
    }

    @Override
    public boolean checkIfYDescriptorValid(IDataChartDescriptor<?, ?> desc, @Nullable IDataChartDescriptor<?, ?> filter) {
        // Only descriptors with the same type are accepted on the Y axis
        return IChartTypeDefinition.filterSameDescriptor(desc, filter);
    }

    @Override
    public boolean checkIfXLogscalePossible(@Nullable IDataChartDescriptor<?, ?> filter) {
        // Enable logarithmic scale for numerical descriptors, but not
        // timestamp, as log scale with timestamp would not make sense
        return checkNumericalNotTimestamp(filter);
    }

    @Override
    public boolean checkIfYLogscalePossible(@Nullable IDataChartDescriptor<?, ?> filter) {
        // Enable logarithmic scale for numerical descriptors, but not
        // timestamp, as log scale with timestamp would not make sense
        return checkNumericalNotTimestamp(filter);
    }

    private static boolean checkNumericalNotTimestamp(@Nullable IDataChartDescriptor<?, ?> filter) {
        if (filter == null) {
            return true;
        }

        return IChartTypeDefinition.checkIfNumerical(filter) && !IChartTypeDefinition.checkIfTimestamp(filter);
    }

}
