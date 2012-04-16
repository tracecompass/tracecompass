/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.types;

import org.eclipse.linuxtools.internal.ctf.core.event.io.BitBuffer;

/**
 * <b><u>Definition</u></b>
 */
public abstract class Definition {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /* The name of the field in its container */
    protected final String fieldName;

    /* The complete path of this field */
    protected final String path;

    /*
     * The definition scope in which this definition is found.
     *
     * The complete path of a definition is thus the path of the definition
     * scope DOT the name of the definition (name of the field in its container)
     */
    protected final IDefinitionScope definitionScope;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public Definition(IDefinitionScope definitionScope, String fieldName) {
        this.definitionScope = definitionScope;
        this.fieldName = fieldName;
        if (definitionScope != null) {
            String parentPath = definitionScope.getPath();
            if (parentPath.length() > 0) {
                path = parentPath + "." + fieldName; //$NON-NLS-1$
            } else {
                path = fieldName;
            }
        } else {
            path = fieldName;
        }

        /*
         * System.out.println("[definition] " + this.getClass().getSimpleName()
         * + " " + path + " created");
         */
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    public abstract void read(BitBuffer input);

    @Override
    public String toString() {
        return path + '[' + Integer.toHexString(hashCode()) + ']';
    }
}
