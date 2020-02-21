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
 * Interface for implementing chart type specific carasteristics.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface IChartTypeDefinition {

    /**
     * Accessor that returns the identifier of the chart.
     *
     * @return Identifier of the chart
     */
    ChartType getType();

    /**
     * Accessor that returns image data of the chart's icon.
     *
     * @return Image data of an icon
     */
    ImageData getImageData();

    /**
     * Method used for checking if a descriptor for the X axis is valid given
     * previously selected descriptors.
     *
     * For example, if for one chart type, only string descriptors should be
     * accepted, then this method will return true only for descriptors
     * resolving to String values, independent on the filter.
     *
     * Also, if a chart type does not mix string and numerical values for the X
     * axis, this method would always return <code>true</code> if the filter is
     * <code>null</code>, but would check the type of the value the descriptor
     * resolves to and return </code>true</code> only if the type matches.
     *
     * @param desc
     *            The descriptor to validate
     * @param filter
     *            The descriptor used for filtering
     * @return {@code true} if the descriptor passes the filter or {@code false}
     *         if the descriptor didn't pass the filter
     */
    boolean checkIfXDescriptorValid(IDataChartDescriptor<?, ?> desc, @Nullable IDataChartDescriptor<?, ?> filter);

    /**
     * Method used for checking if a descriptor for the Y axis is valid given
     * previously selected descriptors.
     *
     * For example, if for one chart type, only string descriptors should be
     * accepted, then this method will return true only for descriptors
     * resolving to String values, independent on the filter.
     *
     * Also, if a chart type does not mix string and numerical values for the Y
     * axis, this method would always return <code>true</code> if the filter is
     * <code>null</code>, but would check the type of the value the descriptor
     * resolves to and return </code>true</code> only if the type matches.
     *
     * @param desc
     *            The descriptor to check
     * @param filter
     *            The descriptor used for filtering
     * @return {@code true} if the descriptor passes the filter or {@code false}
     *         if the descriptor didn't pass the filter
     */
    boolean checkIfYDescriptorValid(IDataChartDescriptor<?, ?> desc, @Nullable IDataChartDescriptor<?, ?> filter);

    /**
     * Method that checks if the X axis logarithmic scale can be enabled.
     *
     * @param filter
     *            An descriptor that represent the X series
     * @return {@code true} if it can be logarithmic, {@code false} if can't be
     */
    boolean checkIfXLogscalePossible(@Nullable IDataChartDescriptor<?, ?> filter);

    /**
     * Method that checks if the Y axis logarithmic scale can be enabled.
     *
     * @param filter
     *            An descriptor that represent the Y series
     * @return {@code true} if it can be logarithmic, {@code false} if can't be
     */
    boolean checkIfYLogscalePossible(@Nullable IDataChartDescriptor<?, ?> filter);

    // ------------------------------------------------------------------------
    // Utility methods
    // ------------------------------------------------------------------------

    /**
     * Utility method to check if a descriptor has the same class as the filter.
     * It compares the class of the descriptor, not if it is an instanceof the
     * same class, so this will return <code>true</code> only if the class is
     * the same, not a child of.
     *
     * @param desc
     *            The descriptor to check
     * @param filter
     *            The descriptor used for filtering
     * @return {@code true} if both descriptors share the same class or the
     *         filter is {@code null}, or {@code false} if they are different
     */
    static boolean filterSameDescriptor(IDataChartDescriptor<?, ?> desc, @Nullable IDataChartDescriptor<?, ?> filter) {
        if (filter != null) {
            if (desc.getClass() != filter.getClass()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Utility method to check if a descriptor is numerical. Durations and
     * timestamps are considered numerical.
     *
     * @param desc
     *            The descriptor to check
     * @return {@code true} if the descriptor is numerical, {@code false} if the
     *         descriptor is something else
     */
    static boolean checkIfNumerical(IDataChartDescriptor<?, ?> desc) {
        DescriptorTypeVisitor visitor = new DescriptorTypeVisitor();
        desc.accept(visitor);

        return visitor.isIndividualType(DescriptorType.NUMERICAL);
    }

    /**
     * Utility method to check if a descriptor is a timestamp
     *
     * @param desc
     *            The descriptor to check
     * @return {@code true} if the descriptor is a timestamp, {@code false} if
     *         the descriptor is something else
     */
    static boolean checkIfTimestamp(IDataChartDescriptor<?, ?> desc) {
        DescriptorTypeVisitor visitor = new DescriptorTypeVisitor();
        desc.accept(visitor);

        return visitor.isIndividualType(DescriptorType.TIMESTAMP);
    }

}