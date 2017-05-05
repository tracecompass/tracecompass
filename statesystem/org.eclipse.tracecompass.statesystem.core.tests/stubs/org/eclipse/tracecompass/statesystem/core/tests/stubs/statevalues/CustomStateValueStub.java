/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests.stubs.statevalues;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferWriter;
import org.eclipse.tracecompass.datastore.core.serialization.SafeByteBufferFactory;
import org.eclipse.tracecompass.internal.provisional.statesystem.core.statevalue.CustomStateValue;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;

/**
 * A custom state value stub containing an integer field and a string.
 *
 * Before using this stub in a unit test when there is a need to
 * serialize/unserialize this class, the factory should be registered by calling
 * the {@link #registerFactory()} method
 *
 * @author Geneviève Bastien
 */
public class CustomStateValueStub extends CustomStateValue {

    /**
     * The factory to rebuild the state value
     */
    public static final CustomStateValueFactory FACTORY = (b) -> {
        int val = b.getInt();
        String str = b.getString();
        return new CustomStateValueStub(val, str);
    };

    /** Custom type ID */
    private static final byte CUSTOM_TYPE_ID = 87;

    private final int fIntField;
    private final String fStringField;

    /**
     * Constructor
     *
     * @param val
     *            the integer value
     * @param str
     *            the string value
     */
    public CustomStateValueStub(int val, String str) {
        fIntField = val;
        fStringField = str;
    }

    /**
     * Registers the factory for this custom state value type
     */
    public static void registerFactory() {
        CustomStateValue.registerCustomFactory(CUSTOM_TYPE_ID, FACTORY);
    }

    /**
     * Registers the factory for this custom state value type
     */
    public static void unregisterFactory() {
        CustomStateValue.unregisterCustomFactory(CUSTOM_TYPE_ID);
    }

    @Override
    public int compareTo(@Nullable ITmfStateValue o) {
        if (o == null) {
            throw new IllegalArgumentException();
        }
        if (!(o instanceof CustomStateValueStub)) {
            throw new StateValueTypeException("Need a TestCustomStateValue object to compare to"); //$NON-NLS-1$
        }
        CustomStateValueStub other = (CustomStateValueStub) o;
        int cmp = Integer.compare(fIntField, other.fIntField);
        if (cmp == 0) {
            cmp = fStringField.compareTo(other.fStringField);
        }
        return cmp;
    }

    @Override
    public boolean equals(@Nullable Object arg0) {
        if (!(arg0 instanceof CustomStateValueStub)) {
            return false;
        }
        CustomStateValueStub tcsv = (CustomStateValueStub) arg0;
        return (fIntField == tcsv.fIntField) && fStringField.equals(tcsv.fStringField);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fIntField;
        result = prime * result + fStringField.hashCode();
        return result;
    }

    @Override
    protected @NonNull Byte getCustomTypeId() {
        return CUSTOM_TYPE_ID;
    }

    @Override
    public String toString() {
        return "[" + fIntField + "," + fStringField + "]";
    }

    @Override
    protected void serializeValue(@NonNull ISafeByteBufferWriter buffer) {
        buffer.putInt(fIntField);
        buffer.putString(fStringField);
    }

    @Override
    protected int getSerializedValueSize() {
        return Integer.BYTES + SafeByteBufferFactory.getStringSizeInBuffer(fStringField);
    }

    @Override
    public @Nullable Object unboxValue() {
        return this;
    }

}
