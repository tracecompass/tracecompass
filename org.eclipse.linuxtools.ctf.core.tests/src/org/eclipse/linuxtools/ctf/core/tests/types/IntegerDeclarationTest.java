package org.eclipse.linuxtools.ctf.core.tests.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>IntegerDeclarationTest</code> contains tests for the class
 * <code>{@link IntegerDeclaration}</code>.
 * 
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class IntegerDeclarationTest {

    private IntegerDeclaration fixture;

    /**
     * Launch the test.
     * 
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(IntegerDeclarationTest.class);
    }

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new IntegerDeclaration(1, true, 1, ByteOrder.BIG_ENDIAN,
                Encoding.ASCII);
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the IntegerDeclaration(int,boolean,int,ByteOrder,Encoding)
     * constructor test.
     */
    @Test
    public void testIntegerDeclaration() {
        int len = 1;
        boolean signed = true;
        int base = 1;
        ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
        Encoding encoding = Encoding.ASCII;

        IntegerDeclaration result = new IntegerDeclaration(len, signed, base,
                byteOrder, encoding);

        assertNotNull(result);
        assertEquals(1, result.getBase());
        assertEquals(false, result.isCharacter());
        String outputValue = "[declaration] integer["; //$NON-NLS-1$
        assertEquals(outputValue,
                result.toString().substring(0, outputValue.length()));
        assertEquals(1, result.getLength());
        assertEquals(true, result.isSigned());
    }

    /**
     * Run the int getBase() method test.
     */
    @Test
    public void testGetBase() {
        int result = fixture.getBase();
        assertEquals(1, result);
    }

    /**
     * Run the ByteOrder getByteOrder() method test.
     */
    @Test
    public void testGetByteOrder() {
        ByteOrder result = fixture.getByteOrder();
        assertNotNull(result);
        assertEquals("BIG_ENDIAN", result.toString()); //$NON-NLS-1$
    }

    /**
     * Run the Encoding getEncoding() method test.
     */
    @Test
    public void testGetEncoding() {
        Encoding result = fixture.getEncoding();
        assertNotNull(result);
        assertEquals("ASCII", result.name()); //$NON-NLS-1$
        assertEquals("ASCII", result.toString()); //$NON-NLS-1$
        assertEquals(1, result.ordinal());
    }

    /**
     * Run the int getLength() method test.
     */
    @Test
    public void testGetLength() {
        int result = fixture.getLength();
        assertEquals(1, result);
    }

    /**
     * Run the boolean isCharacter() method test.
     */
    @Test
    public void testIsCharacter() {
        boolean result = fixture.isCharacter();
        assertEquals(false, result);
    }

    /**
     * Run the boolean isCharacter() method test.
     */
    @Test
    public void testIsCharacter_8bytes() {
        IntegerDeclaration fixture8 = new IntegerDeclaration(8, true, 1,
                ByteOrder.BIG_ENDIAN, Encoding.ASCII);

        boolean result = fixture8.isCharacter();
        assertEquals(true, result);
    }

    /**
     * Run the boolean isSigned() method test.
     */
    @Test
    public void testIsSigned_signed() {
        boolean result = fixture.isSigned();
        assertEquals(true, result);
    }

    /**
     * Run the boolean isSigned() method test.
     */
    @Test
    public void testIsSigned_unsigned() {
        IntegerDeclaration fixture_unsigned = new IntegerDeclaration(1, false,
                1, ByteOrder.BIG_ENDIAN, Encoding.ASCII);

        boolean result = fixture_unsigned.isSigned();
        assertEquals(false, result);
    }

    /**
     * Run the void setBase(int) method test.
     */
    @Test
    public void testSetBase() {
        int base = 1;
        fixture.setBase(base);
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
     * Run the void setEncoding(Encoding) method test.
     */
    @Test
    public void testSetEncoding() {
        Encoding encoding = Encoding.ASCII;
        fixture.setEncoding(encoding);
    }

    /**
     * Run the void setLength(int) method test.
     */
    @Test
    public void testSetLength() {
        int length = 1;
        fixture.setLength(length);
    }

    /**
     * Run the void setSigned(boolean) method test.
     */
    @Test
    public void testSetSigned() {
        boolean signed = true;
        fixture.setSigned(signed);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String result = fixture.toString();
        String trunc = result.substring(0, 22);
        assertEquals("[declaration] integer[", trunc); //$NON-NLS-1$
    }
}