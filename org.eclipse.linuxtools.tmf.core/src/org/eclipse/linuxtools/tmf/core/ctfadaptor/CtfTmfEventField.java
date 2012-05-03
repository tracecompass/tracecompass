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
import org.eclipse.linuxtools.ctf.core.event.types.FloatDefinition;
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

    /**
     * Constructor for CtfTmfEventField.
     * @param name String
     */
    protected CtfTmfEventField(String name) {
        /* Strip the damn underscores, screw you CTF */
        if ( name.startsWith("_") ) { //$NON-NLS-1$
            this.name = name.substring(1);
        } else {
            this.name = name;
        }
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Method getName.
     * @return String
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEventField#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Method parseField.
     * @param fieldDef Definition
     * @param fieldName String
     * @return CtfTmfEventField
     */
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
        } else if (fieldDef instanceof FloatDefinition){
            FloatDefinition floatDef = (FloatDefinition) fieldDef;
            field = new CTFFloatField( floatDef.getValue(), fieldName);
        }


        return field;
    }

    /**
     * Method copyFrom.
     * @param other CtfTmfEventField
     * @return CtfTmfEventField
     */
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

    /**
     * Method clone.
     * @return CtfTmfEventField
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEventField#clone()
     */
    @Override
    public CtfTmfEventField clone() {
        return CtfTmfEventField.copyFrom(this);
    }

    /**
     * Return the int representing this field's value type
     *
    
     * @return the field type */
    public abstract int getFieldType();

    /**
     * Return this field's value. You can cast it to the correct type depending
     * on what getFieldType says.
     *
    
     * @return the field value * @see org.eclipse.linuxtools.tmf.core.event.ITmfEventField#getValue()
     */
    @Override
    public abstract Object getValue();

    /**
     * Other methods defined by ITmfEventField, but not used here: the CTF
     *       fields do not have sub-fields (yet!)
     *
    
     * @return the field names * @see org.eclipse.linuxtools.tmf.core.event.ITmfEventField#getFieldNames()
     */
    @Override
    public String[] getFieldNames() {
        return null;
    }

    /**
     * Method getFieldName.
     * @param index int
     * @return String
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEventField#getFieldName(int)
     */
    @SuppressWarnings("unused")
    @Override
    public String getFieldName(int index) {
        return null;
    }

    /**
     * Method getFields.
     * @return ITmfEventField[]
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEventField#getFields()
     */
    @Override
    public ITmfEventField[] getFields() {
        return null;
    }

    /**
     * Method getField.
     * @param fieldName String
     * @return ITmfEventField
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEventField#getField(String)
     */
    @SuppressWarnings("unused")
    @Override
    public ITmfEventField getField(String fieldName) {
        return null;
    }

    /**
     * Method getField.
     * @param index int
     * @return ITmfEventField
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEventField#getField(int)
     */
    @SuppressWarnings("unused")
    @Override
    public ITmfEventField getField(int index) {
        return null;
    }
}

/**
 * <b><u>CTFIntegerField</u></b>
 * @author ematkho
 * @version $Revision: 1.0 $
 */
final class CTFIntegerField extends CtfTmfEventField {

    private final long longValue;

    /**
     * A CTF "IntegerDefinition" can be an integer of any byte size, so in the
     * Java parser this is interpreted as a long.
     * @param longValue long
     * @param name String
     */
    CTFIntegerField(long longValue, String name) {
        super(name);
        this.longValue = longValue;
    }

    /**
     * Method getFieldType.
     * @return int
     */
    @Override
    public int getFieldType() {
        return 0;
    }

    /**
     * Method getValue.
     * @return Long
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEventField#getValue()
     */
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
 * @author ematkho
 * @version $Revision: 1.0 $
 */
final class CTFStringField extends CtfTmfEventField {

    private final String strValue;

    /**
     * Constructor for CTFStringField.
     * @param strValue String
     * @param name String
     */
    CTFStringField(String strValue, String name) {
        super(name);
        this.strValue = strValue;
    }

    /**
     * Method getFieldType.
     * @return int
     */
    @Override
    public int getFieldType() {
        return 1;
    }

    /**
     * Method getValue.
     * @return String
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEventField#getValue()
     */
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
 * @author ematkho
 * @version $Revision: 1.0 $
 */
final class CTFIntegerArrayField extends CtfTmfEventField {

    private final long[] longValues;

    /**
     * Constructor for CTFIntegerArrayField.
     * @param longValues long[]
     * @param name String
     */
    CTFIntegerArrayField(long[] longValues, String name) {
        super(name);
        this.longValues = longValues;
    }

    /**
     * Method getFieldType.
     * @return int
     */
    @Override
    public int getFieldType() {
        return 2;
    }

    /**
     * Method getValue.
     * @return long[]
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEventField#getValue()
     */
    @Override
    public long[] getValue() {
        return this.longValues;
    }

    /**
     * Method toString.
     * @return String
     */
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
 */
final class CTFFloatField extends CtfTmfEventField {

    Double value;
    /**
     * Constructor for CTFFloatField.
     * @param value double
     * @param name String
     */
    protected CTFFloatField(double value ,String name) {
        super(name);
        this.value = value;
    }

    /**
     * Method getFieldType.
     * @return int
     */
    @Override
    public int getFieldType() {
        return 3;
    }

    /**
     * Method getValue.
     * @return Object
     * @see org.eclipse.linuxtools.tmf.core.event.ITmfEventField#getValue()
     */
    @Override
    public Object getValue() {
        return this.value;
    }

    /**
     * Method toString.
     * @return String
     */
    @Override
    public String toString(){
        return name + '=' + value;
    }

}
/* Implement other possible fields types here... */
