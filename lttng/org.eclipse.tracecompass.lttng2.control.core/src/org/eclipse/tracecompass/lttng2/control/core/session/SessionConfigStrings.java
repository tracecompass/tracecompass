/**********************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Guilliano Molaire - Initial API and implementation
 *********************************************************************/
package org.eclipse.tracecompass.lttng2.control.core.session;

/**
 * This file defines most markers from a session configuration file used to
 * configure a trace session. They can be found in the session configuration
 * schema "session.xsd" in src/common/config/ folder of LTTng-tools.
 *
 * @author Guilliano Molaire
 */
@SuppressWarnings({ "javadoc", "nls" })
public interface SessionConfigStrings {

    /* Session configuration file extension */
    String SESSION_CONFIG_FILE_EXTENSION = "lttng";

    /* Elements of the session configuration file */
    String CONFIG_ELEMENT_SESSIONS = "sessions";
    String CONFIG_ELEMENT_SESSION = "session";
    String CONFIG_ELEMENT_DOMAINS = "domains";
    String CONFIG_ELEMENT_DOMAIN = "domain";
    String CONFIG_ELEMENT_CHANNELS = "channels";
    String CONFIG_ELEMENT_CHANNEL = "channel";
    String CONFIG_ELEMENT_EVENTS = "events";
    String CONFIG_ELEMENT_EVENT = "event";
    String CONFIG_ELEMENT_OUTPUT = "output";
    String CONFIG_ELEMENT_ATTRIBUTES = "attributes";
    String CONFIG_ELEMENT_NET_OUTPUT = "net_output";
    String CONFIG_ELEMENT_MAX_SIZE = "max_size";
    String CONFIG_ELEMENT_SNAPSHOT_OUTPUTS = "snapshot_outputs";
    String CONFIG_ELEMENT_CONSUMER_OUTPUT = "consumer_output";
    String CONFIG_ELEMENT_DESTINATION = "destination";
    String CONFIG_ELEMENT_CONTROL_URI = "control_uri";
    String CONFIG_ELEMENT_DATA_URI = "data_uri";
    String CONFIG_ELEMENT_SNAPSHOT_MODE = "snapshot_mode";
    String CONFIG_ELEMENT_PATH = "path";
    String CONFIG_ELEMENT_NAME = "name";
    String CONFIG_ELEMENT_ENABLED = "enabled";
    String CONFIG_ELEMENT_TYPE = "type";
    String CONFIG_ELEMENT_STARTED = "started";
    String CONFIG_ELEMENT_DOMAIN_BUFFER_TYPE = "buffer_type";
    String CONFIG_ELEMENT_OVERWRITE_MODE = "overwrite_mode";
    String CONFIG_ELEMENT_SUBBUFFER_SIZE = "subbuffer_size";
    String CONFIG_ELEMENT_SUBBUFFER_COUNT = "subbuffer_count";
    String CONFIG_ELEMENT_SWITCH_TIMER_INTERVAL = "switch_timer_interval";
    String CONFIG_ELEMENT_READ_TIMER_INTERVAL = "read_timer_interval";
    String CONFIG_ELEMENT_OUTPUT_TYPE = "output_type";
    String CONFIG_ELEMENT_TRACEFILE_SIZE = "tracefile_size";
    String CONFIG_ELEMENT_TRACEFILE_COUNT = "tracefile_count";
    String CONFIG_ELEMENT_LIVE_TIMER_INTERVAL = "live_timer_interval";
    String CONFIG_ELEMENT_LOGLEVEL_TYPE = "loglevel_type";
    String CONFIG_ELEMENT_LOGLEVEL = "loglevel";

    /* Common element values */
    String CONFIG_STRING_TRUE = "true";
    String CONFIG_STRING_FALSE = "false";
    String CONFIG_STRING_ZERO = "0";

    String CONFIG_DOMAIN_TYPE_KERNEL = "KERNEL";
    String CONFIG_DOMAIN_TYPE_UST = "UST";

    String CONFIG_BUFFER_TYPE_PER_UID = "PER_UID";
    String CONFIG_BUFFER_TYPE_PER_PID = "PER_PID";
    String CONFIG_BUFFER_TYPE_GLOBAL = "GLOBAL";

    String CONFIG_OVERWRITE_MODE_DISCARD = "DISCARD";
    String CONFIG_OVERWRITE_MODE_OVERWRITE = "OVERWRITE";

    String CONFIG_OUTPUT_TYPE_SPLICE = "SPLICE";
    String CONFIG_OUTPUT_TYPE_MMAP = "MMAP";
}
