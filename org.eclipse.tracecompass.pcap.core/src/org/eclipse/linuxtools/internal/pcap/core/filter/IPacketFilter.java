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

package org.eclipse.linuxtools.internal.pcap.core.filter;

import org.eclipse.linuxtools.internal.pcap.core.packet.Packet;

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