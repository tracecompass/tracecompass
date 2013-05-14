/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Matthew Khouzam - Initial API and implementation
 *  Alexandre Montplaisir - Initial API and implementation, extend TmfEventField
 *  Bernd Hufmann - Add Enum field handling
 *  Geneviève Bastien - Add Struct and Variant field handling
 *  Jean-Christian Kouame - Correct handling of unsigned integer fields
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

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
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.VariantDefinition;
import org.eclipse.linuxtools.internal.tmf.core.Messages;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;

/**
 * The CTF implementation of the TMF event field model
 *
 * @version 2.0
 * @author Matthew Khouzam
 * @author Alexandre Montplaisir
 */
public abstract class CtfTmfEventField extends TmfEventField {

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Standard constructor. Only to be used internally, call parseField() to
     * generate a new field object.
     *
     * @param name
     *            The name of this field
     * @param value
     *            The value of this field. Its type should match the field type.
     * @param fields
     *            The children fields. Useful for composite fields
     * @since 2.0
     */
    protected CtfTmfEventField(String name, Object value, ITmfEventField[] fields) {
        super(/* Strip the underscore from the field name if there is one */
                name.startsWith("_") ? name.substring(1) : name, //$NON-NLS-1$
                value,
                fields);
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
            field = new CTFIntegerField(fieldName, intDef.getValue(), base, intDef.getDeclaration().isSigned());

        } else if (fieldDef instanceof EnumDefinition) {
            EnumDefinition enumDef = (EnumDefinition) fieldDef;
            field = new CTFEnumField(fieldName, new CtfEnumPair(enumDef.getValue(), enumDef.getIntegerValue()));

        } else if (fieldDef instanceof StringDefinition) {
            field = new CTFStringField(fieldName, ((StringDefinition) fieldDef).getValue());

        } else if (fieldDef instanceof FloatDefinition) {
            FloatDefinition floatDef = (FloatDefinition) fieldDef;
            field = new CTFFloatField(fieldName, floatDef.getValue());

        } else if (fieldDef instanceof ArrayDefinition) {
            ArrayDefinition arrayDef = (ArrayDefinition) fieldDef;
            ArrayDeclaration arrayDecl = arrayDef.getDeclaration();

            if (arrayDef.isString()) {
                /* This is an array of UTF-8 bytes, a.k.a. a String! */
                field = new CTFStringField(fieldName, fieldDef.toString());

            } else if (arrayDecl.getElementType() instanceof IntegerDeclaration) {
                /* This is a an array of CTF Integers */
                List<Long> values = new ArrayList<Long>(arrayDecl.getLength());
                for (int i = 0; i < arrayDecl.getLength(); i++) {
                    values.add(((IntegerDefinition) arrayDef.getElem(i)).getValue());
                }
                field = new CTFIntegerArrayField(fieldName, values, ((IntegerDeclaration) arrayDecl.getElementType()).getBase(),
                        ((IntegerDeclaration) arrayDecl.getElementType()).isSigned());
            }
            /* Add other types of arrays here */

        } else if (fieldDef instanceof SequenceDefinition) {
            SequenceDefinition seqDef = (SequenceDefinition) fieldDef;
            SequenceDeclaration seqDecl = seqDef.getDeclaration();

            if (seqDef.getLength() == 0) {
                /* Some sequences have length = 0. Simply use an empty string */
                field = new CTFStringField(fieldName, ""); //$NON-NLS-1$
            } else if (seqDef.isString()) {
                /* Interpret this sequence as a String */
                field = new CTFStringField(fieldName, seqDef.toString());
            } else if (seqDecl.getElementType() instanceof IntegerDeclaration) {
                /* Sequence of integers => CTFIntegerArrayField */
                List<Long> values = new ArrayList<Long>(seqDef.getLength());
                for (int i = 0; i < seqDef.getLength(); i++) {
                    values.add(((IntegerDefinition) seqDef.getElem(i)).getValue());
                }
                field = new CTFIntegerArrayField(fieldName, values, ((IntegerDeclaration) seqDecl.getElementType()).getBase(),
                        ((IntegerDeclaration) seqDecl.getElementType()).isSigned());
            }
            /* Add other Sequence types here */

        } else if (fieldDef instanceof StructDefinition) {
            StructDefinition strDef = (StructDefinition) fieldDef;

            String curFieldName = null;
            Definition curFieldDef;
            CtfTmfEventField curField;
            List<ITmfEventField> list = new ArrayList<ITmfEventField>();
            /* Recursively parse the fields */
            for (Entry<String, Definition> entry : strDef.getDefinitions().entrySet()) {
                curFieldName = entry.getKey();
                curFieldDef = entry.getValue();
                curField = CtfTmfEventField.parseField(curFieldDef, curFieldName);
                list.add(curField);
            }
            field = new CTFStructField(fieldName, list.toArray(new CtfTmfEventField[list.size()]));

        } else if (fieldDef instanceof VariantDefinition) {
            VariantDefinition varDef = (VariantDefinition) fieldDef;

            String curFieldName = varDef.getCurrentFieldName();
            Definition curFieldDef = varDef.getDefinitions().get(curFieldName);
            if (curFieldDef != null) {
                CtfTmfEventField subField = CtfTmfEventField.parseField(curFieldDef, curFieldName);
                field = new CTFVariantField(fieldName, subField);
            } else {
                /* A safe-guard, but curFieldDef should never be null */
                field = new CTFStringField(curFieldName, ""); //$NON-NLS-1$
            }

        } else {
            /*
             * Safe-guard, to avoid null exceptions later, field is expected not
             * to be null
             */
            field = new CTFStringField(fieldName, Messages.TmfEventField_UnsupportedType + fieldDef.getClass().toString());
        }
        return field;
    }

    @Override
    public String toString() {
        return getName() + '=' + getFormattedValue();
    }
}

/**
 * The CTF field implementation for integer fields.
 *
 * @author alexmont
 */
final class CTFIntegerField extends CtfTmfEventField {

    private final int base;
    private final boolean signed;

    /**
     * A CTF "IntegerDefinition" can be an integer of any byte size, so in the
     * Java parser this is interpreted as a long.
     *
     * @param name
     *            The name of this field
     * @param longValue
     *            The integer value of this field
     * @param signed
     *            Is the value signed or not
     */
    CTFIntegerField(String name, long longValue, int base, boolean signed) {
        super(name, longValue, null);
        this.signed = signed;
        this.base = base;
    }

    @Override
    public Long getValue() {
        return (Long) super.getValue();
    }

    @Override
    public String getFormattedValue() {
        return IntegerDefinition.formatNumber(getValue(), base, signed);
    }

}

/**
 * The CTF field implementation for string fields
 *
 * @author alexmont
 */
final class CTFStringField extends CtfTmfEventField {

    /**
     * Constructor for CTFStringField.
     *
     * @param strValue
     *            The string value of this field
     * @param name
     *            The name of this field
     */
    CTFStringField(String name, String strValue) {
        super(name, strValue, null);
    }

    @Override
    public String getValue() {
        return (String) super.getValue();
    }
}

/**
 * CTF field implementation for arrays of integers.
 *
 * @author alexmont
 */
final class CTFIntegerArrayField extends CtfTmfEventField {

    private final int base;
    private final boolean signed;
    private String formattedValue = null;

    /**
     * Constructor for CTFIntegerArrayField.
     *
     * @param name
     *            The name of this field
     * @param longValues
     *            The array of integers (as longs) that compose this field's
     *            value
     * @param signed
     *            Are the values in the array signed or not
     */
    CTFIntegerArrayField(String name, List<Long> longValues, int base, boolean signed) {
        super(name, longValues, null);
        this.base = base;
        this.signed = signed;
    }

    @Override
    public List<Long> getValue() {
        return (List<Long>) super.getValue();
    }

    @Override
    public String getFormattedValue() {
        if (formattedValue == null) {
            List<String> strings = new ArrayList<String>();
            for (Long value : getValue()) {
                strings.add(IntegerDefinition.formatNumber(value, base, signed));
            }
            formattedValue = strings.toString();
        }
        return formattedValue;
    }

}

/**
 * CTF field implementation for floats.
 *
 * @author emathko
 */
final class CTFFloatField extends CtfTmfEventField {

    /**
     * Constructor for CTFFloatField.
     *
     * @param value
     *            The float value (actually a double) of this field
     * @param name
     *            The name of this field
     */
    protected CTFFloatField(String name, double value) {
        super(name, value, null);
    }

    @Override
    public Double getValue() {
        return (Double) super.getValue();
    }
}

/**
 * The CTF field implementation for Enum fields
 *
 * @author Bernd Hufmann
 */
final class CTFEnumField extends CtfTmfEventField {

    /**
     * Constructor for CTFEnumField.
     *
     * @param enumValue
     *            The Enum value consisting of a pair of Enum value name and its
     *            long value
     * @param name
     *            The name of this field
     */
    CTFEnumField(String name, CtfEnumPair enumValue) {
        super(name, new CtfEnumPair(enumValue.getFirst(),
                enumValue.getSecond().longValue()), null);
    }

    @Override
    public CtfEnumPair getValue() {
        return (CtfEnumPair) super.getValue();
    }
}

/**
 * The CTF field implementation for struct fields with sub-fields
 *
 * @author gbastien
 */
final class CTFStructField extends CtfTmfEventField {

    /**
     * Constructor for CTFStructField.
     *
     * @param fields
     *            The children of this field
     * @param name
     *            The name of this field
     */
    CTFStructField(String name, CtfTmfEventField[] fields) {
        super(name, fields, fields);
    }

    @Override
    public CtfTmfEventField[] getValue() {
        return (CtfTmfEventField[]) super.getValue();
    }

    @Override
    public String getFormattedValue() {
        return Arrays.toString(getValue());
    }

}

/**
 * The CTF field implementation for variant fields its child
 *
 * @author gbastien
 */
final class CTFVariantField extends CtfTmfEventField {

    /**
     * Constructor for CTFVariantField.
     *
     * @param field
     *            The field selected for this variant
     * @param name
     *            The name of this field
     */
    CTFVariantField(String name, CtfTmfEventField field) {
        super(name, field, new CtfTmfEventField[] { field });
    }

    @Override
    public CtfTmfEventField getValue() {
        return (CtfTmfEventField) super.getValue();
    }

}

/* Implement other possible fields types here... */
