/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API and Implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.viewers.statistics.model;

import org.eclipse.osgi.util.NLS;

/**
 * Message strings for the statistics framework.
 *
 * @author Mathieu Denis
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.ui.viewers.statistics.model.messages"; //$NON-NLS-1$

    /**
     * CPU statistic name.
     */
    public static String TmfStatisticsData_CPUs;

    /**
     * Event type statistic name.
     */
    public static String TmfStatisticsData_EventTypes;

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
     * Partial number of events column.
     */
    public static String TmfStatisticsView_NbEventsTimeRangeColumn;

    /**
     * Partial number of events column tool tip.
     */
    public static String TmfStatisticsView_NbEventsTimeRangeTip;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
