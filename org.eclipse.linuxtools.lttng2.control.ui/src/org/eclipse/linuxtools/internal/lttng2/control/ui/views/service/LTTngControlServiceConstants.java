/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Bernd Hufmann - Updated for support of LTTng Tools 2.1
 *   Simon Delisle - Updated for support of LTTng Tools 2.2
 *   Marc-Andre Laperle - Support for creating a live session
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.service;

import java.util.regex.Pattern;

/**
 * <p>
 * Constants for LTTng Control Service.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface LTTngControlServiceConstants {

    // ------------------------------------------------------------------------
    // Version constants
    // ------------------------------------------------------------------------
    /**
     * Pattern to match the LTTng toolchain version 2.x.y.
     */
    static final Pattern VERSION_2_PATTERN = Pattern.compile("(2\\.\\d+\\.\\d+).*"); //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Unused value
     */
    static final int UNUSED_VALUE = -1;
    /**
     * String representation of numerical true element
     */
    static final String TRUE_NUMERICAL = "1"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // LTTng Machine Interface constants
    // ------------------------------------------------------------------------

    /**
     * Name of the XSD to validate against the xml machine interface
     * output from LTTng
     */
    static final String MI_XSD_FILENAME = "mi_lttng.xsd"; //$NON-NLS-1$
    // ------------------------------------------------------------------------
    // Command constants
    // ------------------------------------------------------------------------
    /**
     * The lttng tools command.
     */
    static final String CONTROL_COMMAND = "lttng"; //$NON-NLS-1$
    /**
     * The lttng tools machine interface command.
     */
    static final String CONTROL_COMMAND_MI = CONTROL_COMMAND + " --mi "; //$NON-NLS-1$
    /**
     * The lttng tools XML machine interface command.
     */
    static final String CONTROL_COMMAND_MI_XML = CONTROL_COMMAND_MI + " xml "; //$NON-NLS-1$
    /**
     * Command: lttng version.
     */
    static final String COMMAND_VERSION = " version "; //$NON-NLS-1$
    /**
     * Command: lttng list.
     */
    static final String COMMAND_LIST = " list "; //$NON-NLS-1$
    /**
     * Command to list kernel tracer information.
     */
    static final String COMMAND_LIST_KERNEL = COMMAND_LIST + "-k"; //$NON-NLS-1$
    /**
     * Command to list user space trace information.
     */
    static final String COMMAND_LIST_UST = COMMAND_LIST + "-u";  //$NON-NLS-1$
    /**
     * Command to create a session.
     */
    static final String COMMAND_CREATE_SESSION = " create "; //$NON-NLS-1$
    /**
     * Command to destroy a session.
     */
    static final String COMMAND_DESTROY_SESSION = " destroy "; //$NON-NLS-1$
    /**
     * Command to destroy a session.
     */
    static final String COMMAND_START_SESSION = " start "; //$NON-NLS-1$
    /**
     * Command to destroy a session.
     */
    static final String COMMAND_STOP_SESSION = " stop "; //$NON-NLS-1$
    /**
     * Command to enable a channel.
     */
    static final String COMMAND_ENABLE_CHANNEL = " enable-channel "; //$NON-NLS-1$
    /**
     * Command to disable a channel.
     */
    static final String COMMAND_DISABLE_CHANNEL = " disable-channel "; //$NON-NLS-1$
    /**
     * Command to enable a event.
     */
    static final String COMMAND_ENABLE_EVENT = " enable-event "; //$NON-NLS-1$
    /**
     * Command to disable a event.
     */
    static final String COMMAND_DISABLE_EVENT = " disable-event "; //$NON-NLS-1$
    /**
     * Command to add a context to channels and/or events
     */
    static final String COMMAND_ADD_CONTEXT = " add-context "; //$NON-NLS-1$
    /**
     * Command to execute calibrate command to quantify LTTng overhead
     */
    static final String COMMAND_CALIBRATE = " calibrate "; //$NON-NLS-1$
    /**
     * Command to execute calibrate command to quantify LTTng overhead
     */
    static final String COMMAND_LIST_SNAPSHOT_OUTPUT = " snapshot list-output "; //$NON-NLS-1$
    /**
     * Command to execute calibrate command to quantify LTTng overhead
     */
    static final String COMMAND_RECORD_SNAPSHOT = " snapshot record "; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Command line options constants
    // ------------------------------------------------------------------------
    /**
     * Command line option to add tracing group of user.
     */
    static final String OPTION_TRACING_GROUP = " -g ";  //$NON-NLS-1$
    /**
     * Command line option for verbose output.
     */
    static final String OPTION_VERBOSE = " -v ";  //$NON-NLS-1$
    /**
     * Command line option for verbose output.
     */
    static final String OPTION_VERY_VERBOSE = " -vv ";  //$NON-NLS-1$
    /**
     * Command line option for verbose output.
     */
    static final String OPTION_VERY_VERY_VERBOSE = " -vvv ";  //$NON-NLS-1$
    /**
     * Command line option for output path.
     */
    static final String OPTION_OUTPUT_PATH = " -o "; //$NON-NLS-1$
    /**
     * Command line option for output path.
     */
    static final String OPTION_SNAPSHOT = " --snapshot "; //$NON-NLS-1$
    /**
     * Command line option for live
     */
    static final String OPTION_LIVE = " --live "; //$NON-NLS-1$
    /**
     * Command line option for kernel tracer.
     */
    static final String OPTION_KERNEL = " -k "; //$NON-NLS-1$
    /**
     * Command line option for UST tracer.
     */
    static final String OPTION_UST = " -u "; //$NON-NLS-1$
    /**
     * Command line option for specifying a session.
     */
    static final String OPTION_SESSION = " -s ";  //$NON-NLS-1$
    /**
     * Command line option for specifying a channel.
     */
    static final String OPTION_CHANNEL = " -c ";  //$NON-NLS-1$
    /**
     * Command line option for specifying a event.
     */
    static final String OPTION_EVENT = " -e ";  //$NON-NLS-1$
    /**
     * Command line option for specifying all events.
     */
    static final String OPTION_ALL = " -a ";  //$NON-NLS-1$
    /**
     * Command line option for specifying a context.
     */
    static final String OPTION_CONTEXT_TYPE = " -t ";  //$NON-NLS-1$
    /**
     * Command line option for specifying tracepoint events.
     */
    static final String OPTION_TRACEPOINT = " --tracepoint ";  //$NON-NLS-1$
    /**
     * Command line option for specifying syscall events.
     */
    static final String OPTION_SYSCALL = " --syscall ";  //$NON-NLS-1$
    /**
     * Command line option for specifying a dynamic probe.
     */
    static final String OPTION_PROBE = " --probe ";  //$NON-NLS-1$
    /**
     * Command line option for specifying a dynamic function entry/return probe.
     */
    static final String OPTION_FUNCTION_PROBE = " --function ";  //$NON-NLS-1$
    /**
     * Command line option for specifying a log level range.
     */
    static final String OPTION_LOGLEVEL = " --loglevel ";  //$NON-NLS-1$
    /**
     * Command line option for specifying a specific log level.
     */
    static final String OPTION_LOGLEVEL_ONLY = " --loglevel-only ";  //$NON-NLS-1$
    /**
     * Optional command line option for configuring a channel's overwrite mode.
     */
    static final String OPTION_OVERWRITE = " --overwrite ";  //$NON-NLS-1$
    /**
     * Optional command line option for configuring a channel's number of sub buffers.
     */
    static final String OPTION_NUM_SUB_BUFFERS = " --num-subbuf ";  //$NON-NLS-1$
    /**
     * Optional command line option for configuring a channel's sub buffer size.
     */
    static final String OPTION_SUB_BUFFER_SIZE = " --subbuf-size ";  //$NON-NLS-1$
    /**
     * Optional command line option for configuring a channel's switch timer interval.
     */
    static final String OPTION_SWITCH_TIMER = " --switch-timer ";  //$NON-NLS-1$
    /**
     * Optional command line option for configuring a channel's read timer interval.
     */
    static final String OPTION_READ_TIMER = " --read-timer ";  //$NON-NLS-1$
    /**
     * Command line option for printing the help of a specif command
     */
    static final String OPTION_HELP = " -h ";  //$NON-NLS-1$
    /**
     * Command line option for listing the fields of UST tracepoints
     */
    static final String OPTION_FIELDS = " -f "; //$NON-NLS-1$
    /**
     * Command line option for configuring event's filter
     */
    static final String OPTION_FILTER = " --filter "; //$NON-NLS-1$
    /**
     * Command line option for configuring the streaming network URL (common for control and data channel).
     */
    static final String OPTION_NETWORK_URL = " -U "; //$NON-NLS-1$
    /**
     * Command line option for configuring the streaming control URL.
     */
    static final String OPTION_CONTROL_URL = " -C "; //$NON-NLS-1$
    /**
     * Command line option for configuring the streaming data URL.
     */
    static final String OPTION_DATA_URL = " -D "; //$NON-NLS-1$
    /**
     * Command line option for per UID buffers
     */
    static final String OPTION_PER_UID_BUFFERS = " --buffers-uid "; //$NON-NLS-1$
    /**
     * Command line option for per PID buffers
     */
    static final String OPTION_PER_PID_BUFFERS = " --buffers-pid "; //$NON-NLS-1$
    /**
     * Command line option for maximum size of trace files
     */
    static final String OPTION_MAX_SIZE_TRACE_FILES = " -C "; //$NON-NLS-1$
    /**
     * Command line option for maximum trace files
     */
    static final String OPTION_MAX_TRACE_FILES = " -W "; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Parsing constants
    // ------------------------------------------------------------------------
    /**
     * Pattern to match the version.
     */
    static final Pattern VERSION_PATTERN = Pattern.compile(".*lttng\\s+version\\s+(\\d+\\.\\d+\\.\\d+).*"); //$NON-NLS-1$
    /**
     * Pattern to match for error output
     */
    static final Pattern ERROR_PATTERN = Pattern.compile("\\s*Error\\:.*"); //$NON-NLS-1$
    /**
     * Pattern to match for session information (lttng list)
     */
    static final Pattern SESSION_PATTERN = Pattern.compile("\\s+(\\d+)\\)\\s+(.*)\\s+\\((.*)\\)\\s+\\[(active|inactive).*\\].*"); //$NON-NLS-1$
    /**
     * Pattern to match for session information (lttng list <session>)
     */
    static final Pattern TRACE_SESSION_PATTERN = Pattern.compile("\\s*Tracing\\s+session\\s+(.*)\\:\\s+\\[(active|inactive)\\].*"); //$NON-NLS-1$
    /**
     * Pattern to match for snapshot session information (lttng list <session>)
     */
    static final Pattern TRACE_SNAPSHOT_SESSION_PATTERN = Pattern.compile("\\s*Tracing\\s+session\\s+(.*)\\:\\s+\\[(active|inactive)\\s*snapshot\\].*"); //$NON-NLS-1$
    /**
     * Pattern to match for session path information (lttng list <session>)
     */
    static final Pattern TRACE_SESSION_PATH_PATTERN = Pattern.compile("\\s*Trace\\s+path\\:\\s+(.*)"); //$NON-NLS-1$
    /**
     * Pattern to match session path for network tracing (lttng list <session>)
     * Note: file for protocol is not considered as network trace since local consumer will be used.
     */
    static final Pattern TRACE_NETWORK_PATH_PATTERN = Pattern.compile("\\s*Trace\\s+path\\:\\s+(net|net4|net6|tcp|tcp6)\\:\\/\\/(.*)(\\:(\\d*)\\/(.*)\\[data\\:\\s+(\\d*)\\]){0,1}"); //$NON-NLS-1$
    /**
     * Pattern to match session path for network tracing
     * Note: file for protocol is not considered as network trace since local consumer will be used.
     */
    static final Pattern TRACE_NETWORK_PATTERN = Pattern.compile("\\s*(net|net4|net6|tcp|tcp4|tcp6)\\:\\/\\/(.*)(\\:(\\d*)\\/(.*)\\[data\\:\\s+(\\d*)\\]){0,1}"); //$NON-NLS-1$
    /**
     * Sub-pattern to pattern TRACE_NETWORK_PATH_PATTERN to match file protocol
     */
    static final Pattern TRACE_FILE_PROTOCOL_PATTERN = Pattern.compile("(file)\\:\\/\\/(.*)"); //$NON-NLS-1$
    /**
     * Pattern to match for kernel domain information (lttng list <session>)
     */
    static final Pattern DOMAIN_KERNEL_PATTERN = Pattern.compile("=== Domain: Kernel ==="); //$NON-NLS-1$
    /**
     * Pattern to match for ust domain information (lttng list <session>)
     */
    static final Pattern DOMAIN_UST_GLOBAL_PATTERN = Pattern.compile("=== Domain: UST global ==="); //$NON-NLS-1$
    /**
     * Pattern to match for matching warning about no kernel channel
     */
    static final Pattern DOMAIN_NO_KERNEL_CHANNEL_PATTERN = Pattern.compile("\\s*Warning\\:\\s+No kernel\\s+channel.*"); //$NON-NLS-1$
    /**
     * Pattern to match for matching warning about no UST channel
     */
    static final Pattern DOMAIN_NO_UST_CHANNEL_PATTERN = Pattern.compile("\\s*Error\\:\\s+UST\\s+channel\\s+not\\s+found.*"); //$NON-NLS-1$
    /**
     * Pattern to match for buffer type (lttng list <session>)
     */
    static final Pattern BUFFER_TYPE_PATTERN = Pattern.compile("\\s*Buffer\\s+type\\:.*"); //$NON-NLS-1$
    /**
     * Pattern to match for channels section (lttng list <session>)
     */
    static final Pattern CHANNELS_SECTION_PATTERN = Pattern.compile("\\s*Channels\\:"); //$NON-NLS-1$
    /**
     * Pattern to match for channel information (lttng list <session>)
     */
    static final Pattern CHANNEL_PATTERN = Pattern.compile("\\s*-\\s+(.*)\\:\\s+\\[(enabled|disabled)\\]"); //$NON-NLS-1$
    /**
     * Pattern to match for events section information (lttng list <session>)
     */
    static final Pattern EVENT_SECTION_PATTERN = Pattern.compile("\\s*Events\\:"); //$NON-NLS-1$
    /**
     * Pattern to match for event information (lttng list <session>)
     */
    static final Pattern EVENT_PATTERN = Pattern.compile("\\s+(.*)\\s+\\(loglevel\\s*(:|<=|==)\\s+(.*)\\s+\\(\\d*\\)\\)\\s+\\(type:\\s+(.*)\\)\\s+\\[(enabled|disabled)\\]\\s*(\\[.*\\]){0,1}.*"); //$NON-NLS-1$
    /**
     * Pattern to match a wildcarded event information (lttng list <session>)
     */
    static final Pattern WILDCARD_EVENT_PATTERN = Pattern.compile("\\s+(.*)\\s+\\(type:\\s+(.*)\\)\\s+\\[(enabled|disabled)\\]\\s*(\\[.*\\]){0,1}.*"); //$NON-NLS-1$
    /**
     * Pattern to match a probe address information (lttng list <session>)
     */
    static final Pattern PROBE_ADDRESS_PATTERN = Pattern.compile("\\s+(addr)\\:\\s+(0x[0-9a-fA-F]{1,8})"); //$NON-NLS-1$
    /**
     * Pattern to match a probe OFFSET information (lttng list <session>)
     */
    static final Pattern PROBE_OFFSET_PATTERN = Pattern.compile("\\s+(offset)\\:\\s+(0x[0-9a-fA-F]{1,8})"); //$NON-NLS-1$
    /**
     * Pattern to match a probe SYMBOL information (lttng list <session>)
     */
    static final Pattern PROBE_SYMBOL_PATTERN = Pattern.compile("\\s+(symbol)\\:\\s+(.+)"); //$NON-NLS-1$
    /**
     * Pattern to match for channel (overwite mode) information (lttng list <session>)
     */
    static final Pattern OVERWRITE_MODE_ATTRIBUTE = Pattern.compile("\\s+overwrite\\s+mode\\:.*"); //$NON-NLS-1$
    /**
     * Pattern to match indicating false for overwrite mode
     */
    static final String OVERWRITE_MODE_ATTRIBUTE_FALSE = "0"; //$NON-NLS-1$
    /**
     * Pattern to match indicating false for overwrite mode in machine interface mode
     */
    static final String OVERWRITE_MODE_ATTRIBUTE_FALSE_MI = "DISCARD"; //$NON-NLS-1$
    /**
     * Pattern to match for channel (sub-buffer size) information (lttng list <session>)
     */
    static final Pattern SUBBUFFER_SIZE_ATTRIBUTE = Pattern.compile("\\s+subbufers\\s+size\\:.*"); //$NON-NLS-1$
    /**
     * Pattern to match for channel (number of sub-buffers) information (lttng list <session>)
     */
    static final Pattern NUM_SUBBUFFERS_ATTRIBUTE = Pattern.compile("\\s+number\\s+of\\s+subbufers\\:.*"); //$NON-NLS-1$
    /**
     * Pattern to match for channel (switch timer) information (lttng list <session>)
     */
    static final Pattern SWITCH_TIMER_ATTRIBUTE = Pattern.compile("\\s+switch\\s+timer\\s+interval\\:.*"); //$NON-NLS-1$
    /**
     * Pattern to match for channel (read timer) information (lttng list <session>)
     */
    static final Pattern READ_TIMER_ATTRIBUTE = Pattern.compile("\\s+read\\s+timer\\s+interval\\:.*"); //$NON-NLS-1$
    /**
     * Pattern to match for channel (output type) information (lttng list <session>)
     */
    static final Pattern OUTPUT_ATTRIBUTE = Pattern.compile("\\s+output\\:.*"); //$NON-NLS-1$
    /**
     * Pattern to match for provider information (lttng list -k/-u)
     */
    static final Pattern PROVIDER_EVENT_PATTERN = Pattern.compile("\\s*(.*)\\s+\\(loglevel:\\s+(.*)\\s+\\(\\d*\\)\\)\\s+\\(type:\\s+(.*)\\)"); //$NON-NLS-1$
    /**
     * Pattern to match event fields
     */
    static final Pattern EVENT_FIELD_PATTERN = Pattern.compile("\\s*(field:)\\s+(.*)\\s+\\((.*)\\)"); //$NON-NLS-1$
    /**
     * Pattern to match for UST provider information (lttng list -u)
     */
    static final Pattern UST_PROVIDER_PATTERN = Pattern.compile("\\s*PID\\:\\s+(\\d+)\\s+-\\s+Name\\:\\s+(.*)"); //$NON-NLS-1$
    /**
     * Pattern to match for session information (lttng create <session name>)
     */
    static final Pattern CREATE_SESSION_NAME_PATTERN = Pattern.compile(".*Session\\s+(.*)\\s+created\\."); //$NON-NLS-1$
    /**
     * Pattern to match for session path information (lttng create <session name>)
     */
    static final Pattern CREATE_SESSION_PATH_PATTERN = Pattern.compile("\\s*Traces\\s+will\\s+be\\s+written\\s+in\\s+(.*).*"); //$NON-NLS-1$
    /**
     * Pattern to match for session command output for "session name not found".
     */
    static final Pattern SESSION_NOT_FOUND_ERROR_PATTERN = Pattern.compile("\\s*Error:\\s+Session\\s+name\\s.*not\\s+found"); //$NON-NLS-1$
    /**
     * Pattern to match introduction line of context list.
     */
    static final Pattern ADD_CONTEXT_HELP_CONTEXTS_INTRO = Pattern.compile("\\s*TYPE can\\s+be\\s+one\\s+of\\s+the\\s+strings\\s+below.*"); //$NON-NLS-1$
    /**
     * Pattern to match introduction line of context list.
     */
    static final Pattern ADD_CONTEXT_HELP_CONTEXTS_END_LINE = Pattern.compile("\\s*Example.*"); //$NON-NLS-1$
    /**
     * Pattern to match error line if no kernel tracer is available or installed.
     */
    static final Pattern LIST_KERNEL_NO_KERNEL_PROVIDER_PATTERN = Pattern.compile("\\s*Error:\\s+Unable\\s+to\\s+list\\s+kernel\\s+events.*"); //$NON-NLS-1$;
    /**
     * Pattern to match error line if no ust tracer is available or installed.
     */
    static final Pattern LIST_UST_NO_UST_PROVIDER_PATTERN = Pattern.compile(".*Unable\\s*to\\s*list\\s*UST\\s*event.*"); //$NON-NLS-1$;
    /**
     * Pattern to match for list snapshot information (lttng snapshot list-output)
     */
    static final Pattern LIST_SNAPSHOT_OUTPUT_PATTERN = Pattern.compile("\\s+\\[(\\d+)\\]\\s+(.*)\\:\\s+(.*)"); //$NON-NLS-1$
    /**
     * Pattern to match snapshot path for network tracing (lttng list <session>)
     * Note: file for protocol is not considered as network trace since local consumer will be used.
     */
    static final Pattern SNAPSHOT_NETWORK_PATH_PATTERN = Pattern.compile("(net|net4|net6|tcp|tcp6)\\:\\/\\/(.*)(\\:(\\d*)\\/(.*)\\[data\\:\\s+(\\d*)\\]){0,1}"); //$NON-NLS-1$

}
