/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   William Bourque - Initial API and implementation
 *   Francois Chouinard - Cleanup and refactoring
 *   Francois Chouinard - Moved from LTTng to TMF
 *   Patrick Tasse - Update for histogram selection range and tool tip
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.histogram;

import org.eclipse.osgi.util.NLS;

/**
 * Messages file for the histogram widgets.
 * <p>
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class Messages extends NLS {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.ui.views.histogram.messages"; //$NON-NLS-1$

    /**
     * @since 3.0
     */
    public static String HistogramView_showTraces;

    /**
     * @since 2.2
     */
    public static String HistogramView_hideLostEvents;
    /**
     * The label for the selection start time
     * @since 2.2
     */
    public static String HistogramView_selectionStartLabel;
    /**
     * The label for the selection end time
     * @since 2.2
     */
    public static String HistogramView_selectionEndLabel;
    /**
     * The label for the window span.
     */
    public static String HistogramView_windowSpanLabel;
    /**
     * The tool tip text for the selection span.
     * @since 2.2
     */
    public static String Histogram_selectionSpanToolTip;
    /**
     * The tool tip text for the bucket range.
     * @since 2.2
     */
    public static String Histogram_bucketRangeToolTip;
    /**
     * The tool tip text for the event count.
     * @since 2.2
     */
    public static String Histogram_eventCountToolTip;
    /**
     * The tool tip text for the lost event count.
     * @since 2.2
     */
    public static String Histogram_lostEventCountToolTip;

    // ------------------------------------------------------------------------
    // Initializer
    // ------------------------------------------------------------------------

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    private Messages() {
    }

}
