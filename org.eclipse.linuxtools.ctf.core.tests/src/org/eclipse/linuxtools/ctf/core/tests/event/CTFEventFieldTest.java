package org.eclipse.linuxtools.ctf.core.tests.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.types.ArrayDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.ArrayDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.SequenceDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.SequenceDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StringDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StringDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.internal.ctf.core.event.io.BitBuffer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>CTFEventFieldTest</code> contains tests for the class
 * <code>{@link CTFEventField}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 * @param <CTFIntegerArrayField>
 */
public class CTFEventFieldTest {

    private static final String fieldName = "id"; //$NON-NLS-1$


    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        // add additional set up code here
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(CTFEventFieldTest.class);
    }

    /**
     * Run the CTFEventField parseField(Definition,String) method test.
     * @throws CTFReaderException 
     */
    @Test
    public void testParseField_complex() throws CTFReaderException {
        int len = 32;
        IntegerDeclaration id = new IntegerDeclaration(len, false, len,
                ByteOrder.LITTLE_ENDIAN, Encoding.ASCII, null);
        String lengthName = "LengthName"; //$NON-NLS-1$
        StructDeclaration structDec = new StructDeclaration(0);
        structDec.addField(lengthName, id);
        StructDefinition structDef = new StructDefinition(structDec, null,
                lengthName);

        structDef.lookupInteger(lengthName).setValue(32);
        SequenceDeclaration sd = new SequenceDeclaration(lengthName, id);
        Definition fieldDef = new SequenceDefinition(sd, structDef, "TestX"); //$NON-NLS-1$
        ByteBuffer byb = ByteBuffer.allocate(1024);
        for (int i = 0; i < 1024; i++) {
            byb.put((byte) i);
        }
        BitBuffer bb = new BitBuffer(byb);
        fieldDef.read(bb);

        assertNotNull(fieldDef);
    }

    /**
     * Run the CTFEventField parseField(Definition,String) method test.
     * @throws CTFReaderException 
     */
    @Test
    public void testParseField_simple() throws CTFReaderException {
        Definition fieldDef = new SequenceDefinition(new SequenceDeclaration(
                "", new StringDeclaration()), null, fieldName); //$NON-NLS-1$

        assertNotNull(fieldDef);
    }

    /**
     * Run the CTFEventField parseField(Definition,String) method test.
     */
    @Test
    public void testParseField_simple2() {
        IntegerDefinition fieldDef = new IntegerDefinition(
                new IntegerDeclaration(1, true, 1, ByteOrder.BIG_ENDIAN,
                        Encoding.ASCII, null), null, fieldName);
        fieldDef.setValue(1L);

        assertNotNull(fieldDef);
    }

    /**
     *
     */
    @Test
    public void testParseField_simple3() {
        StringDefinition fieldDef = new StringDefinition(
                new StringDeclaration(), null, fieldName);
        fieldDef.setString(new StringBuilder("Hello World")); //$NON-NLS-1$

        String other = fieldName + "=Hello World"; //$NON-NLS-1$
        assertNotNull(fieldDef);
        assertEquals(fieldDef.toString(), other);
    }

    /**
     * Run the CTFEventField parseField(Definition,String) method test.
     */
    @Test
    public void testParseField_manual() {
        Definition fieldDef = new ArrayDefinition(new ArrayDeclaration(20,
                new IntegerDeclaration(8, false, 8, null, Encoding.UTF8, null)),
                null, fieldName);
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[0]).setValue('H');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[1]).setValue('e');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[2]).setValue('l');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[3]).setValue('l');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[4]).setValue('o');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[5]).setValue(' ');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[6]).setValue('W');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[7]).setValue('o');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[8]).setValue('r');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[9]).setValue('l');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[10]).setValue('d');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[11]).setValue(0);

        assertNotNull(fieldDef);
    }

    /**
     * Run the CTFEventField parseField(Definition,String) method test.
     */
    @Test
    public void testParseField_manual2() {
        Definition fieldDef = new ArrayDefinition(new ArrayDeclaration(12,
                new IntegerDeclaration(32, false, 32, null, null, null)), null,
                fieldName);
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[0]).setValue('H');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[1]).setValue('e');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[2]).setValue('l');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[3]).setValue('l');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[4]).setValue('o');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[5]).setValue(' ');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[6]).setValue('W');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[7]).setValue('o');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[8]).setValue('r');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[9]).setValue('l');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[10]).setValue('d');
        ((IntegerDefinition) ((ArrayDefinition) fieldDef).getDefinitions()[11]).setValue(0);

        assertNotNull(fieldDef);
        String other = fieldName
                + "={ 72, 101, 108, 108, 111, 32, 87, 111, 114, 108, 100, 0}"; //$NON-NLS-1$
        assertEquals(other, fieldDef.toString());
    }
}