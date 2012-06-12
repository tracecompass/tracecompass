/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis      (mathieu.denis@polymtl.ca)  - Initial API and Implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.statistics;

import org.eclipse.osgi.util.NLS;

/**
 * Messages file for statistics view strings.
 *
 * @version 1.0
 * @author Mathieu Denis
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.ui.views.statistics.messages"; //$NON-NLS-1$
    /**
     * Level column name
     */
    public static String TmfStatisticsView_LevelColumn;
    /**
     * Level column tool tip.
     */
    public static String TmfStatisticsView_LevelColumnTip;
    /**
     * Number of events column name.
     */
    public static String TmfStatisticsView_NbEventsColumn;
    /**
     * Number of events column tool tip.
     */
    public static String TmfStatisticsView_NbEventsTip;
    /**
     * String for unknown trace name.
     */
    public static String TmfStatisticsView_UnknownTraceName;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
