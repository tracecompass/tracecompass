/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.trace;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.UUID;

import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests growing streams
 *
 * @author Matthew Khouzam
 *
 */
public class CTFTraceGrowingStreamTest {

    private Path fCtfDirectory;
    private File fGrowingStream;
    private byte[][] fPackets;
    private CTFTrace fFixture;
    private UUID fUUID;

    /**
     * Run before every test
     *
     * @throws IOException won't happen
     * @throws CTFReaderException won't happen
     */
    @Before
    public void init() throws IOException, CTFReaderException {
        fCtfDirectory = Files.createTempDirectory("temptrace", new FileAttribute<?>[] {});
        File metadata = new File(fCtfDirectory.toString() + "/" + "metadata");
        fGrowingStream = new File(fCtfDirectory.toString() + "/" + "stream");
        fUUID = UUID.randomUUID();
        fPackets = new byte[2][];
        fPackets[0] = new byte[32];
        fPackets[1] = new byte[32];
        try (PrintWriter pw = new PrintWriter(metadata)) {
            pw.println("/*CTF 1.8*/");
            pw.println("typealias integer { size = 8; align = 8; signed = false; base = 10; } := uint8_t;");
            pw.println("typealias integer { size = 32; align = 32; signed = false; base = hex; } := uint32_t;");

            pw.println("trace {");
            pw.println(" major = 0;");
            pw.println(" minor = 1;");
            pw.println(" uuid = \"" + fUUID.toString() + "\";");
            pw.println(" byte_order = le;");
            pw.println(" packet.header := struct {");
            pw.println("  uint32_t magic;");
            pw.println("  uint8_t uuid[16];");
            pw.println(" };");
            pw.println("};");
            pw.println("");
            pw.println("stream {");
            pw.println(" packet.context := struct {");
            pw.println("  uint32_t packet_size;");
            pw.println("  uint32_t content_size;");
            pw.println(" };");
            pw.println("};");
            pw.println("");
            pw.println("event {");
            pw.println(" name = thing;");
            pw.println(" fields := struct { uint32_t f; };");
            pw.println("};");
            pw.println("");
            pw.close();
        }
        setupPacket(fPackets[0], 41);
        setupPacket(fPackets[1], 0xbab4face);

        try (FileOutputStream fos = new FileOutputStream(fGrowingStream)) {
            fos.write(fPackets[0]);
        }
        fFixture = new CTFTrace(fCtfDirectory.toString());
    }

    private void setupPacket(byte data[], int value) {
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.clear();
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(0xc1fc1fc1);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putLong(fUUID.getMostSignificantBits());
        bb.putLong(fUUID.getLeastSignificantBits());
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(256);
        bb.putInt(256);
        bb.putInt(value);
    }

    /**
     * Test a growing stream
     *
     * @throws CTFReaderException won't happen
     * @throws IOException won't happen
     * @throws FileNotFoundException won't happen
     */
    @Test
    public void testGrowingLive() throws CTFReaderException, FileNotFoundException, IOException {
        try (CTFTraceReader reader = new CTFTraceReader(fFixture);) {
            reader.setLive(true);
            assertEquals("0x29", reader.getCurrentEventDef().getFields().getDefinitions().get("f").toString());
            reader.advance();
            try (FileOutputStream fos = new FileOutputStream(fGrowingStream, true)) {
                fos.write(fPackets[1]);
            }
            reader.advance();
            assertNotNull(reader.getCurrentEventDef());
            assertEquals("0xbab4face", reader.getCurrentEventDef().getFields().getDefinitions().get("f").toString());
        }
    }

    /**
     * Test a growing stream
     *
     * @throws CTFReaderException won't happen
     * @throws IOException won't happen
     * @throws FileNotFoundException won't happen
     */
    @Test
    public void testGrowingNotLive() throws CTFReaderException, FileNotFoundException, IOException {
        try (CTFTraceReader reader = new CTFTraceReader(fFixture);) {
            reader.setLive(false);
            assertEquals("0x29", reader.getCurrentEventDef().getFields().getDefinitions().get("f").toString());
            reader.advance();
            try (FileOutputStream fos = new FileOutputStream(fGrowingStream, true)) {
                fos.write(fPackets[1]);
            }
            reader.advance();
            assertNull(reader.getCurrentEventDef());
        }
    }
}
