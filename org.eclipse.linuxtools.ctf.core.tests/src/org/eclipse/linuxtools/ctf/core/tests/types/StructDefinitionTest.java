package org.eclipse.linuxtools.ctf.core.tests.types;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.nio.ByteBuffer;
import java.util.HashMap;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.ArrayDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.SequenceDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StringDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.VariantDefinition;
import org.eclipse.linuxtools.ctf.core.tests.TestParams;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader;
import org.eclipse.linuxtools.internal.ctf.core.event.io.BitBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StructDefinitionTest</code> contains tests for the class
 * <code>{@link StructDefinition}</code>.
 * 
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class StructDefinitionTest {

    private StructDefinition fixture;

    private static final String VAR_FIELD_NAME = "SomeVariant"; //$NON-NLS-1$

    /**
     * Launch the test.
     * 
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(StructDefinitionTest.class);
    }

    /**
     * Perform pre-test initialization.
     * 
     * @throws CTFReaderException 
     */
    @Before
    public void setUp() throws CTFReaderException {
        CTFTrace c = TestParams.createTrace();
        CTFTraceReader tr = new CTFTraceReader(c);
        EventDefinition ed = tr.getCurrentEventDef();
        fixture = ed.fields;
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the StructDeclaration getDeclaration() method test.
     */
    @Test
    public void testGetDeclaration() {
        StructDeclaration result = fixture.getDeclaration();
        assertNotNull(result);
    }

    /**
     * Run the HashMap<String, Definition> getDefinitions() method test.
     */
    @Test
    public void testGetDefinitions_1() {
        HashMap<String, Definition> result = fixture.getDefinitions();
        assertNotNull(result);
    }

    /**
     * Run the ArrayDefinition lookupArray(String) method test.
     */
    @Test
    public void testLookupArray() {
        String name = "id"; //$NON-NLS-1$
        ArrayDefinition result = fixture.lookupArray(name);

        assertNull(result);
    }

    /**
     * Run the Definition lookupDefinition(String) method test.
     */
    @Test
    public void testLookupDefinition() {
        String lookupPath = "id"; //$NON-NLS-1$
        Definition result = fixture.lookupDefinition(lookupPath);

        assertNotNull(result);
    }

    /**
     * Run the EnumDefinition lookupEnum(String) method test.
     */
    @Test
    public void testLookupEnum() {
        String name = ""; //$NON-NLS-1$
        EnumDefinition result = fixture.lookupEnum(name);

        /* There are no enums in the test trace */
        assertNull(result);
    }

    /**
     * Run the IntegerDefinition lookupInteger(String) method test.
     */
    @Test
    public void testLookupInteger_1() {
        String name = "id"; //$NON-NLS-1$
        IntegerDefinition result = fixture.lookupInteger(name);

        assertNotNull(result);
    }

    /**
     * Run the IntegerDefinition lookupInteger(String) method test.
     */
    @Test
    public void testLookupInteger_2() {
        String name = VAR_FIELD_NAME;
        IntegerDefinition result = fixture.lookupInteger(name);

        assertNull(result);
    }

    /**
     * Run the SequenceDefinition lookupSequence(String) method test.
     */
    @Test
    public void testLookupSequence() {
        String name = VAR_FIELD_NAME;
        SequenceDefinition result = fixture.lookupSequence(name);

        assertNull(result);
    }

    /**
     * Run the StringDefinition lookupString(String) method test.
     */
    @Test
    public void testLookupString() {
        String name = VAR_FIELD_NAME;
        StringDefinition result = fixture.lookupString(name);

        assertNull(result);
    }

    /**
     * Run the StructDefinition lookupStruct(String) method test.
     */
    @Test
    public void testLookupStruct() {
        String name = VAR_FIELD_NAME;
        StructDefinition result = fixture.lookupStruct(name);

        assertNull(result);
    }

    /**
     * Run the VariantDefinition lookupVariant(String) method test.
     */
    @Test
    public void testLookupVariant() {
        String name = VAR_FIELD_NAME;
        VariantDefinition result = fixture.lookupVariant(name);

        assertNull(result);
    }

    /**
     * Run the void read(BitBuffer) method test.
     */
    @Test
    public void testRead_() {
        ByteBuffer bb = ByteBuffer.allocateDirect(128);
        bb.put((byte) 20);
        BitBuffer input = new BitBuffer(bb);

        fixture.read(input);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String result = fixture.toString();
        assertNotNull(result);
    }
}