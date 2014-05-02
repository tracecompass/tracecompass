/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.scope;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A lttng specific speedup node (the packet header with magic, uuid and stream
 * id ) of a lexical scope the sole reason to have this is to accelerate tostring()
 *
 * @author Matthew Khouzam
 * @since 3.1
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
    public String toString() {
        return "packet.header"; //$NON-NLS-1$
    }

}
