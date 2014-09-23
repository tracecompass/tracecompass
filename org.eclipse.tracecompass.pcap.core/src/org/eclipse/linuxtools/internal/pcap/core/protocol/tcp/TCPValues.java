/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.pcap.core.protocol.tcp;

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
