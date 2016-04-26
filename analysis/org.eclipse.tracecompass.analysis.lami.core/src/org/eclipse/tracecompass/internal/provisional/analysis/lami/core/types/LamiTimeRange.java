/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Lami time range data type
 *
 * @author Alexandre Montplaisir
 */
public class LamiTimeRange extends LamiData {

    private final long fStart;
    private final long fEnd;
    private final long fDuration;

    /**
     * Construct a new time range
     *
     * @param start
     *            Start time
     * @param end
     *            End time
     */
    public LamiTimeRange(long start, long end) {
        fStart = start;
        fEnd = end;
        fDuration = fEnd - fStart;
    }

    /**
     * Get the start time of this time range.
     *
     * @return The start time
     */
    public long getStart() {
        return fStart;
    }

    /**
     * Get the end time of this time range.
     *
     * @return The end time
     */
    public long getEnd() {
        return fEnd;
    }
    /**
     * Get the duration of this time range.
     *
     * @return The duration
     */
    public long getDuration() {
        return fDuration;
    }

    @Override
    public @Nullable String toString() {
        return "[" + String.valueOf(fStart) + " - " + String.valueOf(fEnd) + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
