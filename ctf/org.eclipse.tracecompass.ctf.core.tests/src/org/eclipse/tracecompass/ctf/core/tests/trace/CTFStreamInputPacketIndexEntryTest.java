/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.ILexicalScope;
import org.eclipse.tracecompass.ctf.core.event.types.Encoding;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.FloatDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StringDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;
import org.eclipse.tracecompass.ctf.core.trace.ICTFPacketDescriptor;
import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputPacketIndexEntry;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StreamInputPacketIndexEntryTest</code> contains tests for the
 * class <code>{@link StreamInputPacketIndexEntry}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class CTFStreamInputPacketIndexEntryTest {

    private ICTFPacketDescriptor fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new StreamInputPacketIndexEntry(1L, 1L);
    }

    /**
     * Run the StreamInputPacketIndexEntry(long) constructor test.
     */
    @Test
    public void testStreamInputPacketIndexEntry_1() {
        String expectedResult = "StreamInputPacketIndexEntry [offsetBits=1, " +
                "timestampBegin=" + 0 +
                ", timestampEnd=" + Long.MAX_VALUE +
                "]";

        assertNotNull(fixture);
        assertEquals(expectedResult, fixture.toString());
        assertEquals(1, fixture.getOffsetBits());
        assertEquals(1, fixture.getOffsetBytes());
    }

    /**
     * Test the constructor
     *
     * @throws CTFException
     *             exception
     */
    @Test
    public void testStreamInputPacketIndexEntryConstructor1() throws CTFException {
        StructDeclaration sd = new StructDeclaration(8);
        sd.addField("timestamp_begin", IntegerDeclaration.INT_32B_DECL);
        sd.addField("timestamp_end", IntegerDeclaration.INT_32B_DECL);
        sd.addField("load_factor", new FloatDeclaration(24, 8, ByteOrder.nativeOrder(), 8));
        sd.addField("target", StringDeclaration.getStringDeclaration(Encoding.ASCII));
        final EnumDeclaration declaration = new EnumDeclaration(IntegerDeclaration.INT_8_DECL);
        declaration.add(-100, 100, "");
        sd.addField("Enum", declaration);
        BitBuffer bb = new BitBuffer(ByteBuffer.allocate(128));
        bb.getByteBuffer().putInt(100);
        bb.getByteBuffer().putInt(200);
        bb.getByteBuffer().putFloat((float) .75);
        bb.getByteBuffer().put(("Test").getBytes());
        bb.getByteBuffer().put((byte) 0);
        bb.getByteBuffer().put((byte) 0);
        StructDefinition sdef = sd.createDefinition(null, ILexicalScope.PACKET_HEADER, bb);
        ICTFPacketDescriptor sipie = new StreamInputPacketIndexEntry(0, sdef, 128, 0);
        assertNull(sipie.getTarget());
        assertEquals(100, sipie.getTimestampBegin());
        assertEquals(200, sipie.getTimestampEnd());
    }

    /**
     * Test the constructor
     *
     * @throws CTFException
     *             exception
     */
    @Test
    public void testStreamInputPacketIndexEntryConstructor2() throws CTFException {
        StructDeclaration sd = new StructDeclaration(8);
        sd.addField("timestamp_begin", IntegerDeclaration.INT_32B_DECL);
        sd.addField("timestamp_end", IntegerDeclaration.INT_32B_DECL);
        sd.addField("content_size", IntegerDeclaration.INT_32B_DECL);
        sd.addField("target", StringDeclaration.getStringDeclaration(Encoding.ASCII));
        final EnumDeclaration declaration = new EnumDeclaration(IntegerDeclaration.INT_8_DECL);
        declaration.add(-100, 100, "");
        sd.addField("Enum", declaration);
        BitBuffer bb = new BitBuffer(ByteBuffer.allocate(128));
        bb.getByteBuffer().putInt(100);
        bb.getByteBuffer().putInt(200);
        bb.getByteBuffer().putInt(128);
        bb.getByteBuffer().put(("Test").getBytes());
        bb.getByteBuffer().put((byte) 0);
        bb.getByteBuffer().put((byte) 0);
        StructDefinition sdef = sd.createDefinition(null, ILexicalScope.PACKET_HEADER, bb);
        ICTFPacketDescriptor sipie = new StreamInputPacketIndexEntry(0, sdef, 128, 0);
        assertNull(sipie.getTarget());
        assertEquals(100, sipie.getTimestampBegin());
        assertEquals(200, sipie.getTimestampEnd());
    }

    /**
     * Test the constructor
     *
     * @throws CTFException
     *             exception
     */
    @Test
    public void testStreamInputPacketIndexEntryConstructor3() throws CTFException {
        StructDeclaration sd = new StructDeclaration(8);
        sd.addField("timestamp_begin", IntegerDeclaration.INT_32B_DECL);
        sd.addField("timestamp_end", IntegerDeclaration.INT_32B_DECL);
        sd.addField("packet_size", IntegerDeclaration.INT_32B_DECL);
        sd.addField("target", StringDeclaration.getStringDeclaration(Encoding.ASCII));
        final EnumDeclaration declaration = new EnumDeclaration(IntegerDeclaration.INT_8_DECL);
        declaration.add(-100, 100, "");
        sd.addField("Enum", declaration);
        sd.addField("intruder", new StructDeclaration(8));
        BitBuffer bb = new BitBuffer(ByteBuffer.allocate(128));
        bb.getByteBuffer().putInt(100);
        bb.getByteBuffer().putInt(200);
        bb.getByteBuffer().putInt(128);
        bb.getByteBuffer().put(("Test").getBytes());
        bb.getByteBuffer().put((byte) 0);
        bb.getByteBuffer().put((byte) 0);
        StructDefinition sdef = sd.createDefinition(null, ILexicalScope.PACKET_HEADER, bb);
        ICTFPacketDescriptor sipie = new StreamInputPacketIndexEntry(0, sdef, 128, 0);
        assertNull(sipie.getTarget());
        assertEquals(100, sipie.getTimestampBegin());
        assertEquals(200, sipie.getTimestampEnd());
        assertTrue(sipie.includes(150));
        assertFalse(sipie.includes(10));
        assertFalse(sipie.includes(250));
    }

    /**
     * Test the constructor
     *
     * @throws CTFException
     *             exception
     */
    @Test
    public void testStreamInputPacketIndexEntryConstructor4() throws CTFException {
        StructDeclaration sd = new StructDeclaration(8);
        sd.addField("content_size", IntegerDeclaration.INT_32B_DECL);
        sd.addField("target", StringDeclaration.getStringDeclaration(Encoding.ASCII));
        final EnumDeclaration declaration = new EnumDeclaration(IntegerDeclaration.INT_8_DECL);
        declaration.add(-100, 100, "");
        sd.addField("Enum", declaration);
        BitBuffer bb = new BitBuffer(ByteBuffer.allocate(128));
        bb.getByteBuffer().putInt(0);
        bb.getByteBuffer().put(("Test").getBytes());
        bb.getByteBuffer().put((byte) 0);
        bb.getByteBuffer().put((byte) 0);
        StructDefinition sdef = sd.createDefinition(null, ILexicalScope.PACKET_HEADER, bb);
        ICTFPacketDescriptor sipie = new StreamInputPacketIndexEntry(0, sdef, 128, 0);
        assertNull(sipie.getTarget());
        assertEquals(0, sipie.getTimestampBegin());
        assertEquals(Long.MAX_VALUE, sipie.getTimestampEnd());
    }

    /**
     * Test the constructor
     *
     * @throws CTFException
     *             exception
     */
    @Test
    public void testStreamInputPacketIndexEntryConstructor5() throws CTFException {
        StructDeclaration sd = new StructDeclaration(8);
        sd.addField("timestamp_end", IntegerDeclaration.INT_32B_DECL);
        sd.addField("content_size", IntegerDeclaration.INT_32B_DECL);
        sd.addField("device", StringDeclaration.getStringDeclaration(Encoding.ASCII));
        final EnumDeclaration declaration = new EnumDeclaration(IntegerDeclaration.INT_8_DECL);
        declaration.add(-100, 100, "");
        sd.addField("Enum", declaration);
        BitBuffer bb = new BitBuffer(ByteBuffer.allocate(128));
        bb.getByteBuffer().putInt(-1);
        bb.getByteBuffer().putInt(0);
        bb.getByteBuffer().put(("Test66").getBytes());
        bb.getByteBuffer().put((byte) 0);
        bb.getByteBuffer().put((byte) 0);
        StructDefinition sdef = sd.createDefinition(null, ILexicalScope.PACKET_HEADER, bb);
        ICTFPacketDescriptor sipie = new StreamInputPacketIndexEntry(0, sdef, 128, 0);
        assertEquals(0, sipie.getTimestampBegin());
        assertEquals(Long.MAX_VALUE, sipie.getTimestampEnd());
        assertEquals("Test66", sipie.getTarget());
        assertEquals(66, sipie.getTargetId());
    }

    /**
     * Test the constructor
     *
     * @throws CTFException
     *             exception
     */
    @Test
    public void testStreamInputPacketIndexEntryConstructor6() throws CTFException {
        StructDeclaration sd = new StructDeclaration(8);
        sd.addField("timestamp_end", IntegerDeclaration.INT_32B_DECL);
        sd.addField("content_size", IntegerDeclaration.INT_32B_DECL);
        sd.addField("cpu_id", IntegerDeclaration.INT_32B_DECL);
        sd.addField("events_discarded", IntegerDeclaration.INT_32B_DECL);
        BitBuffer bb = new BitBuffer(ByteBuffer.allocate(128));
        bb.getByteBuffer().putInt(-1);
        bb.getByteBuffer().putInt(0);
        bb.getByteBuffer().putInt(66);
        bb.getByteBuffer().putInt(300);
        StructDefinition sdef = sd.createDefinition(null, ILexicalScope.PACKET_HEADER, bb);
        ICTFPacketDescriptor sipie = new StreamInputPacketIndexEntry(0, sdef, 128, 100);
        assertEquals(0, sipie.getTimestampBegin());
        assertEquals(Long.MAX_VALUE, sipie.getTimestampEnd());
        assertEquals("CPU66", sipie.getTarget());
        assertEquals(66, sipie.getTargetId());
        assertEquals(200, sipie.getLostEvents());
        assertEquals(0, sipie.getOffsetBits());
        assertEquals(1024, sipie.getPacketSizeBits());
    }

    /**
     * Test the constructor
     *
     * @throws CTFException
     *             exception
     */
    @Test
    public void testStreamInputPacketIndexEntryConstructor7() throws CTFException {
        StructDeclaration sd = new StructDeclaration(8);
        sd.addField("timestamp_end", IntegerDeclaration.INT_32B_DECL);
        sd.addField("content_size", IntegerDeclaration.INT_32B_DECL);
        final EnumDeclaration declaration = new EnumDeclaration(IntegerDeclaration.createDeclaration(16, false, 10, ByteOrder.BIG_ENDIAN, Encoding.NONE, "", 8));
        declaration.add(313, 315, "CPU-PI");
        sd.addField("device", declaration);
        BitBuffer bb = new BitBuffer(ByteBuffer.allocate(128));
        bb.getByteBuffer().putInt(-1);
        bb.getByteBuffer().putInt(0);
        bb.getByteBuffer().putShort((short) 314);
        StructDefinition sdef = sd.createDefinition(null, ILexicalScope.PACKET_HEADER, bb);
        ICTFPacketDescriptor sipie = new StreamInputPacketIndexEntry(0, sdef, 128, 0);
        assertEquals(0, sipie.getTimestampBegin());
        assertEquals(Long.MAX_VALUE, sipie.getTimestampEnd());
        assertEquals("CPU-PI", sipie.getTarget());
        assertEquals(314, sipie.getTargetId());
    }

    /**
     * Test the constructor
     *
     * @throws CTFException
     *             exception
     */
    @Test
    public void testStreamInputPacketIndexEntryConstructor8() throws CTFException {
        StructDeclaration sd = new StructDeclaration(8);
        sd.addField("timestamp_end", IntegerDeclaration.INT_32B_DECL);
        sd.addField("content_size", IntegerDeclaration.INT_32B_DECL);
        final IDeclaration declaration = IntegerDeclaration.createDeclaration(16, false, 10, ByteOrder.BIG_ENDIAN, Encoding.NONE, "", 8);
        sd.addField("device", declaration);
        BitBuffer bb = new BitBuffer(ByteBuffer.allocate(128));
        bb.getByteBuffer().putInt(-1);
        bb.getByteBuffer().putInt(0);
        bb.getByteBuffer().putShort((short) 314);
        StructDefinition sdef = sd.createDefinition(null, ILexicalScope.PACKET_HEADER, bb);
        ICTFPacketDescriptor sipie = new StreamInputPacketIndexEntry(0, sdef, 128, 0);
        assertEquals(0, sipie.getTimestampBegin());
        assertEquals(Long.MAX_VALUE, sipie.getTimestampEnd());
        assertEquals("314", sipie.getTarget());
        assertEquals(314, sipie.getTargetId());
    }

    /**
     * Run the String toString() method test.
     *
     * @throws CTFException
     *             won't happen
     */
    @Test
    public void testToString() throws CTFException {

        String expectedResult = "StreamInputPacketIndexEntry [offsetBits=0, timestampBegin=0, timestampEnd=0]";
        StructDeclaration sd = new StructDeclaration(8);
        sd.addField("timestamp_begin", IntegerDeclaration.INT_32B_DECL);
        sd.addField("timestamp_end", IntegerDeclaration.INT_32B_DECL);
        BitBuffer bb = new BitBuffer(ByteBuffer.allocate(128));

        StructDefinition sdef = sd.createDefinition(null, ILexicalScope.PACKET_HEADER, bb);
        assertEquals(expectedResult, new StreamInputPacketIndexEntry(0, sdef, 10000, 0).toString());
    }
}