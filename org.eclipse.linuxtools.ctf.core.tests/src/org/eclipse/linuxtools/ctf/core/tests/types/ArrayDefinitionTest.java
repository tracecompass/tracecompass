package org.eclipse.linuxtools.ctf.core.tests.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.types.ArrayDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.ArrayDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StringDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StringDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.tests.TestParams;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>ArrayDefinitionTest</code> contains tests for the class
 * <code>{@link ArrayDefinition}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class ArrayDefinitionTest {

    private CTFTrace trace;
    private ArrayDefinition fixture;

    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(ArrayDefinitionTest.class);
    }

    /**
     * Perform pre-test initialization.
     *
     * structDef shouldn't be null after parsing the CTFTraceReader object, so
     * we can ignore the warning.
     * 
     * @throws CTFReaderException 
     */
    @SuppressWarnings("null")
    @Before
    public void setUp() throws CTFReaderException {
        this.trace = TestParams.createTrace();

        CTFTraceReader tr = new CTFTraceReader(this.trace);
        String name = ""; //$NON-NLS-1$
        StructDefinition structDef = null;
        boolean foundArray = false;

        while (tr.hasMoreEvents() && !foundArray) {
            tr.advance();
            EventDefinition ed = tr.getCurrentEventDef();
            for (String key : ed.fields.getDefinitions().keySet()) {
                structDef = ed.fields;
                Definition d = structDef.lookupDefinition(key);
                if (d instanceof ArrayDefinition) {
                    foundArray = true;
                    name = key;
                    break;
                }
            }
        }
        fixture = structDef.lookupArray(name);
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    private static StringDefinition[] createDefs() {
        int size = 4;
        StringDefinition[] defs = new StringDefinition[size];
        for (int i = 0; i < size; i++) {

            String content = "test" + i; //$NON-NLS-1$
            defs[i] = new StringDefinition(
                    new StringDeclaration(Encoding.UTF8), null, content);
            defs[i].setString(new StringBuilder(content));
        }
        return defs;
    }

    /**
     * Run the ArrayDefinition(ArrayDeclaration,DefinitionScope,String)
     * constructor test.
     */
    @Test
    public void testArrayDefinition_baseDeclaration() {
        ArrayDeclaration declaration = fixture.getDeclaration();
        String fieldName = ""; //$NON-NLS-1$

        ArrayDefinition result = new ArrayDefinition(declaration, this.trace,
                fieldName);

        assertNotNull(result);
    }

    /**
     * Run the ArrayDefinition(ArrayDeclaration,DefinitionScope,String)
     * constructor test.
     */
    @Test
    public void testArrayDefinition_newDeclaration() {
        ArrayDeclaration declaration = new ArrayDeclaration(0,
                new StringDeclaration());
        IDefinitionScope definitionScope = null;
        String fieldName = ""; //$NON-NLS-1$

        ArrayDefinition result = new ArrayDefinition(declaration,
                definitionScope, fieldName);

        assertNotNull(result);
    }

    /**
     * Run the ArrayDeclaration getDeclaration() method test.
     */
    @Test
    public void testGetDeclaration() {
        fixture.setDefinitions(new Definition[] {});
        ArrayDeclaration result = fixture.getDeclaration();

        assertNotNull(result);
    }

    /**
     * Run the Definition getElem(int) method test.
     */
    @Test
    public void testGetElem_noDefs() {
        int i = 0;
        Definition result = fixture.getElem(i);

        assertNotNull(result);
    }

    /**
     * Run the Definition getElem(int) method test.
     */
    @Test
    public void testGetElem_withDefs() {
        Definition defs[] = createDefs();
        fixture.setDefinitions(defs);
        int j = 1;

        Definition result = fixture.getElem(j);

        assertNotNull(result);
    }

    /**
     * Run the boolean isString() method test.
     */
    @Test
    public void testIsString_ownDefs() {
        StringDefinition[] defs = createDefs();
        fixture.setDefinitions(defs);

        boolean result = fixture.isString();

        assertFalse(result);
    }

    /**
     * Run the boolean isString() method test.
     */
    @Test
    public void testIsString_complex() {
        final IntegerDeclaration id = new IntegerDeclaration(8, false, 16,
                ByteOrder.LITTLE_ENDIAN, Encoding.UTF8, null);
        ArrayDeclaration ad = new ArrayDeclaration(0, id);
        ArrayDefinition ownFixture = new ArrayDefinition(ad, this.trace,
                "Testx"); //$NON-NLS-1$

        int size = 4;
        IntegerDefinition[] defs = new IntegerDefinition[size];
        for (int i = 0; i < size; i++) {

            String content = "test" + i; //$NON-NLS-1$
            defs[i] = new IntegerDefinition(new IntegerDeclaration(8, false,
                    16, ByteOrder.LITTLE_ENDIAN, Encoding.UTF8, content), null, content);
            defs[i].setValue(i);
        }

        ownFixture.setDefinitions(defs);
        boolean result = ownFixture.isString();

        assertTrue(result);
    }

    /**
     * Run the boolean isString() method test.
     */
    @Test
    public void testIsString_emptyDef() {
        fixture.setDefinitions(new Definition[] {});
        boolean result = fixture.isString();

        assertFalse(result);
    }

    /**
     * Run the void read(BitBuffer) method test.
     */
    @Test
    public void testRead_noDefs() {
        BitBuffer input = new BitBuffer(ByteBuffer.allocateDirect(128));

        fixture.read(input);
    }

    /**
     * Run the void read(BitBuffer) method test.
     */
    @Test
    public void testRead_withDefs() {
        fixture.setDefinitions(new Definition[] {});
        BitBuffer input = new BitBuffer(java.nio.ByteBuffer.allocateDirect(128));

        fixture.read(input);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_base() {
        String result = fixture.toString();

        assertNotNull(result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_withDefs() {
        int size = 2;
        StringDefinition[] defs = new StringDefinition[size];
        for (int i = 0; i < size; i++) {
            defs[i] = new StringDefinition(null, null, ("test" + i)); //$NON-NLS-1$
        }
        fixture.setDefinitions(defs);
        String result = fixture.toString();

        assertNotNull(result);
    }
}
