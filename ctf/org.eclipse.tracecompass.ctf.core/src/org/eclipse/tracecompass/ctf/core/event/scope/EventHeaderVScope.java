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
 * A lttng specific speedup node (v variant for event headers) of a lexical
 * scope they normally contain a timestamp
 *
 * @author Matthew Khouzam
 */
@NonNullByDefault
public final class EventHeaderVScope extends LexicalScope {

    /**
     * The scope constructor
     *
     * @param parent
     *            The parent node, can be null, but shouldn't
     * @param name
     *            the name of the field
     */
    EventHeaderVScope(ILexicalScope parent, String name) {
        super(parent, name);
    }

    @Override
    @Nullable
    public ILexicalScope getChild(String name) {
        if (name.equals(EVENT_HEADER_V_TIMESTAMP.getName())) {
            return EVENT_HEADER_V_TIMESTAMP;
        }
        if (name.equals(EVENT_HEADER_V_ID.getName())) {
            return EVENT_HEADER_V_ID;
        }
        return super.getChild(name);
    }
}
