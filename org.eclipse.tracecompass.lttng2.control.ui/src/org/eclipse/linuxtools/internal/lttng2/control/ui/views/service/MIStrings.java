/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jonathan Rajotte Julien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.control.ui.views.service;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Non-externalized strings for use with the LTTng Control services. This
 * nformation is extracted from mi_lttng.xsd from lttng-tool libmi.
 *
 * @author Jonathan Rajotte
 */
@SuppressWarnings("nls")
@NonNullByDefault
public interface MIStrings {

    /**
     * Represent the command_action xml element
     */
     String COMMAND_ACTION = "snapshot_action";

    /**
     * Represent the command_add_context xml element
     */
     String COMMAND_ADD_CONTEXT = "add-context";

    /**
     * Represent the command_calibrate xml element
     */
     String COMMAND_CALIBRATE = "calibrate";

    /**
     * Represent the command_create xml element
     */
     String COMMAND_CREATE = "create";

    /**
     * Represent the command_destroy xml element
     */
     String COMMAND_DESTROY = "destroy";

    /**
     * Represent the command_disable_channel xml element
     */
     String COMMAND_DISABLE_CHANNEL = "disable-channel";

    /**
     * Represent the command_disable_event xml element
     */
     String COMMAND_DISABLE_EVENT = "disable-event";

    /**
     * Represent the command_enable_channels xml element
     */
     String COMMAND_ENABLE_CHANNELS = "enable-channel";

    /**
     * Represent the command_enable_event xml element
     */
     String COMMAND_ENABLE_EVENT = "enable-event";

    /**
     * Represent the command_list xml element
     */
     String COMMAND_LIST = "list";

    /**
     * Represent the command_load xml element
     */
     String COMMAND_LOAD = "load";

    /**
     * Represent the command_name xml element
     */
     String COMMAND_NAME = "name";

    /**
     * Represent the command_output xml element
     */
     String COMMAND_OUTPUT = "output";

    /**
     * Represent the command_save xml element
     */
     String COMMAND_SAVE = "save";

    /**
     * Represent the command_set_session xml element
     */
     String COMMAND_SET_SESSION = "set-session";

    /**
     * Represent the command_snapshot xml element
     */
     String COMMAND_SNAPSHOT = "snapshot";

    /**
     * Represent the command_snapshot_add xml element
     */
     String COMMAND_SNAPSHOT_ADD = "add_snapshot";

    /**
     * Represent the command_snapshot_del xml element
     */
     String COMMAND_SNAPSHOT_DEL = "del_snapshot";

    /**
     * Represent the command_snapshot_list xml element
     */
     String COMMAND_SNAPSHOT_LIST = "list_snapshot";

    /**
     * Represent the command_snapshot_record xml element
     */
     String COMMAND_SNAPSHOT_RECORD = "record_snapshot";

    /**
     * Represent the command_start xml element
     */
     String COMMAND_START = "start";

    /**
     * Represent the command_stop xml element
     */
     String COMMAND_STOP = "stop";

    /**
     * Represent the command_success xml element
     */
     String COMMAND_SUCCESS = "success";

    /**
     * Represent the command_version xml element
     */
     String COMMAND_VERSION = "version";

    /**
     * Represent the version xml element
     */
     String VERSION = "version";

    /**
     * Represent the version_commit xml element
     */
     String VERSION_COMMIT = "commit";

    /**
     * Represent the version_description xml element
     */
     String VERSION_DESCRIPTION = "description";

    /**
     * Represent the version_license xml element
     */
     String VERSION_LICENSE = "license";

    /**
     * Represent the version_major xml element
     */
     String VERSION_MAJOR = "major";

    /**
     * Represent the version_minor xml element
     */
     String VERSION_MINOR = "minor";

    /**
     * Represent the version_patch_level xml element
     */
     String VERSION_PATCH_LEVEL = "patchLevel";

    /**
     * Represent the version_str xml element
     */
     String VERSION_STR = "string";

    /**
     * Represent the version_web xml element
     */
     String VERSION_WEB = "url";

    /**
     * Represent the version_name xml element
     */
     String VERSION_NAME = "name";
    /* String related to a lttng_event_field */

    /**
     * Represent the event_field xml element
     */
     String EVENT_FIELD = "event_field";

    /**
     * Represent the event_fields xml element
     */
     String EVENT_FIELDS = "event_fields";

    /**
     * Represent the perf_counter_context xml element
     */
     String PERF_COUNTER_CONTEXT = "perf_counter_context";

     // ------------------------------------------------------------------------
     // String related to pid
     // ------------------------------------------------------------------------/

    /**
     * Represent the pids xml element
     */
     String PIDS = "pids";

    /**
     * Represent the pid xml element
     */
     String PID = "pid";

    /**
     * Represent the pid_id xml element
     */
     String PID_ID = "id";

     // ------------------------------------------------------------------------
     // String related to save command
     // ------------------------------------------------------------------------
    /**
     * Represent the save xml element
     */
     String SAVE = "save";

     // ------------------------------------------------------------------------
     // String related to load command
     // ------------------------------------------------------------------------
    /**
     * Represent the load xml element
     */
     String LOAD = "load";

     // ------------------------------------------------------------------------
     // String related to general element of mi_lttng
     // ------------------------------------------------------------------------
    /**
     * Represent the empty xml element
     */
     String EMPTY = "";

    /**
     * Represent the id xml element
     */
     String ID = "id";

    /**
     * Represent the nowrite xml element
     */
     String NOWRITE = "nowrite";

    /**
     * Represent the success xml element
     */
     String SUCCESS = "success";

    /**
     * Represent the type_enum xml element
     */
     String TYPE_ENUM = "ENUM";

    /**
     * Represent the type_float xml element
     */
     String TYPE_FLOAT = "FLOAT";

    /**
     * Represent the type_integer xml element
     */
     String TYPE_INTEGER = "INTEGER";

    /**
     * Represent the type_other xml element
     */
     String TYPE_OTHER = "OTHER";

    /**
     * Represent the type_string xml element
     */
     String TYPE_STRING = "STRING";

     // ------------------------------------------------------------------------
     // String related to lttng_calibrate
     // ------------------------------------------------------------------------
    /**
     * Represent the calibrate xml element
     */
     String CALIBRATE = "calibrate";

    /**
     * Represent the calibrate_function xml element
     */
     String CALIBRATE_FUNCTION = "FUNCTION";

     // ------------------------------------------------------------------------
     // String related to a lttng_snapshot_output
     // ------------------------------------------------------------------------
    /**
     * Represent the snapshot_ctrl_url xml element
     */
     String SNAPSHOT_CTRL_URL = "ctrl_url";

    /**
     * Represent the snapshot_data_url xml element
     */
     String SNAPSHOT_DATA_URL = "data_url";

    /**
     * Represent the snapshot_max_size xml element
     */

     String SNAPSHOT_MAX_SIZE = "max_size";

    /**
     * Represent the snapshot_n_ptr xml element
     */
     String SNAPSHOT_N_PTR = "n_ptr";

    /**
     * Represent the snapshot_session_name xml element
     */
     String SNAPSHOT_SESSION_NAME = "session_name";

    /**
     * Represent the snapshots xml element
     */
     String SNAPSHOTS = "snapshots";
    /**
     * Represent the channel xml element
     */
     String CHANNEL = "channel";

    /**
     * Represent the channels xml element
     */
     String CHANNELS = "channels";

    /**
     * Represent the domain xml element
     */
     String DOMAIN = "domain";

    /**
     * Represent the domains xml element
     */
     String DOMAINS = "domains";

    /**
     * Represent the event xml element
     */
     String EVENT = "event";

    /**
     * Represent the events xml element
     */
     String EVENTS = "events";

    /**
     * Represent the context xml element
     */
     String CONTEXT = "context";

    /**
     * Represent the contexts xml element
     */
     String CONTEXTS = "contexts";

    /**
     * Represent the attributes xml element
     */
     String ATTRIBUTES = "attributes";

    /**
     * Represent the exclusion xml element
     */
     String EXCLUSION = "exclusion";

    /**
     * Represent the exclusions xml element
     */
     String EXCLUSIONS = "exclusions";

    /**
     * Represent the function_attributes xml element
     */
     String FUNCTION_ATTRIBUTES = "function_attributes";

    /**
     * Represent the probe_attributes xml element
     */
     String PROBE_ATTRIBUTES = "probe_attributes";

    /**
     * Represent the symbol_name xml element
     */
     String SYMBOL_NAME = "symbol_name";

    /**
     * Represent the address xml element
     */
     String ADDRESS = "address";

    /**
     * Represent the offset xml element
     */
     String OFFSET = "offset";

    /**
     * Represent the name xml element
     */
     String NAME = "name";

    /**
     * Represent the enabled xml element
     */
     String ENABLED = "enabled";

    /**
     * Represent the overwrite_mode xml element
     */
     String OVERWRITE_MODE = "overwrite_mode";

    /**
     * Represent the subbuf_size xml element
     */
     String SUBBUF_SIZE = "subbuffer_size";

    /**
     * Represent the num_subbuf xml element
     */
     String NUM_SUBBUF = "subbuffer_count";

    /**
     * Represent the switch_timer_interval xml element
     */
     String SWITCH_TIMER_INTERVAL = "switch_timer_interval";

    /**
     * Represent the read_timer_interval xml element
     */
     String READ_TIMER_INTERVAL = "read_timer_interval";

    /**
     * Represent the output xml element
     */
     String OUTPUT = "output";

    /**
     * Represent the output_type xml element
     */
     String OUTPUT_TYPE = "output_type";

    /**
     * Represent the tracefile_size xml element
     */
     String TRACEFILE_SIZE = "tracefile_size";

    /**
     * Represent the tracefile_count xml element
     */
     String TRACEFILE_COUNT = "tracefile_count";

    /**
     * Represent the live_timer_interval xml element
     */
     String LIVE_TIMER_INTERVAL = "live_timer_interval";

    /**
     * Represent the type xml element
     */
     String TYPE = "type";

    /**
     * Represent the buffer_type xml element
     */
     String BUFFER_TYPE = "buffer_type";

    /**
     * Represent the session xml element
     */
     String SESSION = "session";

    /**
     * Represent the sessions xml element
     */
     String SESSIONS = "sessions";

    /**
     * Represent the perf xml element
     */
     String PERF = "perf";

    /**
     * Represent the config xml element
     */
     String CONFIG = "config";

    /**
     * Represent the started xml element
     */
     String STARTED = "started";

    /**
     * Represent the snapshot_mode xml element
     */
     String SNAPSHOT_MODE = "snapshot_mode";

    /**
     * Represent the loglevel xml element
     */
     String LOGLEVEL = "loglevel";

    /**
     * Represent the loglevel_type xml element
     */
     String LOGLEVEL_TYPE = "loglevel_type";

    /**
     * Represent the filter xml element
     */
     String FILTER = "filter";

    /**
     * Represent the snapshot_outputs xml element
     */
     String SNAPSHOT_OUTPUTS = "snapshot";

    /**
     * Represent the consumer_output xml element
     */
     String CONSUMER_OUTPUT = "consumer_output";

    /**
     * Represent the destination xml element
     */
     String DESTINATION = "destination";

    /**
     * Represent the path xml element
     */
     String PATH = "path";

    /**
     * Represent the net_output xml element
     */
     String NET_OUTPUT = "net_output";

    /**
     * Represent the control_uri xml element
     */
     String CONTROL_URI = "control_uri";

    /**
     * Represent the data_uri xml element
     */
     String DATA_URI = "data_uri";

    /**
     * Represent the max_size xml element
     */
    String MAX_SIZE = "max_size";
}
