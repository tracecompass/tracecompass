/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.inputoutput;

/**
 * This file defines all the attribute names used in the handler. Both the
 * construction and query steps should use them.
 *
 * These should not be externalized! The values here are used as-is in the
 * history file on disk, so they should be kept the same to keep the file format
 * compatible. If a view shows attribute names directly, the localization should
 * be done on the viewer side.
 *
 * @author Houssem Daoud
 */
@SuppressWarnings({ "nls" })
public interface Attributes {

    /* First-level attributes */

    /** Root attribute for disks */
    String DISKS = "Disks";
    /** Root attribute of the waiting queue requests */
    String WAITING_QUEUE = "Waiting_queue";
    /** Root attribute of the driver queue requests */
    String DRIVER_QUEUE = "Driver_queue";
    /** Length of the driver queue */
    String DRIVER_QUEUE_LENGTH = "driverqueue_length";
    /** Length of the waiting queue */
    String WAITING_QUEUE_LENGTH = "waitingqueue_length";
    /** Base sector of the request */
    String CURRENT_REQUEST = "Current_request";
    /** Size of a request */
    String REQUEST_SIZE = "Request_size";
    /** Type of a request */
    String TYPE = "Type";
    /**
     * Contains the request in the waiting queue to which this request was
     * merged
     */
    String MERGED_IN = "merged_in";
    /** The request in the waiting queue this driver request was issued from */
    String ISSUED_FROM = "issued_from";
    /** Number of sectors read */
    String SECTORS_READ = "sectors_read";
    /** Number of sectors written */
    String SECTORS_WRITTEN = "sectors_written";

    /** System call root attribute */
    String SYSTEM_CALLS_ROOT = "system_calls";

    /** Root attribute for thread r/w */
    String THREADS = "Threads";
    /** Number of bytes read by a thread */
    String BYTES_READ = "bytes_read";
    /** Number of bytes written by a thread */
    String BYTES_WRITTEN = "bytes_written";

}
