package org.eclipse.linuxtools.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.UUID;

import org.eclipse.linuxtools.ctf.core.event.metadata.exceptions.ParseException;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.tests.TestParams;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>CTFTraceTest</code> contains tests for the class
 * <code>{@link CTFTrace}</code>.
 * 
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class CTFTraceTest {

    private CTFTrace fixture;

    /**
     * Launch the test.
     * 
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(CTFTraceTest.class);
    }

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = TestParams.createTraceFromFile();
        fixture.setMinor(1L);
        fixture.setUUID(UUID.randomUUID());
        fixture.setPacketHeader(new StructDeclaration(1L));
        fixture.setMajor(1L);
        fixture.setByteOrder(ByteOrder.BIG_ENDIAN);
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the CTFTrace(File) constructor test with a known existing trace.
     */
    @Test
    public void testOpen_existing() {
        CTFTrace result = TestParams.createTraceFromFile();
        assertNotNull(result.getUUID());
    }

    /**
     * Run the CTFTrace(File) constructor test with an invalid path.
     * 
     * @throws CTFReaderException
     */
    @Test(expected = org.eclipse.linuxtools.ctf.core.trace.CTFReaderException.class)
    public void testOpen_invalid() throws CTFReaderException {
        File path = new File(""); //$NON-NLS-1$
        CTFTrace result = new CTFTrace(path);
        assertNotNull(result);
    }

    /**
     * Run the boolean UUIDIsSet() method test.
     */
    @Test
    public void testUUIDIsSet() {
        boolean result = fixture.UUIDIsSet();
        assertTrue(result);
    }

    /**
     * Run the void addStream(Stream) method test.
     * 
     * @throws ParseException
     * @throws CTFReaderException 
     */
    @Test
    public void testAddStream() throws ParseException, CTFReaderException {
        Stream stream = new Stream(TestParams.createTrace());
        stream.setId(1L);
        fixture.addStream(stream);
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
        Stream result = fixture.getStream(id);
        assertNotNull(result);
    }

    /**
     * Run the Map<Long, Stream> getStreams() method test.
     */
    @Test
    public void testGetStreams() {
        Map<Long, Stream> result = fixture.getStreams();
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
        String lookupPath = "trace.packet.header"; //$NON-NLS-1$
        Definition result = fixture.lookupDefinition(lookupPath);
        assertNotNull(result);
    }

    /**
     * Run the boolean majortIsSet() method test.
     */
    @Test
    public void testMajortIsSet() {
        boolean result = fixture.majortIsSet();
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
     * Run the int nbStreams() method test.
     */
    @Test
    public void testNbStreams() {
        int result = fixture.nbStreams();
        assertEquals(2, result);
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
        CTFTrace fixture2 = TestParams.createTraceFromFile();
        fixture2.setMinor(1L);
        fixture2.setUUID(UUID.randomUUID());
        fixture2.setPacketHeader((StructDeclaration) null); /* it's null here! */
        fixture2.setMajor(1L);
        fixture2.setByteOrder(ByteOrder.BIG_ENDIAN);

        boolean result = fixture2.packetHeaderIsSet();
        assertFalse(result);
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
}
