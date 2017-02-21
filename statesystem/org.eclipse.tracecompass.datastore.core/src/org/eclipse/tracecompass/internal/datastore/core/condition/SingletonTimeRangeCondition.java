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
 * A time range condition for a singleton time.
 *
 * @author Geneviève Bastien
 */
public class SingletonTimeRangeCondition implements TimeRangeCondition {

    private final long fValue;

    /**
     * Constructor
     *
     * @param ts
     *            The timestamp for this condition
     */
    public SingletonTimeRangeCondition(long ts) {
        fValue = ts;
    }

    @Override
    public long min() {
        return fValue;
    }

    @Override
    public long max() {
        return fValue;
    }

    @Override
    public boolean test(long element) {
        return element == fValue;
    }

    @Override
    public boolean intersects(long low, long high) {
        return low <= fValue && high >= fValue;
    }

    @Override
    public @Nullable TimeRangeCondition subCondition(long from, long to) {
        if (intersects(from, to)) {
            return this;
        }
        return null;
    }

}
