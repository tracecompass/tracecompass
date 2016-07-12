/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor;

/**
 * Interface for visiting data chart descriptors.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface IDescriptorVisitor {

    /**
     * Method for visiting a {@link DataChartStringDescriptor}.
     *
     * @param desc
     *            A string descriptor
     */
    void visit(DataChartStringDescriptor<?> desc);

    /**
     * Method for visiting a {@link DataChartNumericalDescriptor}.
     *
     * @param desc
     *            A number descriptor
     */
    void visit(DataChartNumericalDescriptor<?, ? extends Number> desc);

    /**
     * Method for visiting a {@link DataChartDurationDescriptor}.
     *
     * @param desc
     *            A duration descriptor
     */
    default void visit(DataChartDurationDescriptor<?, ? extends Number> desc) {
        visit((DataChartNumericalDescriptor<?, ? extends Number>) desc);
    }

    /**
     * Method for visiting a {@link DataChartTimestampDescriptor}.
     *
     * @param desc
     *            A timestamp descriptor
     */
    default void visit(DataChartTimestampDescriptor<?> desc) {
        visit((DataChartNumericalDescriptor<?, Long>) desc);
    }

}
