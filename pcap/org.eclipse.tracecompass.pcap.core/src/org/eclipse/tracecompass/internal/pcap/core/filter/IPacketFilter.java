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

package org.eclipse.tracecompass.internal.pcap.core.filter;

import org.eclipse.tracecompass.internal.pcap.core.packet.Packet;

/**
 * Interface used to filter the packets.
 *
 * @author Vincent Perot
 */
public interface IPacketFilter {

    /**
     * Accept a packet or not.
     *
     * @param packet
     *            the packet to accept or not
     *
     * @return The decision regarding the packet.
     */
    boolean accepts(Packet packet);
}