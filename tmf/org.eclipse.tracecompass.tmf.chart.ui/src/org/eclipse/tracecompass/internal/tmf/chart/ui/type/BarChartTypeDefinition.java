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
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DescriptorTypeVisitor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DescriptorTypeVisitor.DescriptorType;

/**
 * Bar chart implementation of the a chart type.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class BarChartTypeDefinition implements IChartTypeDefinition {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Icons used in the chart maker
     */
    private static final String BAR_CHART_ICON = "icons/barchart.png"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public ChartType getType() {
        return ChartType.BAR_CHART;
    }

    @Override
    public ImageData getImageData() {
        return new ImageData(getClass().getClassLoader().getResourceAsStream(BAR_CHART_ICON));
    }

    @Override
    public boolean checkIfXDescriptorValid(IDataChartDescriptor<?, ?> desc, @Nullable IDataChartDescriptor<?, ?> filter) {
        DescriptorTypeVisitor visitor = new DescriptorTypeVisitor();
        desc.accept(visitor);

        // A bar chart only accepts string values for the X axis
        if (!visitor.isIndividualType(DescriptorType.STRING)) {
            return false;
        }

        // Only one descriptor is accepted for the X axis, so return true only
        // if the descriptor is the filter.
        if (filter != null) {
            return desc.getName().equals(filter.getName());
        }
        return true;
    }

    @Override
    public boolean checkIfYDescriptorValid(IDataChartDescriptor<?, ?> desc, @Nullable IDataChartDescriptor<?, ?> filter) {
        DescriptorTypeVisitor visitor = new DescriptorTypeVisitor();
        desc.accept(visitor);

        // Only numerical data are accepted for the bar chart: it weighs the
        // values and it is not possible to weigh Strings
        if (visitor.isIndividualType(DescriptorType.STRING)) {
            return false;
        }

        // Only allow descriptors of the same type, that will mean the same
        // thing. It is hard to compare durations to timestamps for instance
        return IChartTypeDefinition.filterSameDescriptor(desc, filter);
    }

    @Override
    public boolean checkIfXLogscalePossible(@Nullable IDataChartDescriptor<?, ?> filter) {
        // Only strings are allowed for X axis, so no log scale possible
        return false;
    }

    @Override
    public boolean checkIfYLogscalePossible(@Nullable IDataChartDescriptor<?, ?> filter) {
        if (filter == null) {
            return true;
        }

        // It would not make sense to allow log scales for timestamps, so it
        // only applies to other numerical values
        return IChartTypeDefinition.checkIfNumerical(filter) && !IChartTypeDefinition.checkIfTimestamp(filter);
    }

}
