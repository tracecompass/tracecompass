/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.scope;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A lttng specific speedup node (the packet header with magic, uuid and stream
 * id ) of a lexical scope the sole reason to have this is to accelerate tostring()
 *
 * @author Matthew Khouzam
 */
@NonNullByDefault
public class PacketHeaderScope extends LexicalScope {

    /**
     * Constructor
     */
    public PacketHeaderScope() {
        super(PACKET, "header"); //$NON-NLS-1$
    }

    @Override
    public String getPath() {
        return "packet.header"; //$NON-NLS-1$
    }

}
