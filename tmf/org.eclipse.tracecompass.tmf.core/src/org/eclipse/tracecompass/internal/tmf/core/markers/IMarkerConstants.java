/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.markers;

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
}
