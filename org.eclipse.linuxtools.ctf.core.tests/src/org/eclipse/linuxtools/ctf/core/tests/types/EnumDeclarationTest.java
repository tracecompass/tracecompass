package org.eclipse.linuxtools.ctf.core.tests.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>EnumDeclarationTest</code> contains tests for the class
 * <code>{@link EnumDeclaration}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class EnumDeclarationTest {

    private EnumDeclaration fixture;

    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(EnumDeclarationTest.class);
    }

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new EnumDeclaration(new IntegerDeclaration(1, true, 1,
                ByteOrder.BIG_ENDIAN, Encoding.ASCII, null));
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the EnumDeclaration(IntegerDeclaration) constructor test.
     */
    @Test
    public void testEnumDeclaration() {
        IntegerDeclaration containerType = new IntegerDeclaration(1, true, 1,
                ByteOrder.BIG_ENDIAN, Encoding.ASCII, null);

        EnumDeclaration result = new EnumDeclaration(containerType);

        assertNotNull(result);
        String left = "[declaration] enum["; //$NON-NLS-1$
        assertEquals(left, result.toString().substring(0, left.length()));
    }

    /**
     * Run the boolean add(long,long,String) method test.
     */
    @Test
    public void testAdd() {
        long low = 1L;
        long high = 1L;
        String label = ""; //$NON-NLS-1$

        boolean result = fixture.add(low, high, label);

        assertTrue(result);
    }

    /**
     * Run the EnumDefinition createDefinition(DefinitionScope,String) method
     * test.
     */
    @Test
    public void testCreateDefinition() {
        IDefinitionScope definitionScope = null;
        String fieldName = ""; //$NON-NLS-1$

        EnumDefinition result = fixture.createDefinition(definitionScope,
                fieldName);

        assertNotNull(result);
    }

    /**
     * Run the String getLabel(long) method test.
     */
    @Test
    public void testGetLabel() {
        long i = 0;
        String result = fixture.getLabel(i);

        assertNull(result);
    }

    /**
     * Run the String query(long) method test.
     */
    @Test
    public void testQuery() {
        long value = 0;
        String result = fixture.query(value);

        assertNull(result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String result = fixture.toString();

        String left = "[declaration] enum["; //$NON-NLS-1$
        assertEquals(left, result.substring(0, left.length()));
    }
}
