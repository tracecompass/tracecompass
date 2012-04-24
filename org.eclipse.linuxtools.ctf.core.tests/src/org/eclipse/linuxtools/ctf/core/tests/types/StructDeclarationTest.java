package org.eclipse.linuxtools.ctf.core.tests.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;

import org.eclipse.linuxtools.ctf.core.event.types.IDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StringDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StructDeclarationTest</code> contains tests for the class
 * <code>{@link StructDeclaration}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class StructDeclarationTest {

    private StructDeclaration fixture;

    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(StructDeclarationTest.class);
    }

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new StructDeclaration(1L);
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the StructDeclaration(long) constructor test.
     */
    @Test
    public void testStructDeclaration() {
        assertNotNull(fixture);
        assertEquals(1L, fixture.getMaxAlign());

        String regex = "^\\[declaration\\] struct\\[[0-9a-f]{1,8}\\]$"; //$NON-NLS-1$
        assertTrue(fixture.toString().matches(regex));
    }

    /**
     * Run the void addField(String,Declaration) method test.
     */
    @Test
    public void testAddField() {
        String name = ""; //$NON-NLS-1$
        IDeclaration declaration = new StringDeclaration();
        fixture.addField(name, declaration);
    }

    /**
     * Run the StructDefinition createDefinition(DefinitionScope,String) method
     * test.
     */
    @Test
    public void testCreateDefinition() {
        String fieldName = ""; //$NON-NLS-1$
        StructDefinition result = fixture.createDefinition(null, fieldName);
        assertNotNull(result);
    }

    /**
     * Run the HashMap<String, Declaration> getFields() method test.
     */
    @Test
    public void testGetFields() {
        HashMap<String, IDeclaration> result = fixture.getFields();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    /**
     * Run the List<String> getFieldsList() method test.
     */
    @Test
    public void testGetFieldsList() {
        List<String> result = fixture.getFieldsList();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    /**
     * Run the long getMinAlign() method test.
     */
    @Test
    public void testGetMinAlign() {
        long result = fixture.getMaxAlign();
        assertEquals(1L, result);
    }

    /**
     * Run the boolean hasField(String) method test.
     */
    @Test
    public void testHasField() {
        String name = ""; //$NON-NLS-1$
        boolean result = fixture.hasField(name);

        assertEquals(false, result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String result = fixture.toString();
        String trunc = result.substring(0, 21);

        assertEquals("[declaration] struct[", trunc); //$NON-NLS-1$
    }
}
