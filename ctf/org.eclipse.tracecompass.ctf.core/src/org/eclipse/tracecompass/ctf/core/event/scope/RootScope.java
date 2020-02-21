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
import org.eclipse.jdt.annotation.Nullable;

/**
 * A lttng specific speedup node a root with accelerated returns for some scopes
 * of a lexical scope
 *
 * @author Matthew Khouzam
 */
@NonNullByDefault
public final class RootScope extends LexicalScope {

    /**
     * The scope constructor
     */
    public RootScope() {
        super();
    }

    @Override
    @Nullable
    public ILexicalScope getChild(String name) {
        /*
         * This happens ~40 % of the time
         */
        if (name.equals(EVENT_HEADER.getPath())) {
            return EVENT_HEADER;
        }
        /*
         * This happens ~30 % of the time
         */
        if (name.equals(FIELDS.getPath())) {
            return FIELDS;
        }
        /*
         * This happens ~30 % of the time
         */
        if (name.equals(CONTEXT.getPath())) {
            return CONTEXT;
        }
        /*
         * This happens ~1 % of the time
         */
        if (name.equals(PACKET_HEADER.getPath())) {
            return PACKET_HEADER;
        }
        return super.getChild(name);
    }

}
