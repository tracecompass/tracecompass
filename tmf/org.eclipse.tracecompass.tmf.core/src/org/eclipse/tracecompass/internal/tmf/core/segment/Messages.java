/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.segment;

import org.eclipse.osgi.util.NLS;

/**
 * Message Strings for TMF segment base aspects
 *
 * @author David Piché
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.core.segment.messages"; //$NON-NLS-1$
    /** Segment start time */
    public static String SegmentStartTimeAspect_startDescription;
    /** Segment end time */
    public static String SegmentEndTimeAspect_endDescription;
    /** Segment duration */
    public static String SegmentDurationAspect_durationDescription;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // do nothing
    }
}
