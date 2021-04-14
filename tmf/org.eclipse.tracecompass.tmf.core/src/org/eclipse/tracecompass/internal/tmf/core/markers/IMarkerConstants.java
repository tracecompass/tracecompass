/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.markers;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.ICyclesConverter;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Marker constants
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IMarkerConstants {

    /** Marker Sets element */
    String MARKER_SETS = "marker-sets"; //$NON-NLS-1$
    /** Marker Set element */
    String MARKER_SET = "marker-set"; //$NON-NLS-1$
    /** Marker element */
    String MARKER = "marker"; //$NON-NLS-1$
    /** SubMarker element */
    String SUBMARKER = "submarker"; //$NON-NLS-1$
    /** Segments element */
    String SEGMENTS = "segments"; //$NON-NLS-1$
    /** Segment element */
    String SEGMENT = "segment"; //$NON-NLS-1$

    /** Name attribute */
    String NAME = "name"; //$NON-NLS-1$
    /** Label attribute */
    String LABEL = "label"; //$NON-NLS-1$
    /** ID attribute */
    String ID = "id"; //$NON-NLS-1$
    /** Reference ID attribute */
    String REFERENCE_ID = "referenceid"; //$NON-NLS-1$
    /** Color attribute */
    String COLOR = "color"; //$NON-NLS-1$
    /** Period attribute */
    String PERIOD = "period"; //$NON-NLS-1$
    /** Unit attribute */
    String UNIT = "unit"; //$NON-NLS-1$
    /** Range attribute */
    String RANGE = "range"; //$NON-NLS-1$
    /** Offset attribute */
    String OFFSET = "offset"; //$NON-NLS-1$
    /** Index attribute */
    String INDEX = "index"; //$NON-NLS-1$
    /** Length attribute */
    String LENGTH = "length"; //$NON-NLS-1$

    /** Milliseconds unit */
    String MS = "ms"; //$NON-NLS-1$
    /** Microseconds unit */
    String US = "us"; //$NON-NLS-1$
    /** Nanoseconds unit */
    String NS = "ns"; //$NON-NLS-1$
    /** Cycles unit */
    String CYCLES = "cycles"; //$NON-NLS-1$

    /** Nanoseconds to milliseconds */
    long NANO_PER_MILLI = 1000000L;
    /** Nanoseconds to microseconds */
    long NANO_PER_MICRO = 1000L;

    /**
     * Converter for a number with unit to
     *
     * @param number
     *            the value of the time to be converted, for 314 us it would be
     *            314.
     * @param unit
     *            the unit, {@link IMarkerConstants#MS},
     *            {@link IMarkerConstants#US},{@link IMarkerConstants#NS} or
     *            {@link IMarkerConstants#CYCLES}
     * @param trace
     *            needed for cycle conversion, can be {@code null}
     * @return a double of nanoseconds. Note, will lose precision for UTC times,
     *         but it is needed to not accumulate errors. This makes sense in
     *         the context of "periodic markers"
     */
    static double convertToNanos(double number, String unit, @Nullable ITmfTrace trace) {
        if (unit.equalsIgnoreCase(MS)) {
            return number * NANO_PER_MILLI;
        } else if (unit.equalsIgnoreCase(US)) {
            return number * NANO_PER_MICRO;
        } else if (unit.equalsIgnoreCase(NS)) {
            return number;
        } else if (unit.equalsIgnoreCase(CYCLES) &&
                trace instanceof IAdaptable) {
            ICyclesConverter adapter = ((IAdaptable) trace).getAdapter(ICyclesConverter.class);
            if (adapter != null) {
                return adapter.cyclesToNanos((long) number);
            }
        }
        return number;
    }
}
