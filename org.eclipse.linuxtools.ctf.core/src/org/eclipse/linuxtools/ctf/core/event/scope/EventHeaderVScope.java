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
import org.eclipse.jdt.annotation.Nullable;

/**
 * A lttng specific speedup node (v variant for event headers) of a lexical
 * scope they normally contain a timestamp
 *
 * @author Matthew Khouzam
 * @since 3.1
 */
@NonNullByDefault
public class EventHeaderVScope extends LexicalScope {

    /**
     * Packet header v id string
     */
    public static final LexicalScope PACKET_HEADER_V_ID = new LexicalScope(PACKET_HEADER, "id"); //$NON-NLS-1$
    /**
     * Packet header v timestamp string
     */
    public static final LexicalScope PACKET_HEADER_V_TIMESTAMP = new LexicalScope(PACKET_HEADER, "timestamp"); //$NON-NLS-1$

    /**
     * The scope constructor
     *
     * @param parent
     *            The parent node, can be null, but shouldn't
     * @param name
     *            the name of the field
     */
    public EventHeaderVScope(LexicalScope parent, String name) {
        super(parent, name);
    }

    @Override
    @Nullable
    public LexicalScope getChild(String name) {
        if (name.equals(PACKET_HEADER_V_TIMESTAMP.getName())) {
            return PACKET_HEADER_V_TIMESTAMP;
        }
        if (name.equals(PACKET_HEADER_V_ID.getName())) {
            return PACKET_HEADER_V_ID;
        }
        return super.getChild(name);
    }
}
