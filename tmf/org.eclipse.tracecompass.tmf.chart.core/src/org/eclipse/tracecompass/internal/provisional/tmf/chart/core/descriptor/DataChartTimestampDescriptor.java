/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor;

import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.INumericalResolver;

/**
 * Abstract class for describing timestamps from a stream of values it
 * understands.
 *
 * @param <T>
 *            The type of the input it understands
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class DataChartTimestampDescriptor<T> extends DataChartNumericalDescriptor<T, Long> {

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param name
     *            The name of the descriptor
     * @param resolver
     *            The resolver used for mapping timestamps
     */
    public DataChartTimestampDescriptor(String name, INumericalResolver<T, Long> resolver) {
        super(name, resolver);
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public void accept(IDescriptorVisitor visitor) {
        visitor.visit(this);
    }

}
