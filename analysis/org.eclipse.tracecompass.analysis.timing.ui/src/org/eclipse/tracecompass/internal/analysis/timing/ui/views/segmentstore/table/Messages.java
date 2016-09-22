/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   France Lapointe Nguyen - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.table;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.osgi.util.NLS;

/**
 * @author France Lapointe Nguyen
 */
@NonNullByDefault({})
public class Messages extends NLS {
    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    /**
     * Name of the duration column
     */
    public static String SegmentStoreTableViewer_duration;

    /**
     * Name of the end time column
     */
    public static String SegmentStoreTableViewer_endTime;

    /**
     * Name of the start time column
     */
    public static String SegmentStoreTableViewer_startTime;

    /**
     * Title of action to goto start time time
     */
    public static String SegmentStoreTableViewer_goToStartEvent;

    /**
     * Title of action to goto end event
     */
    public static String SegmentStoreTableViewer_goToEndEvent;

    /** Title of the table, to be appended to the analysis name for the title of the view */
    public static String SegmentStoreTableViewer_genericTitle;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
