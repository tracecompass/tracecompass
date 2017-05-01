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
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.IntegerRangeCondition;

/**
 * Primitive array backed integer range condition
 *
 * @author Loic Prieur-Drevon
 */
public class ArrayIntegerRangeCondition implements IntegerRangeCondition {

    private final int[] fQuarkArray;

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
    }

    /**
     * internal sub condition constructor
     *
     * @param intArray
     *            a sorted array of integers.
     */
    private ArrayIntegerRangeCondition(int[] intArray) {
        fQuarkArray = intArray;
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
        return Arrays.binarySearch(fQuarkArray, element) >= 0;
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
        int highIndex = Arrays.binarySearch(fQuarkArray, high);
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
    public @Nullable IntegerRangeCondition subCondition(int from, int to) {
        int fromIndex = Arrays.binarySearch(fQuarkArray, from);
        if (fromIndex == -fQuarkArray.length - 1) {
            // from is larger than than the maximum quark
            return null;
        }
        int toIndex = Arrays.binarySearch(fQuarkArray, to);
        if (toIndex == -1) {
            // to is smaller than the minimum quark
            return null;
        }
        fromIndex = (fromIndex >= 0) ? fromIndex : -fromIndex - 1;
        toIndex = (toIndex >= 0) ? toIndex + 1 : -toIndex - 1;
        if (toIndex <= fromIndex) {
            return null;
        }
        return new ArrayIntegerRangeCondition(Arrays.copyOfRange(fQuarkArray, fromIndex, toIndex));
    }

    @Override
    public String toString() {
        return "ArrayIntegerRangeCondition[" + fQuarkArray.length + "](" + min() + '\u2025' + max() + ')'; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
