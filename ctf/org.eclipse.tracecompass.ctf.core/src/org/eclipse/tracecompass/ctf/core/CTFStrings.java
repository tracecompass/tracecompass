/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Matthew Khouzam - Add packet header Strings
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Non-externalized strings for use with the CTF plugin (event names, field
 * names, etc.)
 *
 * @author Alexandre Montplaisir
 * @noimplement This interface only contains static definitions.
 */
@SuppressWarnings("nls")
@NonNullByDefault
public interface CTFStrings {

    /** Event name for lost events */
    String LOST_EVENT_NAME = "Lost event";

    /**
     * Name of the field in lost events indicating how many actual events were
     * lost
     */
    String LOST_EVENTS_FIELD = "Lost events";

    /**
     * Name of the field in lost events indicating the time range
     */
    String LOST_EVENTS_DURATION = "duration";

    // -------------------------------------------------------------------------
    // Packet header strings
    // -------------------------------------------------------------------------

    /**
     * Lost events so far in this stream (LTTng Specific)
     *
     * @since 1.0
     */
    String EVENTS_DISCARDED = "events_discarded";

    /**
     * The CPU ID of this packet (LTTng Specific)
     *
     * @since 1.0
     */
    String CPU_ID = "cpu_id";

    /**
     * The device of this packet
     *
     * @since 1.0
     */
    String DEVICE = "device";

    /**
     * The first time stamp of this packet
     *
     * @since 1.0
     */
    String TIMESTAMP_BEGIN = "timestamp_begin";

    /**
     * The last time stamp of this packet
     *
     * @since 1.0
     */
    String TIMESTAMP_END = "timestamp_end";

    /**
     * Size of this packet
     *
     * @since 1.0
     */
    String PACKET_SIZE = "packet_size";

    /**
     * Size of data in this packet (not necessarily the packet size)
     *
     * @since 1.0
     */
    String CONTENT_SIZE = "content_size";

    /**
     * Magic number
     *
     * @since 1.1
     */
    String MAGIC = "magic";
    /**
     * Header
     *
     * @since 1.1
     */
    String HEADER = "header";
    /**
     * Context
     *
     * @since 1.1
     */
    String CONTEXT = "context";
    /**
     * Packet
     *
     * @since 1.1
     */
    String PACKET = "packet";
    /**
     * Timestamp
     *
     * @since 1.1
     */
    String TIMESTAMP = "timestamp";
}
