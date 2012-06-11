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

import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

/**
 * <b><u>SequenceDeclaration</u></b> <br>
 * An array where the size is fixed but declared in the trace, unlike array
 * where it is declared with a literal
 */
public class SequenceDeclaration implements IDeclaration {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final IDeclaration elemType;
    private final String lengthName;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param lengthName
     *            the name of the field describing the length
     * @param elemType
     *            The element type
     */
    public SequenceDeclaration(String lengthName, IDeclaration elemType) {
        this.elemType = elemType;
        this.lengthName = lengthName;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Gets the element type
     * @return the element type
     */
    public IDeclaration getElementType() {
        return elemType;
    }

    /**
     * Gets the name of the length field
     * @return the name of the length field
     */
    public String getLengthName() {
        return lengthName;
    }

    @Override
    public long getAlignment() {
        return getElementType().getAlignment();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public SequenceDefinition createDefinition(
            IDefinitionScope definitionScope, String fieldName) {
        SequenceDefinition ret = null;
        try {
            ret = new SequenceDefinition(this, definitionScope, fieldName);
        } catch (CTFReaderException e) {
            // Temporarily catch this here, eventually this should be thrown
            // up the call stack
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public String toString() {
        /* Only used for debugging */
        return "[declaration] sequence[" + Integer.toHexString(hashCode()) + ']'; //$NON-NLS-1$
    }

}
