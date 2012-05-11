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

    final private int length;
    final private boolean signed;
    final private int base;
    final private ByteOrder byteOrder;
    final private Encoding encoding;
    final private long alignment;
    final private String clock;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public IntegerDeclaration(int len, boolean signed, int base,
            ByteOrder byteOrder, Encoding encoding, String clock, long alignment) {
        this.length = len;
        this.signed = signed;
        this.base = base;
        this.byteOrder = byteOrder;
        this.encoding = encoding;
        this.clock = clock;
        this.alignment = alignment;
    }

    // ------------------------------------------------------------------------
    // Gettters/Setters/Predicates
    // ------------------------------------------------------------------------

    public boolean isSigned() {
        return signed;
    }

    public int getBase() {
        return base;
    }

    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    public Encoding getEncoding() {
        return encoding;
    }

   public boolean isCharacter() {
        return (length == 8) && (encoding != Encoding.NONE);
    }

    public int getLength() {
        return length;
    }

    @Override
    public long getAlignment(){
        return alignment;
    }

    public String getClock(){
        return clock;
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
