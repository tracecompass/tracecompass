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
 * Abstract class for describing numbers from a stream of objects it
 * understands.
 *
 * @param <T>
 *            The type of the input it understands
 * @param <R>
 *            The type of the output number it describes
 *
 * @see IDataChartDescriptor
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class DataChartNumericalDescriptor<T, R extends Number> implements IDataChartDescriptor<T, R> {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final String fName;
    private final INumericalResolver<T, R> fResolver;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param name
     *            The name of the descriptor
     * @param resolver
     *            The resolver used for mapping numbers
     */
    public DataChartNumericalDescriptor(String name, INumericalResolver<T, R> resolver) {
        fName = name;
        fResolver = resolver;
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public void accept(IDescriptorVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public INumericalResolver<T, R> getResolver() {
        return fResolver;
    }

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public @Nullable String getUnit() {
        return null;
    }

    @Override
    public String toString() {
        return "Numerical Descriptor: " + getName(); //$NON-NLS-1$
    }

}
