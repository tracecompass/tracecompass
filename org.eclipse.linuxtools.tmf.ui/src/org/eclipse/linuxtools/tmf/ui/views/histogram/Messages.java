/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
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
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.histogram;

import org.eclipse.osgi.util.NLS;

/**
 * Messages file for the histogram widgets.
 * <p>
 * @version 1.0
 * @author Francois Chouinard
 */
public class Messages extends NLS {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.ui.views.histogram.messages"; //$NON-NLS-1$

	/**
	 * The label for the current event time
	 */
	public static String HistogramView_currentEventLabel;
	/**
	 * The label for the window span.
	 */
    public static String HistogramView_windowSpanLabel;

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
