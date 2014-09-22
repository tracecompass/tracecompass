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
 * A lttng specific speedup node field scope of a lexical scope
 *
 * @author Matthew Khouzam
 * @since 3.1
 */
@NonNullByDefault
public class FieldsScope extends LexicalScope {

    /**
     * ret field
     */
    public static final LexicalScope FIELDS_RET = new LexicalScope(FIELDS, "_ret"); //$NON-NLS-1$

    /**
     * tid field
     */
    public static final LexicalScope FIELDS_TID = new LexicalScope(FIELDS, "_tid"); //$NON-NLS-1$

    /**
     * The scope constructor
     *
     * @param parent
     *            The parent node, can be null, but shouldn't
     * @param name
     *            the name of the field
     */
    public FieldsScope(LexicalScope parent, String name) {
        super(parent, name);
    }

    @Override
    @Nullable
    public LexicalScope getChild(String name) {
        if (name.equals(FIELDS_RET.getName())) {
            return FIELDS_RET;
        }
        if (name.equals(FIELDS_TID.getName())) {
            return FIELDS_TID;
        }
        return super.getChild(name);
    }

}
