/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Extracted from org.eclipse.linuxtools.tmf.ui
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.viewers.timeAnalysis;

import org.eclipse.osgi.util.NLS;

/**
 * Returns localized strings from the resource bundle (i.e. "messages.properties").
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.lttng.ui.viewers.timeAnalysis.messages"; //$NON-NLS-1$

    public static String TmfTimeFilterDialog_EDIT_PROFILING_OPTIONS;
    public static String TmfTimeFilterDialog_TRACE_FILTER;
    public static String TmfTimeFilterDialog_TRACE_FILTER_DESC;
    public static String TmfTimeFilterDialog_TRACE_ID;
    public static String TmfTimeFilterDialog_TRACE_NAME;
    public static String TmfTimeLegend_LEGEND;
    public static String TmfTimeLegend_TRACE_STATES;
    public static String TmfTimeLegend_TRACE_STATES_TITLE;
    public static String TmfTimeLegend_WINDOW_TITLE;

    public static String TimeScaleCtrl_Timescale;
    public static String TmfTimeStatesCtrl_TRACE_GROUP_LABEL;
    public static String TmfTimeStatesCtrl_UNDEFINED_GROUP;
    public static String TmfTimeTipHandler_DURATION;
    public static String TmfTimeTipHandler_NUMBER_OF_TRACES;
    public static String TmfTimeTipHandler_TRACE_CLASS_NAME;
    public static String TmfTimeTipHandler_TRACE_DATE;
    public static String TmfTimeTipHandler_TRACE_EVENT_TIME;
    public static String TmfTimeTipHandler_TRACE_GROUP_NAME;
    public static String TmfTimeTipHandler_TRACE_NAME;
    public static String TmfTimeTipHandler_TRACE_START_TIME;
    public static String TmfTimeTipHandler_TRACE_STATE;
    public static String TmfTimeTipHandler_TRACE_STOP_TIME;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}