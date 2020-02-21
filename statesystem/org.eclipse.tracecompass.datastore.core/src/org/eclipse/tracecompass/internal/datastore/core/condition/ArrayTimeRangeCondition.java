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

package org.eclipse.tracecompass.internal.datastore.core.condition;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.TimeRangeCondition;

/**
 * Primitive array backed time range condition
 *
 * @author Loic Prieur-Drevon
 */
public class ArrayTimeRangeCondition implements TimeRangeCondition {

    private final long[] fTimeArray;

    /**
     * {@link ArrayTimeRangeCondition} from a collection
     *
     * @param times
     *            Collection of longs representing times.
     */
    public ArrayTimeRangeCondition(Collection<@NonNull Long> times) {
        if (times.isEmpty()) {
            throw new IllegalArgumentException("QuarkArrayRangeCondition requires a non empty collection"); //$NON-NLS-1$
        }
        fTimeArray = new long[times.size()];
        int i = 0;
        for (Long quark : times) {
            fTimeArray[i] = quark;
            i++;
        }
        Arrays.sort(fTimeArray);
    }

    /**
     * internal sub condition constructor
     *
     * @param timeArray
     *            a sorted array of times.
     */
    private ArrayTimeRangeCondition(long[] timeArray) {
        fTimeArray = timeArray;
    }

    @Override
    public long min() {
        return fTimeArray[0];
    }

    @Override
    public long max() {
        return fTimeArray[fTimeArray.length - 1];
    }

    @Override
    public boolean test(long element) {
        return Arrays.binarySearch(fTimeArray, element) >= 0;
    }

    @Override
    public boolean intersects(long low, long high) {
        int lowIndex = Arrays.binarySearch(fTimeArray, low);
        if (lowIndex >= 0) {
            // low is one of the times.
            return true;
        }
        if (lowIndex == -fTimeArray.length - 1) {
            // low is higher than the maximum times
            return false;
        }
        /**
         * -lowIndex - 1 is the insertion position low, which means that the current
         * value at -lowIndex - 1 is larger than low. we just need to check that it is
         * also smaller than or equal to high for the intersection
         */
        return fTimeArray[-lowIndex - 1] <= high;
    }

    @Override
    public @Nullable TimeRangeCondition subCondition(long from, long to) {
        if (from <= min() && max() <= to) {
            // all the elements are within the bounds
            return this;
        }
        int fromIndex = Arrays.binarySearch(fTimeArray, from);
        if (fromIndex == -fTimeArray.length - 1) {
            // from is larger than than the maximum quark
            return null;
        }
        fromIndex = (fromIndex >= 0) ? fromIndex : -fromIndex - 1;
        int toIndex = Arrays.binarySearch(fTimeArray, fromIndex, fTimeArray.length, to);
        if (toIndex == -1) {
            // to is smaller than the minimum quark
            return null;
        }
        toIndex = (toIndex >= 0) ? toIndex + 1 : -toIndex - 1;
        if (toIndex <= fromIndex) {
            return null;
        }
        return new ArrayTimeRangeCondition(Arrays.copyOfRange(fTimeArray, fromIndex, toIndex));
    }

    @Override
    public String toString() {
        return "ArrayTimeRangeCondition[" + fTimeArray.length + "](" + min() + '\u2025' + max() + ')'; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
