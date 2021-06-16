/*******************************************************************************
 * Copyright (c) 2021 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.markers;

/**
 * Time reference, contains a timestamp and an index. A simple
 * {@link ITimeReference} implementation.
 *
 * @author Matthew Khouzam
 * @since 7.1
 */
public class TimeReference implements ITimeReference {

    private final long time;
    private final long index;

    /**
     * Constructor
     *
     * @param time
     *            the reference marker time in time units
     * @param index
     *            the reference marker index
     */
    public TimeReference(long time, long index) {
        this.time = time;
        this.index = index;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public long getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return String.format("[%d, %d]", time, index); //$NON-NLS-1$
    }
}
