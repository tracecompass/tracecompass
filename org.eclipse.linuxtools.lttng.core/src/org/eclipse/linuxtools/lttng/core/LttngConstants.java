/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.core;

/**
 * <b><u>LttngConstants</u></b>
 * <p>
 * Declaration of LTTng specific constants.
 * <p>
 */
@SuppressWarnings("nls")
public class LttngConstants {

    /**
     * <h4>Number of bits of an integer to be used for statistic node identifier. </h4>
     */
    public static final int STATS_ID_SHIFT = 28;
    /**
     * <h4>Maximum number of trace ids to be created, before wrapping around to 0. </h4>
     * Note that there is a tight coupling to STATS_ID_SHIFT, because the trace id is
     * also used for statistics node identification.
     */
    public static final int MAX_NUMBER_OF_TRACES_ID = (1 << STATS_ID_SHIFT) - 1;
    public static final int STATS_ID_MASK = MAX_NUMBER_OF_TRACES_ID;

    /**
     * <h4>Statistic node identifier for unknown/none kernel submode. </h4>
     */
    public static final int STATS_NONE_ID = 0x1 << STATS_ID_SHIFT;
    /**
     * <h4>Statistic node identifier for IRQ kernel submodes. </h4>
     */
    public static final int STATS_IRQ_NAME_ID = 0x2 << STATS_ID_SHIFT;
    /**
     * <h4>Statistic node identifier for soft IRQ kernel submodes. </h4>
     */
    public static final int STATS_SOFT_IRQ_NAME_ID = 0x3 << STATS_ID_SHIFT;
    /**
     * <h4>Statistic node identifier for sys_call kernel submodes.</h4>
     */
    public static final int STATS_SYS_CALL_NAME_ID = 0x4 << STATS_ID_SHIFT;
    /**
     * <h4>Statistic node identifier for trab kernel submodes. </h4>
     */
    public static final int STATS_TRAP_NAME_ID = 0x5 << STATS_ID_SHIFT;
    /**
     * <h4>Statistic node identifier for the trace. </h4>
     */
    public static final int STATS_TRACE_NAME_ID = 0x6 << STATS_ID_SHIFT;
    /**
     * <h4>Statistic node identifier for the CPU IDs. </h4>
     */
    public static final int STATS_CPU_ID = 0x7 << STATS_ID_SHIFT;
    /**
     * <h4>Statistic node identifier for the kernel modes. </h4>
     */
    public static final int STATS_MODE_ID = 0x8 << STATS_ID_SHIFT;
    /**
     * <h4>Statistic node identifier for the kernel function IDs. </h4>
     */
    public static final int STATS_FUNCTION_ID = 0x9 << STATS_ID_SHIFT;
    /**
     * <h4>Statistic node identifier for the process IDs. </h4>
     */
    public static final int STATS_PROCESS_ID = 0xA << STATS_ID_SHIFT;
    /**
     * <h4>Statistic node identifier for the event types. </h4>
     */
    public static final int STATS_TYPE_ID = 0xB << STATS_ID_SHIFT;
    /**
     * <h4>Statistic node identifier for the event types. </h4>
     */
    public static final int STATS_CATEGORY_ID = 0xC << STATS_ID_SHIFT;
    /**
     * <h4>Background requests block size </h4>
     */
    public static final int DEFAULT_BLOCK_SIZE = 50000;

    /*
     * LTTng Trace Control Constants
     */
    
    /**
     * <h4>Kernel Provider name.</h4>
     */
    public static final String Lttng_Provider_Kernel = "kernel";
    /**
     * <h4>UST provider name.</h4> 
     */
    public static final String Lttng_Provider_Ust = "ust";
    /**
     * <h4>LTTng trace control command name.</h4> 
     */
    public static final String Lttng_Control_Command = "ltt_control";
    /**
     * <h4>LTTng trace control command to get providers.</h4> 
     */
    public static final String Lttng_Control_GetProviders = "getProviders";
    /**
     * <h4>LTTng trace control command to get targets.</h4> 
     */
    public static final String Lttng_Control_GetTargets = "getTargets";
    /**
     * <h4>LTTng trace control command to get markers.</h4> 
     */
    public static final String Lttng_Control_GetMarkers = "getMarkers";
    /**
     * <h4>LTTng trace control command to get traces.</h4> 
     */
    public static final String Lttng_Control_GetTraces = "getTraces";
    /**
     * <h4>LTTng trace control command to get active traces.</h4> 
     */
    public static final String Lttng_Control_GetActiveTraces = "getActiveTraces";
    /**
     * <h4>LTTng trace control command to get information about a active trace.</h4> 
     */
    public static final String Lttng_Control_GetActiveTraceInfo = "getActiveTraceInfo";
    /**
     * <h4>LTTng trace control command get all available channels.</h4> 
     */
    public static final String Lttng_Control_GetChannels = "getChannels";
    /**
     * <h4>LTTng trace control command to setup a trace.</h4> 
     */
    public static final String Lttng_Control_SetupTrace = "setupTrace";
    /**
     * <h4>LTTng trace control command to set the trace transport.</h4> 
     */
    public static final String Lttng_Control_SetupTraceTransport = "setTraceTransport";
    /**
     * <h4>LTTng trace control command to get information about a marker.</h4> 
     */
    public static final String Lttng_Control_GetMarkerInfo = "getMarkerInfo";
    /**
     * <h4>LTTng trace control command to enable/disable a marker.</h4> 
     */
    public static final String Lttng_Control_SetMarkerEnable = "setMarkerEnable";
    /**
     * <h4>LTTng trace control command to enable/disable a channel.</h4> 
     */
    public static final String Lttng_Control_SetChannelEnable = "setChannelEnable";
    /**
     * <h4>LTTng trace control command to enable/disable channel buffer overwrite.</h4> 
     */
    public static final String Lttng_Control_SetChannelOverwrite = "setChannelOverwrite";
    /**
     * <h4>LTTng trace control command to the channel timer.</h4> 
     */
    public static final String Lttng_Control_SetChannelTimer = "setChannelTimer";
    /**
     * <h4>LTTng trace control command to set the number of sub-buffers of a channel.</h4> 
     */
    public static final String Lttng_Control_SetChannelSubbufNum = "setChannelSubbufNum";
    /**
     * <h4>LTTng trace control command to set the sub-buffer size of a channel.</h4> 
     */
    public static final String Lttng_Control_SetChannelSubbufSize = "setChannelSubbufSize";
    /**
     * <h4>LTTng trace control command to allocate trace resources.</h4> 
     */
    public static final String Lttng_Control_AllocTrace = "allocTrace";
    /**
     * <h4>LTTng trace control command to configure a trace local trace.</h4> 
     */
    public static final String Lttng_Control_WriteTraceLocal = "writeTraceLocal";
    /**
     * <h4>LTTng trace control command to configure a network trace</h4> 
     */
    public static final String Lttng_Control_WriteTraceNetwork = "writeTraceNetwork";
    /**
     * <h4>LTTng trace control command to stop a network trace transfer</h4> 
     */
    public static final String Lttng_Control_StopWriteTraceNetwork = "stopWriteTraceNetwork";
    /**
     * <h4>LTTng trace control command to start tracing.</h4> 
     */
    public static final String Lttng_Control_StartTrace = "startTrace";
    /**
     * <h4>LTTng trace control command to pause tracing.</h4> 
     */
    public static final String Lttng_Control_PauseTrace = "pauseTrace";
    /**
     * <h4>LTTng trace control command to destroy a trace (i.e. deallocate trace resource)</h4> 
     */
    public static final String Lttng_Control_DestroyTrace = "destroyTrace";
    /**
     * <h4>Separator in command replies.</h4> 
     */
    public static final String Lttng_Control_Separator = ",";
    /**
     * <h4>Separator</h4> 
     */
    public static final String Lttng_Control_GetActiveTraceInfoSeparator = "=";
    /**
     * <h4>Action property indicating a enabled action.</h4> 
     */
    public static final String Rse_Resource_Action_Enabled = "yes";

    /**
     * <h4>Action property indicating a disabled action.</h4> 
     */
    public static final String Rse_Resource_Action_Disabled = "no";
}