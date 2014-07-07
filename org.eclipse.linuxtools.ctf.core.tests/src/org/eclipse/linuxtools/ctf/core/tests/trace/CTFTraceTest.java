/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *     Marc-Andre Laperle - Test in traces directory recursively
 *     Simon Delisle - Add test for getCallsite(eventName, ip)
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.nio.ByteOrder;
import java.util.UUID;

import org.eclipse.linuxtools.ctf.core.event.CTFClock;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.tests.shared.CtfTestTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFStream;
import org.eclipse.linuxtools.internal.ctf.core.event.metadata.exceptions.ParseException;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>CTFTraceTest</code> contains tests for the class
 * <code>{@link CTFTrace}</code>.
 *
 * @author ematkho
 */
public class CTFTraceTest {

    private static final CtfTestTrace testTrace = CtfTestTrace.KERNEL;

    private CTFTrace fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        assumeTrue(testTrace.exists());
        try {
            fixture = testTrace.getTraceFromFile();
        } catch (CTFReaderException e) {
            /* If the assumeTrue() call passed, this should not happen. */
            fail();
        }
        fixture.setMinor(1L);
        fixture.setUUID(UUID.randomUUID());
        fixture.setPacketHeader(new StructDeclaration(1L));
        fixture.setMajor(1L);
        fixture.setByteOrder(ByteOrder.BIG_ENDIAN);
    }

    /**
     * Run the CTFTrace(File) constructor test with a known existing trace.
     */
    @Test
    public void testOpen_existing() {
        try (CTFTrace result = testTrace.getTraceFromFile();) {
            assertNotNull(result.getUUID());
        } catch (CTFReaderException e) {
            fail();
        }
    }

    /**
     * Run the CTFTrace(File) constructor test with an invalid path.
     *
     * @throws CTFReaderException
     *             is expected
     */
    @Test(expected = org.eclipse.linuxtools.ctf.core.trace.CTFReaderException.class)
    public void testOpen_invalid() throws CTFReaderException {
        File path = new File("");
        try (CTFTrace result = new CTFTrace(path);) {
            assertNotNull(result);
        }
    }

    /**
     * Run the boolean UUIDIsSet() method test.
     */
    @Test
    public void testUUIDIsSet() {
        boolean result = fixture.uuidIsSet();
        assertTrue(result);
    }

    /**
     * Run the void addStream(Stream) method test.
     */
    @Test
    public void testAddStream() {
        // test number of streams
        int nbStreams = fixture.nbStreams();
        assertEquals(1, nbStreams);

        // Add a stream
        try {
            CTFStream stream = new CTFStream(testTrace.getTrace());
            stream.setId(1234);
            fixture.addStream(stream);
        } catch (CTFReaderException e) {
            fail();
        } catch (ParseException e) {
            fail();
        }

        // test number of streams
        nbStreams = fixture.nbStreams();
        assertEquals(2, nbStreams);
    }

    /**
     * Run the boolean byteOrderIsSet() method test.
     */
    @Test
    public void testByteOrderIsSet() {
        boolean result = fixture.byteOrderIsSet();
        assertTrue(result);
    }

    /**
     * Run the ByteOrder getByteOrder() method test.
     */
    @Test
    public void testGetByteOrder_1() {
        ByteOrder result = fixture.getByteOrder();
        assertNotNull(result);
    }

    /**
     * Run the long getMajor() method test.
     */
    @Test
    public void testGetMajor() {
        long result = fixture.getMajor();
        assertEquals(1L, result);
    }

    /**
     * Run the long getMinor() method test.
     */
    @Test
    public void testGetMinor() {
        long result = fixture.getMinor();
        assertEquals(1L, result);
    }

    /**
     * Run the StructDeclaration getPacketHeader() method test.
     */
    @Test
    public void testGetPacketHeader() {
        StructDeclaration result = fixture.getPacketHeader();
        assertNotNull(result);
    }

    /**
     * Run the String getPath() method test.
     */
    @Test
    public void testGetPath() {
        String result = fixture.getPath();
        assertNotNull(result);
    }

    /**
     * Run the Stream getStream(Long) method test.
     */
    @Test
    public void testGetStream() {
        Long id = new Long(0L);
        CTFStream result = fixture.getStream(id);
        assertNotNull(result);
    }

    /**
     * Run the File getTraceDirectory() method test.
     */
    @Test
    public void testGetTraceDirectory() {
        File result = fixture.getTraceDirectory();
        assertNotNull(result);
    }

    /**
     * Run the UUID getUUID() method test.
     */
    @Test
    public void testGetUUID() {
        UUID result = fixture.getUUID();
        assertNotNull(result);
    }

    /**
     * Run the Definition lookupDefinition(String) method test.
     */
    @Test
    public void testLookupDefinition() {
        String lookupPath = "trace.packet.header";
        IDefinition result = fixture.lookupDefinition(lookupPath);
        assertNotNull(result);
    }

    /**
     * Run the boolean majorIsSet() method test.
     */
    @Test
    public void testMajorIsSet() {
        boolean result = fixture.majorIsSet();
        assertTrue(result);
    }

    /**
     * Run the boolean minorIsSet() method test.
     */
    @Test
    public void testMinorIsSet() {
        boolean result = fixture.minorIsSet();
        assertTrue(result);
    }

    /**
     * Run the boolean packetHeaderIsSet() method test with a valid header set.
     */
    @Test
    public void testPacketHeaderIsSet_valid() {
        boolean result = fixture.packetHeaderIsSet();
        assertTrue(result);
    }

    /**
     * Run the boolean packetHeaderIsSet() method test, without having a valid
     * header set.
     */
    @Test
    public void testPacketHeaderIsSet_invalid() {
        try (CTFTrace fixture2 = testTrace.getTraceFromFile();){
            fixture2.setMinor(1L);
            fixture2.setUUID(UUID.randomUUID());
            fixture2.setPacketHeader((StructDeclaration) null); /* it's null here! */
            fixture2.setMajor(1L);
            fixture2.setByteOrder(ByteOrder.BIG_ENDIAN);

            boolean result = fixture2.packetHeaderIsSet();
            assertFalse(result);
        } catch (CTFReaderException e) {
            fail();
        }
    }

    /**
     * Run the void setByteOrder(ByteOrder) method test.
     */
    @Test
    public void testSetByteOrder() {
        ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
        fixture.setByteOrder(byteOrder);
    }

    /**
     * Run the void setMajor(long) method test.
     */
    @Test
    public void testSetMajor() {
        long major = 1L;
        fixture.setMajor(major);
    }

    /**
     * Run the void setMinor(long) method test.
     */
    @Test
    public void testSetMinor() {
        long minor = 1L;
        fixture.setMinor(minor);
    }

    /**
     * Run the void setPacketHeader(StructDeclaration) method test.
     */
    @Test
    public void testSetPacketHeader() {
        StructDeclaration packetHeader = new StructDeclaration(1L);
        fixture.setPacketHeader(packetHeader);
    }

    /**
     * Run the void setUUID(UUID) method test.
     */
    @Test
    public void testSetUUID() {
        UUID uuid = UUID.randomUUID();
        fixture.setUUID(uuid);
    }

    /**
     * Run the CTFClock getClock/setClock method test.
     */
    @Test
    public void testGetSetClock_1() {
        String name = "clockyClock";
        fixture.addClock(name, new CTFClock());
        CTFClock result = fixture.getClock(name);

        assertNotNull(result);
    }

    /**
     * Run the CTFClock getClock/setClock method test.
     */
    @Test
    public void testGetSetClock_2() {
        String name = "";
        CTFClock ctfClock = new CTFClock();
        ctfClock.addAttribute("name", "Bob");
        ctfClock.addAttribute("pi", new Double(java.lang.Math.PI));
        fixture.addClock(name, ctfClock);
        CTFClock result = fixture.getClock(name);

        assertNotNull(result);
        assertTrue((Double) ctfClock.getProperty("pi") > 3.0);
        assertTrue(ctfClock.getName().equals("Bob"));
    }

    /**
     * Run the String lookupEnvironment(String) method test.
     */
    @Test
    public void testLookupEnvironment_1() {
        String key = "";
        String result = fixture.getEnvironment().get(key);
        assertNull(result);
    }

    /**
     * Run the String lookupEnvironment(String) method test.
     */
    @Test
    public void testLookupEnvironment_2() {
        String key = "otherTest";
        String result = fixture.getEnvironment().get(key);
        assertNull(result);
    }

    /**
     * Run the String lookupEnvironment(String) method test.
     */
    @Test
    public void testLookupEnvironment_3() {
        String key = "test";
        fixture.addEnvironmentVar(key, key);
        String result = fixture.getEnvironment().get(key);
        assertTrue(result.equals(key));
    }

    /**
     * Run the String lookupEnvironment(String) method test.
     */
    @Test
    public void testLookupEnvironment_4() {
        String key = "test";
        fixture.addEnvironmentVar(key, "bozo");
        fixture.addEnvironmentVar(key, "the clown");
        String result = fixture.getEnvironment().get(key);
        assertNotNull(result);
    }

    /**
     * Test for getCallsite(eventName, ip)
     * @throws CTFReaderException not expected
     */
    @Test
    public void callsitePosition() throws CTFReaderException {
        long ip1 = 2;
        long ip2 = 5;
        long ip3 = 7;
        try (CTFTrace callsiteTest = testTrace.getTraceFromFile()) {
            callsiteTest.addCallsite("testEvent", null, ip1, null, 23);
            callsiteTest.addCallsite("testEvent", null, ip2, null, 50);
            callsiteTest.addCallsite("testEvent", null, ip3, null, 15);

            assertEquals(2, (callsiteTest.getCallsite("testEvent", 1)).getIp());
            assertEquals(2, (callsiteTest.getCallsite("testEvent", 2)).getIp());
            assertEquals(5, (callsiteTest.getCallsite("testEvent", 3)).getIp());
            assertEquals(5, (callsiteTest.getCallsite("testEvent", 5)).getIp());
            assertEquals(7, (callsiteTest.getCallsite("testEvent", 6)).getIp());
            assertEquals(7, (callsiteTest.getCallsite("testEvent", 7)).getIp());
            assertEquals(7, (callsiteTest.getCallsite("testEvent", 8)).getIp());
        }
    }

}
