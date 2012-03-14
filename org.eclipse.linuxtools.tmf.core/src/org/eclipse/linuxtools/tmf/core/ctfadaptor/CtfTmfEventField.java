/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Alexendre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import org.eclipse.linuxtools.ctf.core.event.types.ArrayDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.ArrayDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.SequenceDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.SequenceDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StringDefinition;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;

/**
 * <b><u>CTFEventField</u></b>
 */
public abstract class CtfTmfEventField implements ITmfEventField {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    protected final String name;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    protected CtfTmfEventField(String name) {
        this.name = name;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    @Override
    public String getName() {
        return this.name;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    public static CtfTmfEventField parseField(Definition fieldDef,
            String fieldName) {
        CtfTmfEventField field = null;

        /* Determine the Definition type */
        if (fieldDef instanceof IntegerDefinition) {
            field = new CTFIntegerField(
                    ((IntegerDefinition) fieldDef).getValue(), fieldName);

        } else if (fieldDef instanceof StringDefinition) {
            field = new CTFStringField(
                    ((StringDefinition) fieldDef).getValue(), fieldName);

        } else if (fieldDef instanceof ArrayDefinition) {
            ArrayDefinition arrayDef = (ArrayDefinition) fieldDef;
            ArrayDeclaration arrayDecl = arrayDef.getDeclaration();

            if (arrayDef.isString()) {
                /* This is an array of UTF-8 bytes, a.k.a. a String! */
                field = new CTFStringField(fieldDef.toString(), fieldName);

            } else if (arrayDecl.getElementType() instanceof IntegerDeclaration) {
                /* This is a an array of CTF Integers */
                long[] values = new long[arrayDecl.getLength()];
                for (int i = 0; i < arrayDecl.getLength(); i++) {
                    values[i] = ((IntegerDefinition) arrayDef.getElem(i)).getValue();
                }
                field = new CTFIntegerArrayField(values, fieldName);
            }
            /* Add other types of arrays here */

        } else if (fieldDef instanceof SequenceDefinition) {
            SequenceDefinition seqDef = (SequenceDefinition) fieldDef;
            SequenceDeclaration seqDecl = seqDef.getDeclaration();

            if (seqDef.getLength() == 0) {
                /* Some sequences have length = 0. Simply use an empty string */
                field = new CTFStringField("", fieldName); //$NON-NLS-1$
            } else if (seqDef.isString()) {
                /* Interpret this sequence as a String */
                field = new CTFStringField(seqDef.toString(), fieldName);
            } else if (seqDecl.getElementType() instanceof IntegerDeclaration) {
                /* Sequence of integers => CTFIntegerArrayField */
                long[] values = new long[seqDef.getLength()];
                for (int i = 0; i < seqDef.getLength(); i++) {
                    values[i] = ((IntegerDefinition) seqDef.getElem(i)).getValue();
                }
                field = new CTFIntegerArrayField(values, fieldName);
            }
            /* Add other Sequence types here */
        }
        /* Add other field types here */

        return field;
    }

    public static CtfTmfEventField copyFrom(CtfTmfEventField other) {
        switch (other.getFieldType()) {
        case 0:
            return new CTFIntegerField(((CTFIntegerField) other).getValue(),
                    other.name);
        case 1:
            return new CTFStringField(((CTFStringField) other).getValue(),
                    other.name);
        case 2:
            return new CTFIntegerArrayField(
                    ((CTFIntegerArrayField) other).getValue(), other.name);
        default:
            return null;
        }
    }

    @Override
    public CtfTmfEventField clone() {
        return CtfTmfEventField.copyFrom(this);
    }

    /**
     * Return the int representing this field's value type
     * 
     * @return
     */
    public abstract int getFieldType();

    /**
     * Return this field's value. You can cast it to the correct type depending
     * on what getFieldType says.
     * 
     * @return
     */
    @Override
    public abstract Object getValue();

    /**
     * @name Other methods defined by ITmfEventField, but not used here: the CTF
     *       fields do not have sub-fields (yet!)
     */

    @Override
    public String[] getFieldNames() {
        return null;
    }

    @Override
    public String getFieldName(int index) {
        return null;
    }

    @Override
    public ITmfEventField[] getFields() {
        return null;
    }

    @Override
    public ITmfEventField getField(String fieldName) {
        return null;
    }

    @Override
    public ITmfEventField getField(int index) {
        return null;
    }
}

/**
 * <b><u>CTFIntegerField</u></b>
 */
final class CTFIntegerField extends CtfTmfEventField {

    private final long longValue;

    /**
     * A CTF "IntegerDefinition" can be an integer of any byte size, so in the
     * Java parser this is interpreted as a long.
     */
    CTFIntegerField(long longValue, String name) {
        super(name);
        this.longValue = longValue;
    }

    @Override
    public int getFieldType() {
        return 0;
    }

    @Override
    public Long getValue() {
        return this.longValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return name + '=' + longValue;
    }
}

/**
 * <b><u>CTFStringField</u></b>
 */
final class CTFStringField extends CtfTmfEventField {

    private final String strValue;

    CTFStringField(String strValue, String name) {
        super(name);
        this.strValue = strValue;
    }

    @Override
    public int getFieldType() {
        return 1;
    }

    @Override
    public String getValue() {
        return this.strValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return name + '=' + strValue;
    }
}

/**
 * <b><u>CTFIntegerArrayField</u></b>
 */
final class CTFIntegerArrayField extends CtfTmfEventField {

    private final long[] longValues;

    CTFIntegerArrayField(long[] longValues, String name) {
        super(name);
        this.longValues = longValues;
    }

    @Override
    public int getFieldType() {
        return 2;
    }

    @Override
    public long[] getValue() {
        return this.longValues;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{ "); //$NON-NLS-1$

        buffer.append(longValues[0]);
        for (int i = 1; i < longValues.length; i++) {
            buffer.append(", " + longValues[i]); //$NON-NLS-1$
        }
        buffer.append('}');
        return name + '=' + buffer.toString();
    }
}

/* Implement other possible fields types here... */
