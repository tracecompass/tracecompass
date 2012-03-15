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


@SuppressWarnings("nls")
public class TraceControlConstants {

    // LTTng Resource Constants
    public static final String Rse_Provider_Resource_Remote_Type_Category = "providers";
    public static final String Rse_Provider_Resource_Remote_Type = "provider";
    public static final String Rse_Target_Resource_Remote_Type_Category = "targets";
    public static final String Rse_Target_Resource_Remote_Type = "target";
    public static final String Rse_Trace_Resource_Remote_Type_Category = "traces";
    public static final String Rse_Trace_Resource_Remote_Type = "trace";
    public static final String Lttng_Providers_Name = "Providers";
    public static final String Lttng_Ust_TraceName = "auto";
    public static final String Lttng_Trace_Transport_Relay = "relay";
    public static final String Lttng_Control_AllChannels = "all";
    public static final String Lttng_Control_New_Event_Data = "new_trace_data";
    public static final String Lttng_Control_Unwrite_Trace_Data_Event = "unwrite_trace_data";
    public static final String Lttng_Control_Trace_Done_Event = "trace_done";

    // the parameter names have to be coordinated with lttctltraceinfo.c in lttng-agent
    public static final String ACTIVE_TRACE_INFO_PARAM_DESTINATION = "destination";
    public static final String ACTIVE_TRACE_INFO_PARAM_NUM_THREAD = "numThread";
    public static final String ACTIVE_TRACE_INFO_PARAM_NORMAL_ONLY = "normal_only";
    public static final String ACTIVE_TRACE_INFO_PARAM_FLIGHT_ONLY = "flight_only";
    public static final String ACTIVE_TRACE_INFO_PARAM_ENABLED = "enabled";

    // the destination prefixes have to be coordinated with lttctlkerntransfer.c in lttng-agent
    public static final String ACTIVE_TRACE_INFO_DESTINATION_PREFIX_LOCAL = "local:";
    public static final String ACTIVE_TRACE_INFO_DESTINATION_PREFIX_NETWORK = "network:";

    // Default timeout for TCF tasks (in seconds)
    public static final int DEFAULT_TCF_TASK_TIMEOUT = 10;
}
