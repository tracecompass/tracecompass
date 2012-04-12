package org.eclipse.linuxtools.ctf.core.tests.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.IDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.SequenceDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.SequenceDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StringDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>SequenceDeclarationTest</code> contains tests for the class
 * <code>{@link SequenceDeclaration}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class SequenceDeclarationTest {

    private SequenceDeclaration fixture;

    static final String fieldName = "LengthName"; //$NON-NLS-1$
    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(SequenceDeclarationTest.class);
    }

    @Before
    public void setUp() {
        fixture = new SequenceDeclaration(fieldName, new StringDeclaration());
    }

    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the SequenceDeclaration(String,Declaration) constructor test.
     */
    @Test
    public void testSequenceDeclaration() {
        String lengthName = ""; //$NON-NLS-1$
        IDeclaration elemType = new StringDeclaration();

        SequenceDeclaration result = new SequenceDeclaration(lengthName,
                elemType);
        assertNotNull(result);
        String string = "[declaration] sequence["; //$NON-NLS-1$
        assertEquals(string, result.toString().substring(0, string.length()));
    }

    /**
     * Run the SequenceDefinition createDefinition(DefinitionScope,String)
     * method test.
     */
    @Test
    public void testCreateDefinition() {
        IntegerDeclaration id = new IntegerDeclaration(8, false, 8,
                ByteOrder.LITTLE_ENDIAN, Encoding.UTF8, null);

        StructDeclaration structDec = new StructDeclaration(0);
        structDec.addField(fieldName, id);
        StructDefinition structDef = new StructDefinition(structDec, null, "x"); //$NON-NLS-1$
        long seqLen = 10;
        structDef.lookupInteger(fieldName).setValue(seqLen);
        SequenceDefinition result = this.fixture.createDefinition(structDef,
                fieldName);
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
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String result = fixture.toString();
        String left = "[declaration] sequence["; //$NON-NLS-1$
        assertEquals(left, result.substring(0, left.length()));
    }
}
