/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.statesystem.core.statevalue;

import java.nio.BufferOverflowException;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferReader;
import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferWriter;
import org.eclipse.tracecompass.internal.statesystem.core.Activator;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;

import com.google.common.annotations.VisibleForTesting;

/**
 * This allows to define custom state values.
 *
 * Each sub-class should define a {@link CustomStateValueFactory} that has to be
 * registered, for example by the analysis using this state value type, through
 * the
 * {@link CustomStateValue#registerCustomFactory(byte, CustomStateValueFactory)}
 * method.
 *
 * Note to implementers: These state values are meant to be used in data
 * structures that save information on objects that varies in time, like a state
 * system. It will often be made persistent on disk at some point, so it is
 * suggested to make the child classes immutable.
 *
 * Data structures using these custom values will often keep the values in a
 * transient state before they are sent to be persisted. For persistence, it
 * will request the size of the value, then write its bytes to a buffer. Once
 * the size is requested, the value is about to be saved, so it is important
 * that its value and size do not change after that, as it may get in an
 * incoherent state, or, worse throw runtime exceptions.
 *
 * @author Geneviève Bastien
 * @since 2.0
 */
public abstract class CustomStateValue extends TmfStateValue {

    /* Minimum size of the state value */
    private static final int MIN_SIZE = Byte.BYTES;

    /**
     * Custom value factory interface. Each custom state value should have a
     * corresponding factory to re-create the object from a ByteBuffer
     */
    @FunctionalInterface
    public interface CustomStateValueFactory {

        /**
         * Get the custom state value from a byte buffer
         *
         * @param buffer
         *            the byte buffer
         * @return the custom state value
         */
        CustomStateValue readCustomValue(ISafeByteBufferReader buffer);
    }

    private static final CustomStateValueFactory[] CUSTOM_FACTORIES = new CustomStateValueFactory[256];

    /**
     * Register the custom factory that will be reused to create instances of
     * custom state value objects
     *
     * @param customId
     *            The ID of this custom type. For possible values of this ID,
     *            see {@link #getCustomTypeId()}
     * @param factory
     *            The factory used to create a new custom type object of this ID
     */
    public static void registerCustomFactory(byte customId, CustomStateValueFactory factory) {
        if (customId >= 0 && customId <= 20) {
            throw new IllegalArgumentException("State value IDs between 0 and 20 are reserved for built-in state value types"); //$NON-NLS-1$
        }
        CustomStateValueFactory currentFactory = CUSTOM_FACTORIES[customId - Byte.MIN_VALUE];
        if (currentFactory == null) {
            CUSTOM_FACTORIES[customId - Byte.MIN_VALUE] = factory;
        } else if (currentFactory != factory) {
            throw new IllegalStateException("Already a custom factory registered for " + Byte.toString(customId)); //$NON-NLS-1$
        }
    }

    /**
     * Unregisters the custom factory
     *
     * @param customId
     *            The ID of this custom type
     */
    @VisibleForTesting
    protected static void unregisterCustomFactory(byte customId) {
        CUSTOM_FACTORIES[customId - Byte.MIN_VALUE] = null;
    }

    /**
     * Get the custom factory for a byte
     *
     * @param customId
     *            the custom factory key
     * @return the custom factory or null if none is registered for the key
     */
    public static @Nullable CustomStateValueFactory getCustomFactory(byte customId) {
        return CUSTOM_FACTORIES[customId - Byte.MIN_VALUE];
    }

    /**
     * Read a serialized value (obtained with the
     * {@link #serialize(ISafeByteBufferWriter)} method) into a real
     * {@link CustomStateValue} object.
     *
     * @param buffer
     *            The byte buffer to read from
     * @return The state value object
     */
    public static TmfStateValue readSerializedValue(ISafeByteBufferReader buffer) {
        /* the first byte = the custom type id */
        byte customType = buffer.get();
        CustomStateValueFactory customFactory = CustomStateValue.getCustomFactory(customType);
        if (customFactory == null) {
            Activator.getDefault().logWarning("Custom factory for type " + customType + " does not exist"); //$NON-NLS-1$ //$NON-NLS-2$
            return TmfStateValue.nullValue();
        }

        return customFactory.readCustomValue(buffer);
    }

    /**
     * Get custom type id. The value should be between {@link Byte#MIN_VALUE}
     * and {@link Byte#MAX_VALUE}, but not between {@code 0} and {@code 20} that
     * are reserved for built-in state value types.
     *
     * @return the custom type ID
     */
    protected abstract Byte getCustomTypeId();

    /**
     * Serialize this state value into the byte buffer. This method should only
     * write the payload of the state value and should match what will be read
     * by the factory when deserializing.
     *
     * This serialized value must contain all the payload of the state value
     *
     * @param buffer
     *            The ByteBuffer to write the values to
     */
    protected abstract void serializeValue(ISafeByteBufferWriter buffer);

    /**
     * Get the serialized size of this state value. The size must not exceed
     * {@link Short#MAX_VALUE - MIN_SIZE}, otherwise serialization might throw a
     * {@link BufferOverflowException}
     *
     * @return The serialized size of this state value
     */
    protected abstract int getSerializedValueSize();

    /**
     * Serialize this custom state value into a byte buffer. It calls the
     * serialization of the state value itself and adds the specific fields to
     * interpret that byte array.
     *
     * The format of the value is [custom type (byte)][payload]
     *
     * The total serialized size should never exceed {@link Short#MAX_VALUE}
     *
     * @param buffer
     *            The ByteBuffer to write the values to
     *
     * @throws BufferOverflowException
     *             If the serialized size of the value ends up larger than the
     *             maximum of {@link Short#MAX_VALUE} and the implementation has
     *             no way of handling it
     */
    public final void serialize(ISafeByteBufferWriter buffer) {
        buffer.put(getCustomTypeId());
        serializeValue(buffer);
    }

    /**
     * Get the serialized size of this state value. This size will be used to
     * allow the buffer that will be sent to the
     * {@link #serialize(ISafeByteBufferWriter)} method
     *
     * @return The size of the serialized value
     */
    public final int getSerializedSize() {
        int size = getSerializedValueSize();
        if (size > Short.MAX_VALUE - MIN_SIZE) {
            throw new ArrayIndexOutOfBoundsException("Serialized state value is larger than the maximum allowed size of " + (Short.MAX_VALUE - MIN_SIZE) + ": " + size); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return size + MIN_SIZE;
    }

    @Override
    public final Type getType() {
        return Type.CUSTOM;
    }

    @Override
    public boolean isNull() {
        return false;
    }

}
