package org.eclipse.linuxtools.ctf.core.tests.types;

import static org.junit.Assert.assertNotNull;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.StringDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StringDefinition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StringDefinitionTest</code> contains tests for the class
 * <code>{@link StringDefinition}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class StringDefinitionTest {

    private StringDefinition fixture;

    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(StringDefinitionTest.class);
    }

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        String name = "testString"; //$NON-NLS-1$
        StringDeclaration stringDec = new StringDeclaration();
        fixture = stringDec.createDefinition(null, name);
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the StringDefinition(StringDeclaration,DefinitionScope,String)
     * constructor test.
     */
    @Test
    public void testStringDefinition() {
        StringDeclaration declaration = new StringDeclaration();
        IDefinitionScope definitionScope = null;
        String fieldName = ""; //$NON-NLS-1$

        StringDefinition result = new StringDefinition(declaration,
                definitionScope, fieldName);

        assertNotNull(result);
    }

    /**
     * Run the StringDeclaration getDeclaration() method test.
     */
    @Test
    public void testGetDeclaration() {
        fixture.setString(new StringBuilder());
        StringDeclaration result = fixture.getDeclaration();
        assertNotNull(result);
    }

    /**
     * Run the StringBuilder getString() method test.
     */
    @Test
    public void testGetString() {
        fixture.setString(new StringBuilder());
        StringBuilder result = fixture.getString();
        assertNotNull(result);
    }

    /**
     * Run the String getValue() method test.
     */
    @Test
    public void testGetValue() {
        fixture.setString(new StringBuilder());
        String result = fixture.getValue();
        assertNotNull(result);
    }

    /**
     * Run the void read(BitBuffer) method test.
     */
    @Test
    public void testRead() {
        fixture.setString(new StringBuilder());
        BitBuffer input = new BitBuffer(java.nio.ByteBuffer.allocateDirect(128));
        fixture.read(input);
    }

    /**
     * Run the void setDeclaration(StringDeclaration) method test.
     */
    @Test
    public void testSetDeclaration() {
        fixture.setString(new StringBuilder());
        StringDeclaration declaration = new StringDeclaration();
        fixture.setDeclaration(declaration);
    }

    /**
     * Run the void setString(StringBuilder) method test.
     */
    @Test
    public void testSetString() {
        fixture.setString(new StringBuilder());
        StringBuilder string = new StringBuilder();
        fixture.setString(string);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        fixture.setString(new StringBuilder());
        String result = fixture.toString();
        assertNotNull(result);
    }
}