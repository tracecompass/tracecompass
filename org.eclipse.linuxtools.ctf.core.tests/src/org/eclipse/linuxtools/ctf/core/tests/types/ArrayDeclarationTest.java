package org.eclipse.linuxtools.ctf.core.tests.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.linuxtools.ctf.core.event.types.ArrayDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.ArrayDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.IDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.StringDeclaration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>ArrayDeclarationTest</code> contains tests for the class
 * <code>{@link ArrayDeclaration}</code>.
 * 
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class ArrayDeclarationTest {

    private ArrayDeclaration fixture;

    /**
     * Launch the test.
     * 
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(ArrayDeclarationTest.class);
    }

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new ArrayDeclaration(1, new StringDeclaration());
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the ArrayDeclaration(int,Declaration) constructor test.
     */
    @Test
    public void testArrayDeclaration() {
        int length = 1;
        IDeclaration elemType = new StringDeclaration();
        ArrayDeclaration result = new ArrayDeclaration(length, elemType);

        assertNotNull(result);
        String left = "[declaration] array["; //$NON-NLS-1$
        String right = result.toString().substring(0, left.length());
        assertEquals(left, right);
        assertEquals(1, result.getLength());
    }

    /**
     * Run the ArrayDefinition createDefinition(DefinitionScope,String) method
     * test.
     */
    @Test
    public void testCreateDefinition() {
        String fieldName = ""; //$NON-NLS-1$
        IDefinitionScope definitionScope = null;
        ArrayDefinition result;
        result = fixture.createDefinition(definitionScope, fieldName);

        assertNotNull(result);
    }

    /**
     * Run the Declaration getElementType() method test.
     */
    @Test
    public void testGetElementType() {
        IDeclaration result = fixture.getElementType();
        assertNotNull(result);
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
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String result = fixture.toString();
        String left = "[declaration] array["; //$NON-NLS-1$
        String right = result.substring(0, left.length());

        assertEquals(left, right);
    }
}
