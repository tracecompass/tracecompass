/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.datastore.core.condition;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.TimeRangeCondition;

/**
 * Time range condition that will verify if values are within a range limited by
 * a lower and upper bound.
 *
 * @author Geneviève Bastien
 */
public class ContinuousTimeRangeCondition implements TimeRangeCondition {

    private final long fLongMin;
    private final long fLongMax;

    /**
     * Constructor
     *
     * @param low
     *            Lower bound of the range
     * @param high
     *            Upper bound of the range
     */
    public ContinuousTimeRangeCondition(long low, long high) {
        if (high < low) {
            throw new IllegalArgumentException("Continuous time range condition: lower bound (" + low +") should be <= upper bound (" + high + ')');  //$NON-NLS-1$//$NON-NLS-2$
        }
        fLongMin = low;
        fLongMax = high;
    }

    @Override
    public long min() {
        return fLongMin;
    }

    @Override
    public long max() {
        return fLongMax;
    }

    @Override
    public boolean test(long element) {
        return (element >= fLongMin && element <= fLongMax);
    }

    @Override
    public boolean intersects(long low, long high) {
        return (fLongMin <= high && fLongMax >= low);
    }

    @Override
    public @Nullable TimeRangeCondition subCondition(long from, long to) {
        long low = Math.max(from, fLongMin);
        long high =  Math.min(fLongMax, to);
        if (high < low) {
            return null;
        }
        return new ContinuousTimeRangeCondition(low, high);
    }

    @Override
    public String toString() {
        return "ContinuousTimeRangeCondition: (" + min() + '\u2025' + max() + ')'; //$NON-NLS-1$
    }

}
