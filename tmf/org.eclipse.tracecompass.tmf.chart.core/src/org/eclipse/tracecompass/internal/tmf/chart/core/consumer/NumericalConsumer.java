/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.core.consumer;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.INumericalResolver;

import com.google.common.collect.ImmutableList;

/**
 * This class processes numerical values in order to create valid data for a XY
 * chart. It takes a {@link INumericalResolver} for mapping values and a
 * {@link Predicate} for testing them.
 * <p>
 * It also computes the minimum and maximum values of all the numbers it has
 * consumed.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class NumericalConsumer implements IDataConsumer {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final INumericalResolver<Object, Number> fResolver;
    private final Predicate<@Nullable Number> fPredicate;
    private final List<Number> fData = new ArrayList<>();
    private Number fMin;
    private Number fMax;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor with a default non null predicate.
     *
     * @param resolver
     *            The resolver that maps values
     */
    public NumericalConsumer(INumericalResolver<Object, Number> resolver) {
        fResolver = resolver;
        fPredicate = Objects::nonNull;
        fMin = fResolver.getMaxValue();
        fMax = fResolver.getMinValue();
    }

    /**
     * Overloaded constructor with a predicate.
     *
     * @param resolver
     *            The resolver that maps values
     * @param predicate
     *            The predicate for testing values
     */
    public NumericalConsumer(INumericalResolver<Object, Number> resolver, Predicate<@Nullable Number> predicate) {
        fResolver = resolver;
        fPredicate = predicate;
        fMin = fResolver.getMaxValue();
        fMax = fResolver.getMinValue();
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public boolean test(Object obj) {
        Number number = fResolver.getMapper().apply(obj);
        return fPredicate.test(number);
    }

    @Override
    public void accept(Object obj) {
        Number number = checkNotNull(fResolver.getMapper().apply(obj));

        /* Update the minimum value */
        if (fResolver.getComparator().compare(number, fMin) < 0) {
            fMin = number;
        }

        /* Update the maximum value */
        if (fResolver.getComparator().compare(number, fMax) > 0) {
            fMax = number;
        }

        fData.add(number);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns the generated list of number.
     *
     * @return The list of number
     */
    public List<Number> getData() {
        return ImmutableList.copyOf(fData);
    }

    /**
     * Accessor that returns the minimum numerical value that has been consumed.
     *
     * @return The minimum value
     */
    public Number getMin() {
        return fMin;
    }

    /**
     * Accessor that returns the maximum numerical value that has been consumed.
     *
     * @return The maximum value
     */
    public Number getMax() {
        return fMax;
    }

}
