/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.tracecontrol;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.lttng.ui.tracecontrol.messages"; //$NON-NLS-1$
    
    public static String Property_Type_Provider_Filter;
    public static String Property_Type_Provider;
    
    public static String Lttng_Resource_Root;
    public static String Lttng_Resource_Target;
    public static String Lttng_Resource_Provider;
    public static String Lttng_Resource_Trace;
    public static String Lttng_Resource_Marker;
    public static String Lttng_Resource_Channel;
    
    public static String Filter_Provider_Dlg_Title;
    public static String Filter_Provider_Page_Title;
    public static String Filter_Provider_Page_Text;

    public static String Filter_Target_Dlg_Title;
    public static String Filter_Target_Page_Title;
    public static String Filter_Target_Page_Text;
    public static String Filter_Target_Target_Prompt_label;

    public static String Filter_Trace_Dlg_Title;
    public static String Filter_Trace_Page_Title;
    public static String Filter_Trace_Page_Text;
    public static String Filter_Trace_Target_Prompt_Label;
    
    public static String AllProviders;

    public static String Trace_Connector_Service_Name;
    public static String Trace_Connector_Service_Description;
    public static String Trace_Connector_Service_Connect_Msg;
    public static String Trace_Connector_Service_Disconnect_Msg;
    public static String Trace_Connector_Service_Canceled_Msg;
    
    public static String Lttng_Control_Unknown_Event_Msg;
    
    public static String Lttng_Control_CommandError;

    public static String Lttng_Control_ErrorNewTrace;
    public static String Lttng_Control_ErrorConfigureTrace;
    public static String Lttng_Control_ErrorConfigureMarkers;
    public static String Lttng_Control_ErrorGetMarkers;
    public static String Lttng_Control_ErrorGetMarkerInfo;
    public static String Lttng_Control_ErrorStart;
    public static String Lttng_Control_ErrorPause;
    public static String Lttng_Control_ErrorStop;
    public static String Lttng_Control_ErrorBrowse;
    
    public static String Lttng_Control_GetProvidersError;
    public static String Lttng_Control_GetTargetsError;
    public static String Lttng_Control_GetTracesError;
    public static String Lttng_Control_GetMarkersError;
    public static String Lttng_Control_GetMarkerInfoError;
    
    public static String Ltt_Controller_Service_Not_Connected_Msg;
    public static String Ltt_Controller_Service_Unsupported_Msg;

    public static String Lttng_Control_ErrorSetChannelEnable;
    public static String Lttng_Control_ErrorSetChannelOverwrite;
    public static String Lttng_Control_ErrorSetSubbufNum;
    public static String Lttng_Control_ErrorSetSubbufSize;
    public static String Lttng_Control_ErrorSetChannelTimer;
    
    public static String Lttng_Control_ErrorCreateTracePath;
    
    // Trace resource properties
    public static String Ltt_Trace_Property_TracePathName;
    public static String Ltt_Trace_Property_TracePathDescription;
    public static String Ltt_Trace_Property_NumberOfChannelsName;
    public static String Ltt_Trace_Property_NumberOfChannelsDescr;
    public static String Ltt_Trace_Property_FlighRecorderModeName;
    public static String Ltt_Trace_Property_FlighRecorderModeDesc;
    public static String Ltt_Trace_Property_NormalModeName;
    public static String Ltt_Trace_Property_NormalModeDesc;
    public static String Ltt_Trace_Property_NetworkTraceName;
    public static String Ltt_Trace_Property_NetWorkTraceDescr;
    public static String Ltt_Trace_Property_TraceTransportName;
    public static String Ltt_Trace_Property_TraceTransportDesc;
    
    public static String Ltt_ShutdownWarning;
    public static String Ltt_NetworkTraceRunningWarning;
    
    public static String Ltt_TimeoutMsg;
    
    public static String ConfigureMarkersDialog_Select_All;
    public static String ConfigureMarkersDialog_Call;
    public static String ConfigureMarkersDialog_Cancel;
    public static String ConfigureMarkersDialog_EventId;
    public static String ConfigureMarkersDialog_Format;
    public static String ConfigureMarkersDialog_Location;
    public static String ConfigureMarkersDialog_NameColumn;
    public static String ConfigureMarkersDialog_Ok;
    public static String ConfigureMarkersDialog_Probe_Single;
    public static String ConfigureMarkersDialog_Deselect_All;
    public static String ConfigureMarkersDialog_Title;
    
    public static String ConfigureTraceDialog_Append;
    public static String ConfigureTraceDialog_Browse;
    public static String ConfigureTraceDialog_Cancel;
    public static String ConfigureTraceDialog_Finish;
    public static String ConfigureTraceDialog_Mode_Flight_Recorder;
    public static String ConfigureTraceDialog_Host;
    public static String ConfigureTraceDialog_Target;
    public static String ConfigureTraceDialog_Mode_None;
    public static String ConfigureTraceDialog_Mode_Normal;
    public static String ConfigureTraceDialog_Num_Lttd_Threads;
    public static String ConfigureTraceDialog_Error_Invalid_Folder;
    public static String ConfigureTraceDialog_Error_Invalid_Path;
    public static String ConfigureTraceDialog_Error_Multiple_Seps;
    public static String ConfigureTraceDialog_Error_File_Exists;
    public static String ConfigureTraceDialog_Error_Can_Not_Write;
    public static String ConfigureTraceDialog_Title;
    public static String ConfigureTraceDialog_Trace_Location;
    public static String ConfigureTraceDialog_Trace_Mode;
    public static String ConfigureTraceDialog_Trace_Path;
    public static String ConfigureTraceDialog_Trace_Transport;
    
    public static String NewTraceDialog_Title;
    public static String NewTraceDialog_TraceName;
    public static String NewTraceDialog_Tracing_Project;
    public static String NewTraceDialog_Error_No_Name;
    public static String NewTraceDialog_Error_No_Path;
    public static String NewTraceDialog_Error_No_NumLttdThreads;
    public static String NewTraceDialog_Error_No_Project;
    public static String NewTraceDialog_Error_Already_Exists;
    public static String NewTraceDialog_Error_Invalid_First_Char;
    public static String NewTraceDialog_Error_Invalid_Name;
    
    public static String SeletctTracePathDialog_Title;
    
    public static String DeleteTrace_ConfirmMessage;
	public static String DeleteTrace_ConfirmTitle;

	public static String ImportToProject_AlreadyExists;
	public static String ImportToProject_ImportFailed;
	public static String ImportToProject_NoFileServiceSubsystem;
	public static String ImportToProject_NoProjectTraceFolder;
	public static String ImportToProject_NoRemoteTraceFolder;
	
	public static String ImportTraceDialog_ImportButton;
	public static String ImportTraceDialog_LinkOnly;
	public static String ImportTraceDialog_NameLabel;
	public static String ImportTraceDialog_ProjectColumn;
	public static String ImportTraceDialog_TableLabel;
	public static String ImportTraceDialog_Title;
	
	public static String ChannelConfigPage_BufferOverrideTooltip;
    public static String ChannelConfigPage_ChannelEnabled;
    public static String ChannelConfigPage_ChannelEnabledTooltip;
    public static String ChannelConfigPage_ChannelName;
    public static String ChannelConfigPage_ChannelNameTooltip;
    public static String ChannelConfigPage_ChannelOverride;
    public static String ChannelConfigPage_ChannelTimer;
    public static String ChannelConfigPage_ChannelTimerTooltip;
    public static String ChannelConfigPage_NumSubBuf;
    public static String ChannelConfigPage_NumSubBufTooltip;
    public static String ChannelConfigPage_PageTitle;
    public static String ChannelConfigPage_SubBufSize;
    public static String ChannelConfigPage_SubBufSizeTooltip;
    
    public static String ChannelConfigPage_EnableAll;
    public static String ChannelConfigPage_DisableAll;
    public static String ChannelConfigPage_EnableAllBufferOverride;
    public static String ChannelConfigage_DisableAllBufferOverride;
    public static String ChannelConfigPage_SetAll;
    public static String ChannelConfigPage_SetAllNumSubBuf;
    public static String channelConfigPage_SetAllSubBufSize;
    public static String ChannelConfigPage_SetAllChannelTimer;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
