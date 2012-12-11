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

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;

/**
 * A CTF definiton
 *
 * A definition is like an object of a declaration class. It fills the declaration
 * with values. <br>
 * An example: <br>
 * int i = 0; <br>
 * <b>int</b> is the declaration.<br>
 * <b>i</b> is the definition.<br>
 * <b>0</b> is the value assigned to the definition, not the declaration.<br>
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public abstract class Definition {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** The name of the field in its container */
    protected final String fieldName;

    /** The complete path of this field */
    protected final String path;

    /**
     * The definition scope in which this definition is found.
     *
     * The complete path of a definition is thus the path of the definition
     * scope DOT the name of the definition (name of the field in its container)
     */
    protected final IDefinitionScope definitionScope;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param definitionScope
     *            the definition is in a scope, (normally a struct) what is it?
     * @param fieldName
     *            the name of the definition. (it is a field in the parent
     *            scope)
     */
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

    /**
     *
     * @return gets the declaration of a datatype
     *
     */
    public abstract IDeclaration getDeclaration();

    /**
     * Read the definition from a bitbuffer
     *
     * @param input
     *            the bitbuffer containing the data to read.
     * @since 2.0
     */
    public abstract void read(BitBuffer input);

    @Override
    public String toString() {
        return path + '[' + Integer.toHexString(hashCode()) + ']';
    }
}
