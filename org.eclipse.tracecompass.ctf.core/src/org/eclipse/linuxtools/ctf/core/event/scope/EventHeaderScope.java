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
 * A lttng specific speedup node (the packet header with ID and V) of a lexical
 * scope
 *
 * @author Matthew Khouzam
 * @since 3.1
 */
@NonNullByDefault
public class EventHeaderScope extends LexicalScope {

    /**
     * Event header id string
     */
    public static final LexicalScope EVENT_HEADER_ID = new LexicalScope(EVENT_HEADER, "id"); //$NON-NLS-1$

    /**
     * Event header v as in variant string
     */
    public static final LexicalScope EVENT_HEADER_V = new EventHeaderVScope(EVENT_HEADER, "v"); //$NON-NLS-1$

    /**
     * The scope constructor
     *
     * @param parent
     *            The parent node, can be null, but shouldn't
     * @param name
     *            the name of the field
     */
    public EventHeaderScope(LexicalScope parent, String name) {
        super(parent, name);
    }

    @Override
    @Nullable
    public LexicalScope getChild(String name) {
        if (name.equals(EVENT_HEADER_ID.getName())) {
            return EVENT_HEADER_ID;
        }
        if (name.equals(EVENT_HEADER_V.getName())) {
            return EVENT_HEADER_V;
        }
        return super.getChild(name);
    }

    @Override
    public String toString() {
        return "event.header"; //$NON-NLS-1$
    }

}
