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
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.IDataResolver;

/**
 * Interface for "describing" values inside a stream of objects it
 * "understands". It is pretty much the same as mapping values, except the
 * interface can do more than that. It uses {@link IDataResolver} for resolving
 * data it describes from an an object it understands. For example, it can be
 * seen as a column inside a table we want data from.
 *
 * @param <T>
 *            The type of the input it understands
 * @param <R>
 *            The type of the output it describes
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface IDataChartDescriptor<T, R> {

    /**
     * Method that accepts a visitor that wants to visit the current descriptor.
     *
     * @param visitor
     *            The visitor of the descriptor
     */
    void accept(IDescriptorVisitor visitor);

    /**
     * Get the resolver of this descriptor.
     *
     * @return The resolver of data
     */
    IDataResolver<T, R> getResolver();

    /**
     * Get the name of this descriptor.
     *
     * @return The name of the descriptor
     */
    String getName();

    /**
     * Get the unit of this descriptor.
     *
     * @return The unit of the descriptor
     */
    default @Nullable String getUnit() {
        return null;
    }

    /**
     * Get the label of this descriptor. The default label is composed of the
     * name followed by the unit in parentheses.
     *
     * @return The label of the descriptor
     */
    default String getLabel() {
        String name = getName();
        String units = getUnit();

        if (units == null) {
            return name;
        }

        return (name + " (" + units + ')'); //$NON-NLS-1$
    }

}
