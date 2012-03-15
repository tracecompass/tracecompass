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

import java.nio.ByteOrder;

/**
 * <b><u>IntegerDeclaration</u></b>
 */
public class IntegerDeclaration implements IDeclaration {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private int length;
    private boolean signed;
    private int base;
    private ByteOrder byteOrder;
    private Encoding encoding;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public IntegerDeclaration(int len, boolean signed, int base,
            ByteOrder byteOrder, Encoding encoding) {
        this.length = len;
        this.signed = signed;
        this.base = base;
        this.byteOrder = byteOrder;
        this.encoding = encoding;
    }

    // ------------------------------------------------------------------------
    // Gettters/Setters/Predicates
    // ------------------------------------------------------------------------

    public boolean isSigned() {
        return signed;
    }

    public void setSigned(boolean signed) {
        this.signed = signed;
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    public void setByteOrder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }

    public Encoding getEncoding() {
        return encoding;
    }

    public void setEncoding(Encoding encoding) {
        this.encoding = encoding;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean isCharacter() {
        return (length == 8) && (encoding != Encoding.NONE);
    }

    public int getLength() {
        return length;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public IntegerDefinition createDefinition(IDefinitionScope definitionScope,
            String fieldName) {
        return new IntegerDefinition(this, definitionScope, fieldName);
    }

    @Override
    public String toString() {
        /* Only used for debugging */
        return "[declaration] integer[" + Integer.toHexString(hashCode()) + ']'; //$NON-NLS-1$
    }

}
