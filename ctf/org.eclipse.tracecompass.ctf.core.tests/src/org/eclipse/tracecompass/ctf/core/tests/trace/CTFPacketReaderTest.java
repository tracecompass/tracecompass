/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.CTFStrings;
import org.eclipse.tracecompass.ctf.core.event.IEventDeclaration;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.ILexicalScope;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.ICTFPacketDescriptor;
import org.eclipse.tracecompass.internal.ctf.core.event.EventDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.event.EventDefinition;
import org.eclipse.tracecompass.internal.ctf.core.trace.CTFPacketReader;
import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputPacketIndexEntry;
import org.junit.Test;

/**
 * Unit tests for ctf packet reader: this tests creation and reading.
 *
 * @author Matthew Khouzam
 *
 */
public class CTFPacketReaderTest {

    private static final StructDeclaration EMPTY_STRUCT = new StructDeclaration(8);

    private static @NonNull BitBuffer createBitBuffer(byte[] bytes) {
        return new BitBuffer(ByteBuffer.wrap(bytes));
    }

    /**
     * Test a packet with fields and a header (a normal packet)
     *
     * @throws CTFException
     *             won't happen
     */
    @Test
    public void testPacket() throws CTFException {
        // step 1: create in memory CTF trace
        byte[] bytes = { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xa5 };
        BitBuffer input = createBitBuffer(bytes);
        // step 2, create the packet context
        ICTFPacketDescriptor packetContext = new StreamInputPacketIndexEntry(0, EMPTY_STRUCT.createDefinition(null, ILexicalScope.TRACE, new BitBuffer()), 8, 0, 0);
        // step 3 create the event header
        StructDeclaration eventHeaderDeclaration = new StructDeclaration(8);
        eventHeaderDeclaration.addField("timestamp", IntegerDeclaration.INT_8_DECL);
        // step 4 create an event declaration, and only one!
        final EventDeclaration eventDec = new EventDeclaration();
        eventDec.setName("Hello");
        StructDeclaration fields = new StructDeclaration(8);
        fields.addField("field1", IntegerDeclaration.UINT_16L_DECL);
        fields.addField("field2", IntegerDeclaration.UINT_8_DECL);
        eventDec.setFields(fields);
        eventDec.setLogLevel(4);
        List<@Nullable IEventDeclaration> declarations = Collections.singletonList(eventDec);
        // step 5: give this event a context
        CTFTrace trace = new CTFTrace();
        CTFPacketReader cpr = new CTFPacketReader(input, packetContext, declarations, eventHeaderDeclaration, null, null, trace);
        assertNotNull(cpr);
        assertTrue(cpr.hasMoreEvents());
        EventDefinition event = cpr.readNextEvent();
        assertEquals(0L, event.getTimestamp());
        assertEquals(0L, ((IntegerDefinition) event.getFields().getDefinition("field1")).getValue());
        assertEquals(0L, ((IntegerDefinition) event.getFields().getDefinition("field2")).getValue());
        assertNotNull(cpr);
        assertTrue(cpr.hasMoreEvents());
        event = cpr.readNextEvent();
        assertEquals(1L, event.getTimestamp());
        assertEquals(65535L, ((IntegerDefinition) event.getFields().getDefinition("field1")).getValue());
        assertEquals(0xa5, ((IntegerDefinition) event.getFields().getDefinition("field2")).getValue());
        assertFalse(cpr.hasMoreEvents());
    }

    /**
     * Test a packet with fields, a context and a header (a mostly normal
     * packet)
     *
     * @throws CTFException
     *             won't happen
     */
    @Test
    public void testPacketWithContext() throws CTFException {
        // step 1: create in memory CTF trace
        byte[] bytes = { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xa5 };
        BitBuffer input = createBitBuffer(bytes);
        // step 2, create the packet context
        ICTFPacketDescriptor packetContext = new StreamInputPacketIndexEntry(0, EMPTY_STRUCT.createDefinition(null, ILexicalScope.TRACE, new BitBuffer()), 8, 0, 0);
        // step 3 create the event header
        StructDeclaration eventHeaderDeclaration = new StructDeclaration(8);
        eventHeaderDeclaration.addField("timestamp", IntegerDeclaration.INT_8_DECL);
        // step 4 create an event declaration, and only one!
        final EventDeclaration eventDec = new EventDeclaration();
        eventDec.setName("Hello");
        StructDeclaration context = new StructDeclaration(8);
        context.addField("field1", IntegerDeclaration.UINT_16L_DECL);
        StructDeclaration fields = new StructDeclaration(8);
        fields.addField("field2", IntegerDeclaration.UINT_8_DECL);
        eventDec.setContext(context);
        eventDec.setFields(fields);
        eventDec.setLogLevel(5);// I guess?
        List<@Nullable IEventDeclaration> declarations = Collections.singletonList(eventDec);
        // step 5: give this event a context
        CTFTrace trace = new CTFTrace();
        CTFPacketReader cpr = new CTFPacketReader(input, packetContext, declarations, eventHeaderDeclaration, null, null, trace);
        assertNotNull(cpr);
        assertTrue(cpr.hasMoreEvents());
        EventDefinition event = cpr.readNextEvent();
        assertEquals(0L, event.getTimestamp());
        assertEquals(0L, ((IntegerDefinition) event.getContext().getDefinition("field1")).getValue());
        assertEquals(0L, ((IntegerDefinition) event.getFields().getDefinition("field2")).getValue());
        assertNotNull(cpr);
        assertTrue(cpr.hasMoreEvents());
        event = cpr.readNextEvent();
        assertEquals(1L, event.getTimestamp());
        assertEquals(65535L, ((IntegerDefinition) event.getContext().getDefinition("field1")).getValue());
        assertEquals(0xa5, ((IntegerDefinition) event.getFields().getDefinition("field2")).getValue());
        assertFalse(cpr.hasMoreEvents());
    }

    /**
     * Test a packet with fields and no header (a odd but acceptable packet)
     *
     * @throws CTFException
     *             won't happen
     */
    @Test
    public void testPacketNoHeader() throws CTFException {
        // step 1: create in memory CTF trace
        byte[] bytes = { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xa5 };
        BitBuffer input = createBitBuffer(bytes);
        // step 2, create the packet context
        ICTFPacketDescriptor packetContext = new StreamInputPacketIndexEntry(0, new StructDeclaration(8).createDefinition(null, ILexicalScope.TRACE, new BitBuffer()), 8, 0, 0);
        // step 3 create the event header
        // step 4 create an event declaration, and only one!
        final EventDeclaration eventDec = new EventDeclaration();
        eventDec.setName("Hello");
        StructDeclaration fields = new StructDeclaration(8);
        fields.addField("timestamp", IntegerDeclaration.INT_8_DECL);
        fields.addField("field1", IntegerDeclaration.UINT_16L_DECL);
        fields.addField("field2", IntegerDeclaration.UINT_8_DECL);
        eventDec.setFields(fields);
        List<@Nullable IEventDeclaration> declarations = Collections.singletonList(eventDec);
        // step 5: give this event a context
        CTFTrace trace = new CTFTrace();
        CTFPacketReader cpr = new CTFPacketReader(input, packetContext, declarations, null, null, null, trace);
        assertNotNull(cpr);
        assertTrue(cpr.hasMoreEvents());
        EventDefinition event = cpr.readNextEvent();
        assertEquals(0L, event.getTimestamp());
        assertEquals(0L, ((IntegerDefinition) event.getFields().getDefinition("field1")).getValue());
        assertEquals(0L, ((IntegerDefinition) event.getFields().getDefinition("field2")).getValue());
        assertNotNull(cpr);
        assertTrue(cpr.hasMoreEvents());
        event = cpr.readNextEvent();
        assertEquals(1L, event.getTimestamp());
        assertEquals(65535L, ((IntegerDefinition) event.getFields().getDefinition("field1")).getValue());
        assertEquals(0xa5, ((IntegerDefinition) event.getFields().getDefinition("field2")).getValue());
        assertFalse(cpr.hasMoreEvents());
    }

    /**
     * Test a packet with fields and no header (a odd but acceptable packet)
     *
     * @throws CTFException
     *             won't happen
     */
    @Test
    public void testPacketWithPacketContextAndLostEvents() throws CTFException {
        // step 1: create in memory CTF trace
        byte[] bytes = { (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x5a };
        BitBuffer input = createBitBuffer(bytes);
        // step 2, create the packet context
        final StructDeclaration packetHeader = new StructDeclaration(8);
        packetHeader.addField(CTFStrings.EVENTS_DISCARDED, IntegerDeclaration.UINT_16L_DECL);
        ICTFPacketDescriptor packetContext = new StreamInputPacketIndexEntry(0, packetHeader.createDefinition(null, ILexicalScope.TRACE, input), 8, 0, 16);
        // step 4 create an event declaration, and only one!
        final EventDeclaration eventDec = new EventDeclaration();
        eventDec.setName("Hello");
        StructDeclaration fields = new StructDeclaration(8);
        fields.addField("timestamp", IntegerDeclaration.UINT_16L_DECL);
        fields.addField("field", IntegerDeclaration.UINT_8_DECL);
        eventDec.setFields(fields);
        List<@Nullable IEventDeclaration> declarations = Collections.singletonList(eventDec);
        // step 5: give this event a context
        CTFTrace trace = new CTFTrace();
        CTFPacketReader cpr = new CTFPacketReader(input, packetContext, declarations, null, null, null, trace);
        assertNotNull(cpr);
        assertTrue(cpr.hasMoreEvents());
        EventDefinition event = cpr.readNextEvent();
        assertEquals(0L, event.getTimestamp());
        assertEquals(1L, ((IntegerDefinition) event.getFields().getDefinition("field")).getValue());
        assertNotNull(cpr);
        assertTrue(cpr.hasMoreEvents());
        event = cpr.readNextEvent();
        assertEquals(256L, event.getTimestamp());
        assertEquals(0x5a, ((IntegerDefinition) event.getFields().getDefinition("field")).getValue());
        assertTrue(cpr.hasMoreEvents());
        event = cpr.readNextEvent();
        assertEquals(256L, event.getTimestamp());
        assertEquals(event.getDeclaration().getName(), CTFStrings.LOST_EVENT_NAME);
        assertEquals(512L, ((IntegerDefinition) event.getFields().getDefinition(CTFStrings.LOST_EVENTS_FIELD)).getValue());
        assertFalse(cpr.hasMoreEvents());
    }

}
