/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, École Polytechnique de Montréal
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
 *  François Doray - Add generic array field type
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.event;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.event.types.AbstractArrayDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.CompoundDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.Definition;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.FloatDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.ICompositeDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StringDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.VariantDefinition;
import org.eclipse.tracecompass.internal.ctf.core.event.types.ByteArrayDefinition;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.ctf.core.CtfEnumPair;

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
     */
    protected CtfTmfEventField(@NonNull String name, Object value, ITmfEventField[] fields) {
        super(/* Strip the underscore from the field name if there is one */
                name.startsWith("_") ? checkNotNull(name.substring(1)) : name, //$NON-NLS-1$
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
     * @deprecated use {@link CtfTmfEventField#parseField(IDefinition, String)}
     */
    @Deprecated
    public static CtfTmfEventField parseField(Definition fieldDef,
            @NonNull String fieldName) {
        return parseField((IDefinition) fieldDef, fieldName);
    }

    /**
     * Factory method to instantiate CtfTmfEventField objects.
     *
     * @param fieldDef
     *            The CTF Definition of this event field
     * @param fieldName
     *            String The name to assign to this field
     * @return The resulting CtfTmfEventField object
     */
    public static @NonNull CtfTmfEventField parseField(IDefinition fieldDef,
            @NonNull String fieldName) {
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

        } else if (fieldDef instanceof AbstractArrayDefinition) {
            AbstractArrayDefinition arrayDef = (AbstractArrayDefinition) fieldDef;
            IDeclaration decl = arrayDef.getDeclaration();
            if (!(decl instanceof CompoundDeclaration)) {
                throw new IllegalArgumentException("Array definitions should only come from sequence or array declarations"); //$NON-NLS-1$
            }
            CompoundDeclaration arrDecl = (CompoundDeclaration) decl;
            IDeclaration elemType = null;
            elemType = arrDecl.getElementType();
            if (arrayDef instanceof ByteArrayDefinition) {
                ByteArrayDefinition byteArrayDefinition = (ByteArrayDefinition) arrayDef;
                /* it's a CTFIntegerArrayField */
                int size = arrayDef.getLength();
                long[] values = new long[size];
                for (int i = 0; i < size; i++) {
                    values[i] = Byte.toUnsignedLong(byteArrayDefinition.getByte(i));
                }
                field = new CTFIntegerArrayField(fieldName, values,
                        16,
                        false);

            }
            if (elemType instanceof IntegerDeclaration) {
                /*
                 * Array of integers => CTFIntegerArrayField, unless it's a
                 * CTFStringField
                 */
                IntegerDeclaration elemIntType = (IntegerDeclaration) elemType;
                /* Are the integers characters and encoded? */
                if (elemIntType.isCharacter()) {
                    /* it's a CTFStringField */
                    field = new CTFStringField(fieldName, arrayDef.toString());
                } else {
                    /* it's a CTFIntegerArrayField */
                    int size = arrayDef.getLength();
                    long[] values = new long[size];
                    for (int i = 0; i < size; i++) {
                        IDefinition elem = arrayDef.getDefinitions().get(i);
                        if (elem == null) {
                            break;
                        }
                        values[i] = ((IntegerDefinition) elem).getValue();
                    }
                    field = new CTFIntegerArrayField(fieldName, values,
                            elemIntType.getBase(),
                            elemIntType.isSigned());
                }
            } else {
                /* Arrays of elements of any other type */
                CtfTmfEventField[] elements = new CtfTmfEventField[arrayDef.getLength()];
                /* Parse the elements of the array. */
                int i = 0;
                List<Definition> definitions = arrayDef.getDefinitions();
                for (IDefinition definition : definitions) {
                    CtfTmfEventField curField = CtfTmfEventField.parseField(
                            definition, fieldName + '[' + i + ']');
                    elements[i] = curField;
                    i++;
                }

                field = new CTFArrayField(fieldName, elements);
            }
        } else if (fieldDef instanceof ICompositeDefinition) {
            ICompositeDefinition strDef = (ICompositeDefinition) fieldDef;

            List<ITmfEventField> list = new ArrayList<>();
            /* Recursively parse the fields */
            for (String fn : strDef.getFieldNames()) {
                list.add(CtfTmfEventField.parseField((IDefinition) strDef.getDefinition(fn), fn));
            }
            field = new CTFStructField(fieldName, list.toArray(new CtfTmfEventField[list.size()]));

        } else if (fieldDef instanceof VariantDefinition) {
            VariantDefinition varDef = (VariantDefinition) fieldDef;

            String curFieldName = checkNotNull(varDef.getCurrentFieldName());
            IDefinition curFieldDef = varDef.getCurrentField();
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
            field = new CTFStringField(fieldName, Messages.CtfTmfEventField_UnsupportedType + fieldDef.getClass().toString());
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

    private final int fBase;
    private final boolean fSigned;

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
    CTFIntegerField(@NonNull String name, long longValue, int base, boolean signed) {
        super(name, longValue, null);
        fSigned = signed;
        fBase = base;
    }

    @Override
    public Long getValue() {
        return (Long) super.getValue();
    }

    @Override
    public String getFormattedValue() {
        return IntegerDefinition.formatNumber(getValue(), fBase, fSigned);
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
    CTFStringField(@NonNull String name, String strValue) {
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

    private final int fBase;
    private final boolean fSigned;
    private String fFormattedValue = null;

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
    CTFIntegerArrayField(@NonNull String name, long[] longValues, int base, boolean signed) {
        super(name, longValues, null);
        fBase = base;
        fSigned = signed;
    }

    @Override
    public long[] getValue() {
        return (long[]) super.getValue();
    }

    @Override
    public synchronized String getFormattedValue() {
        if (fFormattedValue == null) {
            List<String> strings = new ArrayList<>();
            for (long value : getValue()) {
                strings.add(IntegerDefinition.formatNumber(value, fBase, fSigned));
            }
            fFormattedValue = strings.toString();
        }
        return fFormattedValue;
    }

}

/**
 * CTF field implementation for arrays of arbitrary types.
 *
 * @author fdoray
 */
final class CTFArrayField extends CtfTmfEventField {

    private String fFormattedValue = null;

    /**
     * Constructor for CTFArrayField.
     *
     * @param name
     *            The name of this field
     * @param elements
     *            The array elements of this field
     */
    CTFArrayField(@NonNull String name, CtfTmfEventField[] elements) {
        super(name, elements, elements);
    }

    @Override
    public CtfTmfEventField[] getValue() {
        return (CtfTmfEventField[]) super.getValue();
    }

    @Override
    public synchronized String getFormattedValue() {
        if (fFormattedValue == null) {
            List<String> strings = new ArrayList<>();
            for (CtfTmfEventField element : getValue()) {
                strings.add(element.getFormattedValue());
            }
            fFormattedValue = strings.toString();
        }
        return fFormattedValue;
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
    protected CTFFloatField(@NonNull String name, double value) {
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
    CTFEnumField(@NonNull String name, CtfEnumPair enumValue) {
        super(name, new CtfEnumPair(enumValue.getFirst(),
                enumValue.getSecond()), null);
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
    CTFStructField(@NonNull String name, CtfTmfEventField[] fields) {
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
    CTFVariantField(@NonNull String name, CtfTmfEventField field) {
        super(name, field, new CtfTmfEventField[] { field });
    }

    @Override
    public CtfTmfEventField getValue() {
        return (CtfTmfEventField) super.getValue();
    }

}

/* Implement other possible fields types here... */
