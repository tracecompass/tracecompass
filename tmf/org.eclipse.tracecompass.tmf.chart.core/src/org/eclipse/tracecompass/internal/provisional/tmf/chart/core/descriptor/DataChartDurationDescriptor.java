/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.INumericalResolver;

/**
 * Generic descriptor that describes a time range duration. Its default units
 * are nanoseconds.
 *
 * @param <T>
 *            The type of the input it understands
 * @param <R>
 *            The type of the output duration it describes
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class DataChartDurationDescriptor<T, R extends Number> extends DataChartNumericalDescriptor<T, R> {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String UNIT = "ns"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param name
     *            The name of the descriptor
     * @param resolver
     *            The resolver used for mapping durations
     */
    public DataChartDurationDescriptor(String name, INumericalResolver<T, R> resolver) {
        super(name, resolver, UNIT);
    }

    /**
     * Constructor.
     *
     * @param name
     *            The name of the descriptor
     * @param resolver
     *            The resolver used for mapping numbers
     * @param unit
     *            The unit of this descriptor, eg. s, ms, ns
     */
    public DataChartDurationDescriptor(String name, INumericalResolver<T, R> resolver, @Nullable String unit) {
        super(name, resolver, unit);
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public void accept(IDescriptorVisitor visitor) {
        visitor.visit(this);
    }

}
