/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Philippe Sawicki (INF4990.A2010@gmail.com)   - Initial API and implementation
 *   Mathieu Denis    (mathieu.denis55@gmail.com) - Refactored code
 *   Bernd Hufmann - Updated    
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.views.latency;

import org.eclipse.osgi.util.NLS;

/**
 * Returns localized strings from the resource bundle (i.e. "messages.properties").
 * 
 * @author Philippe Sawicki
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.lttng.ui.views.latency.messages"; //$NON-NLS-1$

    public static String LatencyView_ViewName;
    public static String LatencyView_Action_IncreaseBarWidth_Tooltip;
    public static String LatencyView_Action_DecreaseBarWidth_Tooltip;
    public static String LatencyView_Action_AddEvents_Tooltip;
    public static String LatencyView_Action_DeleteEvents_Tooltip;
    public static String LatencyView_Action_ListEvents_Tooltip;
    public static String LatencyView_Dialogs_AddEvents_Title;
    public static String LatencyView_Dialogs_AddEvents_Message;
    public static String LatencyView_Dialogs_AddEvents_Buttons_Add;
    public static String LatencyView_Dialogs_AddEvents_Buttons_Close;
    public static String LatencyView_Dialogs_AddEvents_Columns_Start;
    public static String LatencyView_Dialogs_AddEvents_Columns_End;
    public static String LatencyView_Dialogs_AddEvents_Columns_List_Trigger;
    public static String LatencyView_Dialogs_AddEvents_Columns_List_End;
    public static String LatencyView_Dialogs_AddEvents_Errors_NoSelection;
    public static String LatencyView_Dialogs_AddEvents_Errors_StartNotSelected;
    public static String LatencyView_Dialogs_AddEvents_Errors_EndNotSelected;
    public static String LatencyView_Dialogs_AddEvents_Errors_SameSelected;
    public static String LatencyView_Dialogs_AddEvents_Errors_AlreadyMatched;
    public static String LatencyView_Dialogs_AddEvents_Errors_StartAlreadyMatched;
    public static String LatencyView_Dialogs_AddEvents_Errors_EndAlreadyMatched;
    public static String LatencyView_Dialogs_AddEvents_Errors_StartAsEnd;
    public static String LatencyView_Dialogs_AddEvents_Errors_EndAsStart;
    public static String LatencyView_Dialogs_DeleteEvents_Title;
    public static String LatencyView_Dialogs_DeleteEvents_Message;
    public static String LatencyView_Dialogs_DeleteEvents_Buttons_Close;
    public static String LatencyView_Dialogs_DeleteEvents_Buttons_Delete;
    public static String LatencyView_Dialogs_DeleteEvents_Confirm_Title;
    public static String LatencyView_Dialogs_DeleteEvents_Confirm_Message;
    public static String LatencyView_Dialogs_ListEvents_Title;
    public static String LatencyView_Dialogs_ListEvents_Message;
    public static String LatencyView_Dialogs_ListEvents_Buttons_Close;
    public static String LatencyView_Dialogs_ListEvents_Buttons_Reset;
    public static String LatencyView_Dialogs_ListEvents_Columns_Trigger;
    public static String LatencyView_Dialogs_ListEvents_Columns_End;
    public static String LatencyView_Dialogs_ListEvents_Confirm_Title;
    public static String LatencyView_Dialogs_ListEvents_Confirm_Message;
    public static String LatencyView_Graphs_Graph_Title;
    public static String LatencyView_Graphs_Graph_XAxisLabel;
    public static String LatencyView_Graphs_Graph_YAxisLabel;
    public static String LatencyView_Graphs_Histogram_Title;
    public static String LatencyView_Graphs_Histogram_XAxisLabel;
    public static String LatencyView_Graphs_Histogram_YAxisLabel;
    public static String LatencyView_msgSlogan;
    public static String LatencyView_tmf_UI;
    public static String LatencyView_ClippingWarning;
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}