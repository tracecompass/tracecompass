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

package org.eclipse.tracecompass.internal.pcap.core.util;

/**
 * Enum for the different time precision for pcap files.
 *
 * @author Vincent Perot
 */
public enum PcapTimestampScale {

    /** Microsecond Pcap */
    MICROSECOND,
    /** Nanosecond Pcap */
    NANOSECOND
}
