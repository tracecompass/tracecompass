/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Matthew Khouzam - Initial API and implementation
 *  Alexendre Montplaisir - Initial API and implementation
 *  Bernd Hufmann - Add Enum field handling
 *
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import org.eclipse.linuxtools.ctf.core.event.types.ArrayDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.ArrayDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.FloatDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.SequenceDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.SequenceDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StringDefinition;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;

/**
 * The CTF implementation of the TMF event field model
 *
 * @version 2.0
 * @author Matthew Khouzam
 * @author Alexandre Montplaisir
 */
public abstract class CtfTmfEventField implements ITmfEventField {

    // ------------------------------------------------------------------------
    // Class attributes
    // ------------------------------------------------------------------------

    /** @since 2.0 */
    protected static final int FIELDTYPE_INTEGER = 0;

    /** @since 2.0 */
    protected static final int FIELDTYPE_STRING = 1;

    /** @since 2.0 */
    protected static final int FIELDTYPE_INTEGER_ARRAY = 2;

    /** @since 2.0 */
    protected static final int FIELDTYPE_FLOAT = 3;

    /** @since 2.0 */
    protected static final int FIELDTYPE_ENUM = 4;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    protected final String name;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Standard constructor. Only to be used internally, call parseField() to
     * generate a new field object.
     *
     * @param name
     *            The name of this field
     */
    protected CtfTmfEventField(String name) {
        /* Strip the underscore */
        if (name.startsWith("_")) { //$NON-NLS-1$
            this.name = name.substring(1);
        } else {
            this.name = name;
        }
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

    /**
     * Factory method to instantiate CtfTmfEventField objects.
     *
     * @param fieldDef
     *            The CTF Definition of this event field
     * @param fieldName
     *            String The name to assign to this field
     * @return The resulting CtfTmfEventField object
     */
    public static CtfTmfEventField parseField(Definition fieldDef,
            String fieldName) {
        CtfTmfEventField field = null;

        /* Determine the Definition type */
        if (fieldDef instanceof IntegerDefinition) {
            IntegerDefinition intDef = (IntegerDefinition) fieldDef;
            int base = intDef.getDeclaration().getBase();
            field = new CTFIntegerField(intDef.getValue(), fieldName, base);

        } else if (fieldDef instanceof EnumDefinition) {
            EnumDefinition enumDef = (EnumDefinition) fieldDef;
            field = new CTFEnumField(new CtfEnumPair(enumDef.getValue(), enumDef.getIntegerValue()), fieldName);

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
                    values[i] = ((IntegerDefinition) arrayDef.getElem(i))
                            .getValue();
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
                    values[i] = ((IntegerDefinition) seqDef.getElem(i))
                            .getValue();
                }
                field = new CTFIntegerArrayField(values, fieldName);
            }
            /* Add other Sequence types here */

        } else if (fieldDef instanceof FloatDefinition) {
            FloatDefinition floatDef = (FloatDefinition) fieldDef;
            field = new CTFFloatField(floatDef.getValue(), fieldName);
        }

        return field;
    }

    /**
     * Copy factory. Create a new field by (deep-) copying the information in an
     * existing one.
     *
     * @param other
     *            The other CtfTmfEventField to copy
     * @return The new CtfTmfEventField
     */
    public static CtfTmfEventField copyFrom(CtfTmfEventField other) {
        switch (other.getFieldType()) {
        case FIELDTYPE_INTEGER:
            CTFIntegerField intOther = (CTFIntegerField) other;
            return new CTFIntegerField(intOther.getValue(), intOther.name,
                    intOther.getBase());
        case FIELDTYPE_STRING:
            return new CTFStringField(((CTFStringField) other).getValue(),
                    other.name);
        case FIELDTYPE_INTEGER_ARRAY:
            return new CTFIntegerArrayField(
                    ((CTFIntegerArrayField) other).getValue(), other.name);
        case FIELDTYPE_FLOAT:
            return new CTFFloatField(((CTFFloatField) other).getValue(),
                    other.name);
        case FIELDTYPE_ENUM:
            return new CTFEnumField(((CTFEnumField) other).getValue(), other.name);
        default:
            return null;
        }
    }

    @Override
    public CtfTmfEventField clone() {
        return CtfTmfEventField.copyFrom(this);
    }

    // ------------------------------------------------------------------------
    // Abstract methods (to be implemented by each specific field type)
    // ------------------------------------------------------------------------

    /**
     * Return the int representing this field's value type
     *
     * @return The field type
     */
    public abstract int getFieldType();

    /**
     * Return this field's value. You can cast it to the correct type depending
     * on what getFieldType says.
     *
     * @return The field's value
     */
    @Override
    public abstract Object getValue();

    // ------------------------------------------------------------------------
    // Other methods defined by ITmfEventField, but not used here.
    // CTF fields do not have sub-fields (yet!)
    // ------------------------------------------------------------------------

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
 * The CTF field implementation for integer fields.
 *
 * @author alexmont
 */
final class CTFIntegerField extends CtfTmfEventField {

    private final long longValue;
    private final int base;

    /**
     * A CTF "IntegerDefinition" can be an integer of any byte size, so in the
     * Java parser this is interpreted as a long.
     *
     * @param longValue
     *            The integer value of this field
     * @param name
     *            The name of this field
     */
    CTFIntegerField(long longValue, String name, int base) {
        super(name);
        this.longValue = longValue;
        this.base = base;
    }

    /**
     * Return the integer's base. (Not made public until it's needed.)
     *
     * @return The base, usually 10 or 16.
     */
    int getBase() {
        return base;
    }

    @Override
    public int getFieldType() {
        return FIELDTYPE_INTEGER;
    }

    @Override
    public Long getValue() {
        return this.longValue;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        sb.append('=');

        /* Format the number correctly according to the integer's base */
        switch (base) {
        case 2:
            sb.append("0b"); //$NON-NLS-1$
            sb.append(Long.toBinaryString(longValue));
            break;
        case 8:
            sb.append('0');
            sb.append(Long.toOctalString(longValue));
            break;
        case 10:
            sb.append(longValue);
            break;
        case 16:
            sb.append("0x"); //$NON-NLS-1$
            sb.append(Long.toHexString(longValue));
            break;
        default:
            /* Non-standard base, we'll just print it as a decimal number */
            sb.append(longValue);
            break;
        }
        return sb.toString();
    }
}

/**
 * The CTF field implementation for string fields
 *
 * @author alexmont
 */
final class CTFStringField extends CtfTmfEventField {

    private final String strValue;

    /**
     * Constructor for CTFStringField.
     *
     * @param strValue
     *            The string value of this field
     * @param name
     *            The name of this field
     */
    CTFStringField(String strValue, String name) {
        super(name);
        this.strValue = strValue;
    }

    @Override
    public int getFieldType() {
        return FIELDTYPE_STRING;
    }

    @Override
    public String getValue() {
        return this.strValue;
    }

    @Override
    public String toString() {
        return name + '=' + strValue;
    }
}

/**
 * CTF field implementation for arrays of integers.
 *
 * @author alexmont
 */
final class CTFIntegerArrayField extends CtfTmfEventField {

    private final long[] longValues;

    /**
     * Constructor for CTFIntegerArrayField.
     *
     * @param longValues
     *            The array of integers (as longs) that compose this field's
     *            value
     * @param name
     *            The name of this field
     */
    CTFIntegerArrayField(long[] longValues, String name) {
        super(name);
        this.longValues = longValues;
    }

    @Override
    public int getFieldType() {
        return FIELDTYPE_INTEGER_ARRAY;
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

/**
 * CTF field implementation for floats.
 *
 * @author emathko
 */
final class CTFFloatField extends CtfTmfEventField {

    private final Double value;

    /**
     * Constructor for CTFFloatField.
     *
     * @param value
     *            The float value (actually a double) of this field
     * @param name
     *            The name of this field
     */
    protected CTFFloatField(double value, String name) {
        super(name);
        this.value = value;
    }

    @Override
    public int getFieldType() {
        return FIELDTYPE_FLOAT;
    }

    @Override
    public Double getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return name + '=' + value;
    }
}

/**
 * The CTF field implementation for Enum fields
 *
 * @author Bernd Hufmann
 */
final class CTFEnumField extends CtfTmfEventField {

    private final CtfEnumPair value;

    /**
     * Constructor for CTFEnumField.
     *
     * @param enumValue
     *            The Enum value consisting of a pair of Enum value name and its long value
     * @param name
     *            The name of this field
     */
    CTFEnumField(CtfEnumPair enumValue, String name) {
        super(name);
        this.value = new CtfEnumPair(enumValue.getFirst(), enumValue.getSecond().longValue());
    }

    @Override
    public int getFieldType() {
        return FIELDTYPE_ENUM;
    }

    @Override
    public  CtfEnumPair getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return name + '=' + value.toString();
    }
}

/* Implement other possible fields types here... */
