/*******************************************************************************
 * Copyright (c) 2009, 2011, 2012 Ericsson
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
 * <b><u>Messages</u></b>
 * <p>
 */
public class Messages extends NLS {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.ui.views.histogram.messages"; //$NON-NLS-1$
    
	public static String HistogramView_currentEventLabel;
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
