/*******************************************************************************
 * Copyright (c) 2019 Matthew Khouzam
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect;

import java.util.Comparator;
import java.util.function.Function;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableEntry;

/**
 * Common location for simple comparators
 *
 * @author Matthew Khouzam
 */
final class LamiComparators {

    private LamiComparators() {
        // do nothing
    }

    /**
     * Compare two LAMI aspects numerically
     *
     * @param resolveNumber
     *            the function to resolve a number
     * @return the comparator
     */
    public static Comparator<LamiTableEntry> getLongComparator(Function<LamiTableEntry, @Nullable Number> resolveNumber) {
        return (o1, o2) -> {
            Number leftNumber = resolveNumber.apply(o1);
            Number rightNumber = resolveNumber.apply(o2);

            if (leftNumber == null && rightNumber == null) {
                return 0;
            }
            if (leftNumber == null) {
                return 1;
            }

            if (rightNumber == null) {
                return -1;
            }

            return Long.compare(leftNumber.longValue(), rightNumber.longValue());
        };
    }


    /**
     * Compare two LAMI aspects numerically
     *
     * @param resolveNumber
     *            the function to resolve a number
     * @return the comparator
     */
    public static Comparator<LamiTableEntry> getDoubleComparator(Function<LamiTableEntry, @Nullable Number> resolveNumber) {
        return (o1, o2) -> {
            Number leftNumber = resolveNumber.apply(o1);
            Number rightNumber = resolveNumber.apply(o2);

            if (leftNumber == null && rightNumber == null) {
                return 0;
            }
            if (leftNumber == null) {
                return 1;
            }

            if (rightNumber == null) {
                return -1;
            }

            return Double.compare(leftNumber.doubleValue(), rightNumber.doubleValue());
        };
    }


    /**
     * Compare two LAMI aspects alphabetically
     *
     * @param resolveString
     *            the function to resolve a string
     * @return the comparator
     */
    public static Comparator<LamiTableEntry> getStringComparator(Function<LamiTableEntry, @Nullable String> resolveString) {
        return (o1, o2) -> {
            String leftString = resolveString.apply(o1);
            String rightString = resolveString.apply(o2);

            if (leftString == null && rightString == null) {
                return 0;
            }
            if (leftString == null) {
                return 1;
            }

            if (rightString == null) {
                return -1;
            }
            return leftString.compareTo(rightString);
        };
    }

}
