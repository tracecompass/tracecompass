/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model.filters;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.Activator;

import com.google.common.collect.Ordering;
import com.google.common.primitives.Longs;

/**
 * This represents a time query filter used by data providers. It encapsulates
 * an array of times used for requesting data. It's the responsibility of
 * viewers using data provider to create a time query filter and pass it to data
 * providers if needed.
 *
 * Elements of the array should be ORDERED ascendingly.
 *
 * @author Yonni Chen
 * @since 4.0
 */
public class TimeQueryFilter {

    private final long[] fTimesRequested;

    /**
     * Constructor. Given a start value, end value and n entries, this
     * constructor will set its property to an array of n entries uniformly
     * distributed and ordered ascendingly.
     *
     * @param start
     *            The starting value
     * @param end
     *            The ending value
     * @param n
     *            The number of entries
     **/
    public TimeQueryFilter(long start, long end, int n) {
        fTimesRequested = splitRangeIntoEqualParts(start, end, n);
    }

    /**
     * Create a {@link TimeQueryFilter} from a sorted list of times.
     *
     * @param times
     *            sorted list of times to query.
     */
    public TimeQueryFilter(List<Long> times) {
        if (!Ordering.natural().isOrdered(times)) {
            throw new IllegalArgumentException("List of times is not sorted"); //$NON-NLS-1$
        }
        fTimesRequested = Longs.toArray(times);
    }

    /**
     * Gets the array of times requested
     *
     * @return The array of requested times
     */
    public long[] getTimesRequested() {
        return fTimesRequested;
    }

    /**
     * Gets the first time
     *
     * @return The first time
     */
    public long getStart() {
        return fTimesRequested[0];
    }

    /**
     * Gets the last time
     *
     * @return The last time
     */
    public long getEnd() {
        return fTimesRequested[Integer.max(0, fTimesRequested.length - 1)];
    }

    /**
     * Given a start and end value, this method will create an array of n
     * entries uniformly distributed. First entry of resulting array is start
     * and last entry is end. Example : start = 1, end = 15, n = 5, resulting
     * array will be : [1, 5, 8, 12, 15]. <br/>
     * If n is equal to 1, this method will return an array of size 1 ONLY if
     * start and end are equal. Otherwise, an IllegalArgumentException will be
     * thrown.
     *
     * @param start
     *            The starting value
     * @param end
     *            The ending value. Must be greater or equal to start value
     * @param n
     *            The number of entries. Must be greater than 0
     * @return An uniformly distributed array of n elements
     */
    private static final long[] splitRangeIntoEqualParts(long start, long end, int n) {
        if (n < 1) {
            throw new IllegalArgumentException("Number of entries must be greater than 0"); //$NON-NLS-1$
        }

        if (n == 1) {
            if (start == end) {
                long[] result = new long[1];
                result[0] = start;
                return result;
            }
            throw new IllegalArgumentException("Number of entries requested is 1, but start and end are different. Impossible to create array."); //$NON-NLS-1$
        }

        if (n > 65536) {
            Activator.logWarning(String.format("Number of entries is very large, it is likely a bug and will result in slower queries. start time = %d, end time = %d, number of elements = %d", start, end, n)); //$NON-NLS-1$
        }

        double stepSize = Math.abs(end - start) / ((double) n - 1);
        long[] result = new long[n];

        for (int i = 0; i < n; i++) {
            result[i] = Math.min(start, end) + Math.round(i * stepSize);
        }

        /* This is to make sure that last value will always be end */
        result[result.length - 1] = Math.max(start, end);
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        TimeQueryFilter other = (TimeQueryFilter) obj;
        return Arrays.equals(fTimesRequested, other.getTimesRequested());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(fTimesRequested);
    }
}
