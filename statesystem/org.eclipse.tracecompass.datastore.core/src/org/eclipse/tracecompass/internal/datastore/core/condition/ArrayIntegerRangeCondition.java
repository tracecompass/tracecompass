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
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.IntegerRangeCondition;

/**
 * Primitive array backed integer range condition
 *
 * @author Loic Prieur-Drevon
 */
public class ArrayIntegerRangeCondition implements IntegerRangeCondition {

    private static final int[] MASKS = { 0x1, 0x2, 0x4, 0x8, 0x10, 0x20, 0x40, 0x80 };

    /**
     * This array contains the actual integer values
     */
    private final int[] fQuarkArray;
    /**
     * This array is used for efficient lookup: we use bytes to reduce the memory
     * usage ( JVM would allocate a byte per boolean if we were using a boolean
     * array , thus increasing overhead). For lookup, the first bit in this array
     * corresponds to the minimum value, and then, there is a bit for every integer
     * to max.
     */
    private final byte[] fQuarkSet;

    /**
     * {@link ArrayIntegerRangeCondition} from a collection
     *
     * @param quarks
     *            Collection of integers.
     */
    public ArrayIntegerRangeCondition(Collection<@NonNull Integer> quarks) {
        if (quarks.isEmpty()) {
            throw new IllegalArgumentException("QuarkArrayRangeCondition requires a non empty collection"); //$NON-NLS-1$
        }
        fQuarkArray = new int[quarks.size()];
        int i = 0;
        for (Integer quark : quarks) {
            fQuarkArray[i] = quark;
            i++;
        }
        Arrays.sort(fQuarkArray);
        fQuarkSet = buildQuarkSet(fQuarkArray);
    }

    /**
     * internal sub condition constructor
     *
     * @param intArray
     *            a sorted array of integers.
     */
    private ArrayIntegerRangeCondition(int[] intArray) {
        fQuarkArray = intArray;
        fQuarkSet = buildQuarkSet(intArray);
    }

    private static byte[] buildQuarkSet(int[] sortedQuarkArray) {
        int min = sortedQuarkArray[0];
        int max = sortedQuarkArray[sortedQuarkArray.length - 1];
        byte[] quarkSet = new byte[(max - min) / 8 + 1];

        for (int quark : sortedQuarkArray) {
            int delta = quark - min;
            int index = delta / 8;
            int offset = delta & 0x7;
            quarkSet[index] |= MASKS[offset];
        }
        return quarkSet;
    }

    @Override
    public int min() {
        return fQuarkArray[0];
    }

    @Override
    public int max() {
        return fQuarkArray[fQuarkArray.length - 1];
    }

    @Override
    public boolean test(int element) {
        if (element < min() || max() < element) {
            return false;
        }
        int delta = element - min();
        int index = delta / 8;
        int offset = delta & 0x7;
        return (fQuarkSet[index] & MASKS[offset]) != 0;
    }

    @Override
    public boolean intersects(int low, int high) {
        int lowIndex = Arrays.binarySearch(fQuarkArray, low);
        if (lowIndex >= 0) {
            // low is one of the quarks.
            return true;
        }
        if (lowIndex == -fQuarkArray.length - 1) {
            // low is higher than the maximum quark
            return false;
        }
        /**
         * -lowIndex - 1 is the insertion position low, which means that the current
         * value at -lowIndex - 1 is larger than low. we just need to check that it is
         * also smaller than or equal to high for the intersection
         */
        return fQuarkArray[-lowIndex - 1] <= high;
    }

    @Override
    public @Nullable IntegerRangeCondition subCondition(int from, int to) {
        if (from <= min() && max() <= to) {
            // all the elements are within the bounds
            return this;
        }
        int fromIndex = Arrays.binarySearch(fQuarkArray, from);
        if (fromIndex == -fQuarkArray.length - 1) {
            // from is larger than than the maximum quark
            return null;
        }
        fromIndex = (fromIndex >= 0) ? fromIndex : -fromIndex - 1;
        int toIndex = Arrays.binarySearch(fQuarkArray, fromIndex, fQuarkArray.length, to);
        if (toIndex == -1) {
            // to is smaller than the minimum quark
            return null;
        }
        toIndex = (toIndex >= 0) ? toIndex + 1 : -toIndex - 1;
        if (toIndex <= fromIndex) {
            return null;
        }
        return new ArrayIntegerRangeCondition(Arrays.copyOfRange(fQuarkArray, fromIndex, toIndex));
    }

    @Override
    public String toString() {
        return "ArrayIntegerRangeCondition[" + fQuarkSet.length + "](" + min() + '\u2025' + max() + ')'; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
