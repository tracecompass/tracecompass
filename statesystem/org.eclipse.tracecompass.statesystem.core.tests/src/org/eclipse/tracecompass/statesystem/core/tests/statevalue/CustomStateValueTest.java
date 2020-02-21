/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests.statevalue;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferReader;
import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferWriter;
import org.eclipse.tracecompass.datastore.core.serialization.SafeByteBufferFactory;
import org.eclipse.tracecompass.internal.provisional.statesystem.core.statevalue.CustomStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue.Type;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.statesystem.core.tests.stubs.statevalues.CustomStateValueStub;
import org.junit.Test;

/**
 * Test the {@link CustomStateValue} class
 *
 * @author Geneviève Bastien
 */
public class CustomStateValueTest extends StateValueTestBase {

    private static final CustomStateValue VALUE = new CustomStateValueStub(3, "MyString");

    @Override
    protected @NonNull ITmfStateValue getStateValueFixture() {
        return VALUE;
    }

    @Override
    protected @NonNull Type getStateValueType() {
        return Type.CUSTOM;
    }

    /**
     * Test serialization/unserialization with the factory present
     */
    @Test
    public void testSerialization() {
        CustomStateValueStub.registerFactory();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        ISafeByteBufferWriter safeBufferWriter = SafeByteBufferFactory.wrapWriter(buffer, VALUE.getSerializedSize());
        VALUE.serialize(safeBufferWriter);

        // Reset buffer
        buffer.flip();
        ISafeByteBufferReader safeBufferReader = SafeByteBufferFactory.wrapReader(buffer, VALUE.getSerializedSize());
        TmfStateValue read = CustomStateValue.readSerializedValue(safeBufferReader);
        assertEquals("Custom state value: no factory", VALUE, read);
    }

    /**
     * Test serialization/unserialization if no factory is available
     */
    @Test
    public void testNoFactoryAvailable() {
        CustomStateValueStub.unregisterFactory();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        ISafeByteBufferWriter safeBufferWriter = SafeByteBufferFactory.wrapWriter(buffer, VALUE.getSerializedSize());
        VALUE.serialize(safeBufferWriter);

        // Reset buffer
        buffer.flip();
        ISafeByteBufferReader safeBufferReader = SafeByteBufferFactory.wrapReader(buffer, VALUE.getSerializedSize());
        TmfStateValue read = CustomStateValue.readSerializedValue(safeBufferReader);
        assertEquals("Custom state value: no factory", TmfStateValue.nullValue(), read);
    }

    /**
     * Test the exception when asking a size too large
     */
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testSerializedTooLarge() {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder builder = new StringBuilder();
        while (builder.length() < Short.MAX_VALUE) {
            builder.append(alphabet);
        }
        CustomStateValue sv = new CustomStateValueStub(3, builder.toString());
        sv.getSerializedSize();
    }

    /**
     * Test the illegal argument exception with custom forbidden custom IDs
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWrongCustomId() {
        CustomStateValue.registerCustomFactory((byte) 0, CustomStateValueStub.FACTORY);
    }

    /**
     * Test the illegal argument exception with custom forbidden custom IDs
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWrongCustomId2() {
        CustomStateValue.registerCustomFactory((byte) 20, CustomStateValueStub.FACTORY);
    }

}
