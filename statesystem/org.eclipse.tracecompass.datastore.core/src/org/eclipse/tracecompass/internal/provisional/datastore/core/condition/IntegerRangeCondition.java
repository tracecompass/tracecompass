/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.datastore.core.condition;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.datastore.core.condition.ArrayIntegerRangeCondition;

/**
 * A range condition specific for integer ranges. It allows to work with int
 * primitive types, which provides much better performances
 *
 * @author Loic Prieur-Drevon
 */
public interface IntegerRangeCondition {

    /**
     * Get the lower bound of this range
     *
     * @return the lowest acceptable value for this condition.
     */
    int min();

    /**
     * Get the upper bound of this range
     *
     * @return the highest acceptable value for this condition.
     */
    int max();

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
    boolean test(int element);

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
    boolean intersects(int low, int high);

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
    @Nullable IntegerRangeCondition subCondition(int from, int to);

    /**
     * Get a range condition representing a discrete quark range.
     *
     * @param values
     *            Collection of distinct integers, needs to be distinct but not
     *            sorted.
     * @return The corresponding range condition
     */
    static IntegerRangeCondition forDiscreteRange(Collection<@NonNull Integer> values) {
        return new ArrayIntegerRangeCondition(values);
    }

}
