/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.eclipse.tracecompass.internal.datastore.core.condition;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.RangeCondition;

/**
 * Generic Range Condition. This condition will verify that a value are within a
 * range limited by a lower and upper bound.
 *
 * @author Loïc Prieur-Drevon
 * @param <E>
 *            Comparable type, for instance Long for timestamps
 */
public class ContinuousRangeCondition<E extends Comparable<E>> implements RangeCondition<E> {

    private final E fMin;
    private final E fMax;

    /**
     * Build a continuous range condition from two bounds.
     *
     * @param low
     *            The range's lower bound
     * @param high
     *            The range's higher bound
     */
    public ContinuousRangeCondition(@NonNull E low, @NonNull E high) {
        if (low.compareTo(high) > 0) {
            throw new IllegalArgumentException(low.toString() + " is greater than " + high.toString()); //$NON-NLS-1$
        }
        fMin = low;
        fMax = high;
    }

    @Override
    public E min() {
        return fMin;
    }

    @Override
    public E max() {
        return fMax;
    }

    @Override
    public boolean contains(E element) {
        return fMin.compareTo(element) <= 0 && element.compareTo(fMax) <= 0;
    }

    @Override
    public boolean intersects(E low, E high) {
        return fMin.compareTo(high) <= 0 && fMax.compareTo(low) >= 0;
    }

    private static <E extends Comparable<E>> E min(E n1, E n2) {
        if (n1.compareTo(n2) <= 0) {
            return n1;
        }
        return n2;
    }

    private static <E extends Comparable<E>> E max(E n1, E n2) {
        if (n1.compareTo(n2) >= 0) {
            return n1;
        }
        return n2;
    }

    @Override
    public @NonNull RangeCondition<E> subCondition(E from, E to) {
        return new ContinuousRangeCondition<>(max(from, fMin), min(fMax, to));
    }

    @Override
    public String toString() {
        return "Continuous condition: " + fMin.toString() + '-' + fMax.toString(); //$NON-NLS-1$
    }
}