package org.eclipse.linuxtools.ctf.core.tests.types;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.nio.ByteOrder;
import java.util.HashMap;

import org.eclipse.linuxtools.ctf.core.event.types.ArrayDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.SequenceDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StringDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.VariantDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.VariantDefinition;
import org.eclipse.linuxtools.ctf.core.tests.TestParams;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>VariantDefinitionTest</code> contains tests for the class
 * <code>{@link VariantDefinition}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class VariantDefinitionTest {

    private VariantDefinition fixture;

    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(VariantDefinitionTest.class);
    }

    /**
     * Perform pre-test initialization.
     *
     * Not sure it needs to be that complicated, oh well...
     *
     * @throws CTFReaderException
     */
    @Before
    public void setUp() throws CTFReaderException {
        VariantDeclaration vDecl1, vDecl2, vDecl3;
        VariantDefinition vDef1, vDef2;
        StructDefinition sDef1, sDef2;
        EnumDefinition eDef;
        String fName = ""; //$NON-NLS-1$

        vDecl1 = new VariantDeclaration();
        vDecl2 = new VariantDeclaration();
        vDecl3 = new VariantDeclaration();
        vDecl1.setTag(fName);
        vDecl2.setTag(fName);
        vDecl3.setTag(fName);

        vDef1 = new VariantDefinition(vDecl2, TestParams.createTrace(), fName);
        vDef2 = new VariantDefinition(vDecl3, TestParams.createTrace(), fName);

        sDef1 = new StructDefinition(new StructDeclaration(1L), vDef1, fName);
        sDef2 = new StructDefinition(new StructDeclaration(1L), vDef2, fName);

        eDef = new EnumDefinition(new EnumDeclaration(new IntegerDeclaration(1,
                true, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, fName, 8)), sDef2, fName);

        fixture = new VariantDefinition(vDecl1, sDef1, fName);
        fixture.setTagDefinition(eDef);
        fixture.setCurrentField(fName);
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the VariantDefinition(VariantDeclaration,DefinitionScope,String)
     *
     * @throws CTFReaderException
     */
    @Test
    public void testVariantDefinition() throws CTFReaderException {
        VariantDeclaration declaration = new VariantDeclaration();
        declaration.setTag(""); //$NON-NLS-1$
        VariantDeclaration variantDeclaration = new VariantDeclaration();
        variantDeclaration.setTag(""); //$NON-NLS-1$
        VariantDefinition variantDefinition = new VariantDefinition(
                variantDeclaration, TestParams.createTrace(), ""); //$NON-NLS-1$
        IDefinitionScope definitionScope = new StructDefinition(
                new StructDeclaration(1L), variantDefinition, ""); //$NON-NLS-1$
        String fieldName = ""; //$NON-NLS-1$

        VariantDefinition result = new VariantDefinition(declaration,
                definitionScope, fieldName);
        assertNotNull(result);
    }

    /**
     * Run the Definition getCurrentField() method test.
     */
    @Test
    public void testGetCurrentField() {
        Definition result = fixture.getCurrentField();
        assertNull(result);
    }

    /**
     * Run the String getCurrentFieldName() method test.
     */
    @Test
    public void testGetCurrentFieldName() {
        String result = fixture.getCurrentFieldName();
        assertNotNull(result);
    }

    /**
     * Run the VariantDeclaration getDeclaration() method test.
     */
    @Test
    public void testGetDeclaration() {
        VariantDeclaration result = fixture.getDeclaration();
        assertNotNull(result);
    }

    /**
     * Run the HashMap<String, Definition> getDefinitions() method test.
     */
    @Test
    public void testGetDefinitions() {
        HashMap<String, Definition> result = fixture.getDefinitions();
        assertNotNull(result);
    }

    /**
     * Run the String getPath() method test.
     */
    @Test
    public void testGetPath() {
        String result = fixture.getPath();
        assertNotNull(result);
    }

    /**
     * Run the EnumDefinition getTagDefinition() method test.
     */
    @Test
    public void testGetTagDefinition() {
        EnumDefinition result = fixture.getTagDefinition();
        assertNotNull(result);
    }

    /**
     * Run the ArrayDefinition lookupArray(String) method test.
     */
    @Test
    public void testLookupArray() {
        String name = ""; //$NON-NLS-1$
        ArrayDefinition result = fixture.lookupArray(name);
        assertNull(result);
    }

    /**
     * Run the Definition lookupDefinition(String) method test.
     */
    @Test
    public void testLookupDefinition() {
        String lookupPath = ""; //$NON-NLS-1$
        Definition result = fixture.lookupDefinition(lookupPath);
        assertNull(result);
    }

    /**
     * Run the EnumDefinition lookupEnum(String) method test.
     */
    @Test
    public void testLookupEnum() {
        String name = ""; //$NON-NLS-1$
        EnumDefinition result = fixture.lookupEnum(name);
        assertNull(result);
    }

    /**
     * Run the IntegerDefinition lookupInteger(String) method test.
     */
    @Test
    public void testLookupInteger() {
        String name = ""; //$NON-NLS-1$
        IntegerDefinition result = fixture.lookupInteger(name);
        assertNull(result);
    }

    /**
     * Run the SequenceDefinition lookupSequence(String) method test.
     */
    @Test
    public void testLookupSequence_1() {
        String name = ""; //$NON-NLS-1$
        SequenceDefinition result = fixture.lookupSequence(name);
        assertNull(result);
    }

    /**
     * Run the StringDefinition lookupString(String) method test.
     */
    @Test
    public void testLookupString() {
        String name = ""; //$NON-NLS-1$
        StringDefinition result = fixture.lookupString(name);
        assertNull(result);
    }

    /**
     * Run the StructDefinition lookupStruct(String) method test.
     */
    @Test
    public void testLookupStruct() {
        String name = ""; //$NON-NLS-1$
        StructDefinition result = fixture.lookupStruct(name);
        assertNull(result);
    }

    /**
     * Run the VariantDefinition lookupVariant(String) method test.
     */
    @Test
    public void testLookupVariant() {
        String name = ""; //$NON-NLS-1$
        VariantDefinition result = fixture.lookupVariant(name);
        assertNull(result);
    }

    /**
     * Run the void setCurrentField(String) method test.
     */
    @Test
    public void testSetCurrentField() {
        String currentField = ""; //$NON-NLS-1$
        fixture.setCurrentField(currentField);
    }

    /**
     * Run the void setDeclaration(VariantDeclaration) method test.
     */
    @Test
    public void testSetDeclaration() {
        VariantDeclaration declaration = new VariantDeclaration();
        fixture.setDeclaration(declaration);
    }

    /**
     * Run the void setDefinitions(HashMap<String,Definition>) method test.
     */
    @Test
    public void testSetDefinitions() {
        HashMap<String, Definition> definitions = new HashMap<String, Definition>();
        fixture.setDefinitions(definitions);
    }

    /**
     * Run the void setTagDefinition(EnumDefinition) method test.
     *
     * @throws CTFReaderException
     */
    @Test
    public void testSetTagDefinition() throws CTFReaderException {
        VariantDeclaration vDecl;
        VariantDefinition vDef;
        StructDefinition structDef;
        EnumDefinition tagDefinition;
        String fName = ""; //$NON-NLS-1$

        vDecl = new VariantDeclaration();
        vDecl.setTag(fName);
        vDef = new VariantDefinition(vDecl, TestParams.createTrace(), fName);
        structDef = new StructDefinition(new StructDeclaration(1L), vDef, fName);
        tagDefinition = new EnumDefinition(new EnumDeclaration(
                new IntegerDeclaration(1, true, 1, ByteOrder.BIG_ENDIAN,
                        Encoding.ASCII, fName, 8)), structDef, fName);

        fixture.setTagDefinition(tagDefinition);
    }
}
