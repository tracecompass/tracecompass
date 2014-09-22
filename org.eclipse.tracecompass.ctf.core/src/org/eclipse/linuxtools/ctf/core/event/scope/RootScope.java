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
 * A lttng specific speedup node a root with accelerated returns for some scopes
 * of a lexical scope
 *
 * @author Matthew Khouzam
 * @since 3.1
 */
@NonNullByDefault
public class RootScope extends LexicalScope {

    /**
     * The scope constructor
     */
    public RootScope() {
        super(null, ""); //$NON-NLS-1$
    }

    @Override
    @Nullable
    public LexicalScope getChild(String name) {
        /*
         * This happens ~40 % of the time
         */
        if (name.equals(EVENT_HEADER.toString())) {
            return EVENT_HEADER;
        }
        /*
         * This happens ~30 % of the time
         */
        if (name.equals(FIELDS.toString())) {
            return FIELDS;
        }
        /*
         * This happens ~30 % of the time
         */
        if (name.equals(CONTEXT.toString())) {
            return CONTEXT;
        }
        /*
         * This happens ~1 % of the time
         */
        if (name.equals(PACKET_HEADER.toString())) {
            return PACKET_HEADER;
        }
        return super.getChild(name);
    }

}
