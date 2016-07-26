/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.ui.consumer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.IStringResolver;
import org.eclipse.tracecompass.internal.tmf.chart.core.consumer.IDataConsumer;

/**
 * This class processes string values in order to create valid data for a bar
 * chart. It takes a {@link IStringResolver} for mapping values.
 * <p>
 * Since a bar chart cannot have multiples Y values of the same series in the
 * same X value, we simply create a list of all the strings. Thus, the X values
 * might have duplicate names.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class BarStringConsumer implements IDataConsumer {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final IStringResolver<Object> fResolver;
    private final List<@Nullable String> fList = new ArrayList<>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param resolver
     *            The consumer that consumes values
     */
    public BarStringConsumer(IStringResolver<Object> resolver) {
        fResolver = resolver;
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public boolean test(Object obj) {
        return true;
    }

    @Override
    public void accept(Object obj) {
        String str = fResolver.getMapper().apply(obj);
        fList.add(str);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that return the list of string created by the consumer.
     *
     * @return The list of string
     */
    public Collection<@Nullable String> getList() {
        // Cannot use ImmutableList.copyOf since the elements can be null
        return new ArrayList<>(fList);
    }

}
