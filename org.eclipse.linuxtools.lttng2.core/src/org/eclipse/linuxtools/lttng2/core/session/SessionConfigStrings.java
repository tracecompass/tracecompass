/**********************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Guilliano Molaire - Initial API and implementation
 *********************************************************************/
package org.eclipse.linuxtools.lttng2.core.session;

/**
 * This file defines most markers from a session configuration file used to
 * configure a trace session. They can be found in the session configuration
 * schema "session.xsd" in src/common/config/ folder of LTTng-tools.
 *
 * @author Guilliano Molaire
 * @since 3.0
 */
@SuppressWarnings({ "javadoc", "nls" })
public interface SessionConfigStrings {

    /* Session configuration file extension */
    static final String SESSION_CONFIG_FILE_EXTENSION = "lttng";

    /* Elements of the session configuration file */
    static final String CONFIG_ELEMENT_SESSIONS = "sessions";
    static final String CONFIG_ELEMENT_SESSION = "session";
    static final String CONFIG_ELEMENT_DOMAINS = "domains";
    static final String CONFIG_ELEMENT_DOMAIN = "domain";
    static final String CONFIG_ELEMENT_CHANNELS = "channels";
    static final String CONFIG_ELEMENT_CHANNEL = "channel";
    static final String CONFIG_ELEMENT_EVENTS = "events";
    static final String CONFIG_ELEMENT_EVENT = "event";
    static final String CONFIG_ELEMENT_OUTPUT = "output";
    static final String CONFIG_ELEMENT_ATTRIBUTES = "attributes";
    static final String CONFIG_ELEMENT_NET_OUTPUT = "net_output";
    static final String CONFIG_ELEMENT_MAX_SIZE = "max_size";
    static final String CONFIG_ELEMENT_SNAPSHOT_OUTPUTS = "snapshot_outputs";
    static final String CONFIG_ELEMENT_CONSUMER_OUTPUT = "consumer_output";
    static final String CONFIG_ELEMENT_DESTINATION = "destination";
    static final String CONFIG_ELEMENT_CONTROL_URI = "control_uri";
    static final String CONFIG_ELEMENT_DATA_URI = "data_uri";
    static final String CONFIG_ELEMENT_SNAPSHOT_MODE = "snapshot_mode";
    static final String CONFIG_ELEMENT_PATH = "path";
    static final String CONFIG_ELEMENT_NAME = "name";
    static final String CONFIG_ELEMENT_ENABLED = "enabled";
    static final String CONFIG_ELEMENT_TYPE = "type";
    static final String CONFIG_ELEMENT_STARTED = "started";
    static final String CONFIG_ELEMENT_DOMAIN_BUFFER_TYPE = "buffer_type";
    static final String CONFIG_ELEMENT_OVERWRITE_MODE = "overwrite_mode";
    static final String CONFIG_ELEMENT_SUBBUFFER_SIZE = "subbuffer_size";
    static final String CONFIG_ELEMENT_SUBBUFFER_COUNT = "subbuffer_count";
    static final String CONFIG_ELEMENT_SWITCH_TIMER_INTERVAL = "switch_timer_interval";
    static final String CONFIG_ELEMENT_READ_TIMER_INTERVAL = "read_timer_interval";
    static final String CONFIG_ELEMENT_OUTPUT_TYPE = "output_type";
    static final String CONFIG_ELEMENT_TRACEFILE_SIZE = "tracefile_size";
    static final String CONFIG_ELEMENT_TRACEFILE_COUNT = "tracefile_count";
    static final String CONFIG_ELEMENT_LIVE_TIMER_INTERVAL = "live_timer_interval";
    static final String CONFIG_ELEMENT_LOGLEVEL_TYPE = "loglevel_type";
    static final String CONFIG_ELEMENT_LOGLEVEL = "loglevel";

    /* Common element values */
    static final String CONFIG_STRING_TRUE = "true";
    static final String CONFIG_STRING_FALSE = "false";
    static final String CONFIG_STRING_ZERO = "0";

    static final String CONFIG_DOMAIN_TYPE_KERNEL = "KERNEL";
    static final String CONFIG_DOMAIN_TYPE_UST = "UST";

    static final String CONFIG_BUFFER_TYPE_PER_UID = "PER_UID";
    static final String CONFIG_BUFFER_TYPE_PER_PID = "PER_PID";
    static final String CONFIG_BUFFER_TYPE_GLOBAL = "GLOBAL";

    static final String CONFIG_OVERWRITE_MODE_DISCARD = "DISCARD";
    static final String CONFIG_OVERWRITE_MODE_OVERWRITE = "OVERWRITE";

    static final String CONFIG_OUTPUT_TYPE_SPLICE = "SPLICE";
    static final String CONFIG_OUTPUT_TYPE_MMAP = "MMAP";
}
