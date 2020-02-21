/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.pcap.core.protocol.tcp;

/**
 * Interface that lists constants related to TCP.
 *
 * See http://en.wikipedia.org/wiki/Transmission_Control_Protocol#TCP_segment_structure.
 *
 * @author Vincent Perot
 */
public interface TCPValues {

    /** Size in bytes of a default TCP packet header */
    int DEFAULT_HEADER_LENGTH = 5;

    /** Size in bytes of a block of data. Used to convert data block to bytes. */
    int BLOCK_SIZE = 4;

}
