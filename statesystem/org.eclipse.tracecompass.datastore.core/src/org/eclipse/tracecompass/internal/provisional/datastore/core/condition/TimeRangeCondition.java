/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.datastore.core.condition;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.datastore.core.condition.ContinuousTimeRangeCondition;
import org.eclipse.tracecompass.internal.datastore.core.condition.SingletonTimeRangeCondition;

/**
 * A range condition specific for time ranges. It allows to work with long
 * primitive types, which provides much better performances
 *
 * @author Geneviève Bastien
 */
public interface TimeRangeCondition {

    /**
     * Get the lower bound of this range
     *
     * @return the lowest acceptable value for this condition.
     */
    long min();

    /**
     * Get the upper bound of this range
     *
     * @return the highest acceptable value for this condition.
     */
    long max();

    /**
     * Test whether a value is within this specific range boundaries. If the
     * range is continuous, it will return <code>true</code> if the value is
     * between the lower and upper bounds. If the range is discrete, it will
     * return <code>true</code> if the requested element is one of the elements
     * in the discrete range.
     *
     * @param element
     *            value that we want to test
     * @return true if element is contained in this condition's set or range
     */
    boolean test(long element);

    /**
     * Determine if the current range intersects a ranged bounded by the values
     * in parameter
     *
     * @param low
     *            interval's lower bound
     * @param high
     *            interval's upper bound
     * @return true if this element intersects the range's condition or any of
     *         the set's elements
     */
    boolean intersects(long low, long high);

    /**
     * Reduce the Condition to elements or the range within bounds from and to.
     * <code>null</code> is returned if the resulting condition is empty.
     *
     * @param from
     *            lower bound for the condition reduction.
     * @param to
     *            upper bound for the condition reduction.
     * @return the reduced condition or <code>null</code> if the reduced
     *         condition does not contain any element
     */
    @Nullable TimeRangeCondition subCondition(long from, long to);

    /**
     * Get a condition of a single element.
     *
     * @param elem The single element
     * @return The corresponding range condition
     */
    static TimeRangeCondition singleton(long elem) {
        return new SingletonTimeRangeCondition(elem);
    }

    /**
     * Get a range condition representing a continuous time range.
     *
     * @param bound1
     *            The first bound
     * @param bound2
     *            The second bound. It's fine for bound2 to be > or < than
     *            bound1.
     * @return The corresponding range condition
     */
    static TimeRangeCondition forContinuousRange(long bound1, long bound2) {
        if (bound2 < bound1) {
            throw new IllegalArgumentException("Continuous time range condition: lower bound (" + bound1 +") should be <= upper bound (" + bound2 + ')');  //$NON-NLS-1$//$NON-NLS-2$
        }
        return new ContinuousTimeRangeCondition(bound1, bound2);
    }

}
