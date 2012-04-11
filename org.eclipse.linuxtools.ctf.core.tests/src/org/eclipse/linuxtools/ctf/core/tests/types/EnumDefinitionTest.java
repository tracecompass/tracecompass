package org.eclipse.linuxtools.ctf.core.tests.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.internal.ctf.core.event.io.BitBuffer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>EnumDefinitionTest</code> contains tests for the class
 * <code>{@link EnumDefinition}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class EnumDefinitionTest {

    private EnumDefinition fixture;

    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(EnumDefinitionTest.class);
    }

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        EnumDeclaration declaration = new EnumDeclaration(
                new IntegerDeclaration(1, true, 1, ByteOrder.BIG_ENDIAN,
                        Encoding.ASCII, null));
        String fieldName = ""; //$NON-NLS-1$

        fixture = new EnumDefinition(declaration, null, fieldName);
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the EnumDefinition(EnumDeclaration,DefinitionScope,String)
     * constructor test.
     */
    @Test
    public void testEnumDefinition() {
        assertNotNull(fixture);
    }

    /**
     * Run the String getValue() method test.
     */
    @Test
    public void testGetValue() {
        String result = fixture.getValue();

        assertNotNull(result);
    }

    /**
     * Run the long getIntegerValue() method test.
     */
    @Test
    public void testGetIntegerValue_one() {
        fixture.setIntegerValue(1L);
        long result = fixture.getIntegerValue();

        assertEquals(1L, result);
    }

    /**
     * Run the String getValue() method test.
     */
    @Test
    public void testGetIntegerValue_zero() {
        fixture.setIntegerValue(0);
        long result = fixture.getIntegerValue();

        assertTrue(0 == result);
    }

    /**
     * Run the void read(BitBuffer) method test.
     */
    @Test
    public void testRead() {
        fixture.setIntegerValue(1L);
        BitBuffer input = new BitBuffer(ByteBuffer.allocateDirect(128));

        fixture.read(input);
    }
}