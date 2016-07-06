/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor;

import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.IStringResolver;

/**
 * Generic descriptor that describes strings from an input object.
 *
 * @param <T>
 *            The type of the input it understands
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class DataChartStringDescriptor<T> implements IDataChartDescriptor<T, String> {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final String fName;
    private final IStringResolver<T> fResolver;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param name
     *            The name of the descriptor
     * @param resolver
     *            The resolver used for mapping strings
     */
    public DataChartStringDescriptor(String name, IStringResolver<T> resolver) {
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
    public IStringResolver<T> getResolver() {
        return fResolver;
    }

    @Override
    public String getName() {
        return fName;
    }

}
