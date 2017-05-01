/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
            // low is one of the quarks.
            return true;
        }
        if (lowIndex == -fTimeArray.length - 1) {
            // low is higher than the maximum quark
            return false;
        }
        int highIndex = Arrays.binarySearch(fTimeArray, high);
        if (highIndex >= 0) {
            // high is one of the quarks
            return true;
        }
        if (highIndex == -1) {
            // high is smaller than the minimum quark
            return false;
        }
        // there is a quark between low and high
        return highIndex < lowIndex;
    }

    @Override
    public @Nullable TimeRangeCondition subCondition(long from, long to) {
        int fromIndex = Arrays.binarySearch(fTimeArray, from);
        if (fromIndex == -fTimeArray.length - 1) {
            // from is larger than than the maximum quark
            return null;
        }
        int toIndex = Arrays.binarySearch(fTimeArray, to);
        if (toIndex == -1) {
            // to is smaller than the minimum quark
            return null;
        }
        fromIndex = (fromIndex >= 0) ? fromIndex : -fromIndex - 1;
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
