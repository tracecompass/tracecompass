package org.eclipse.linuxtools.ctf.core.tests.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

import org.eclipse.linuxtools.ctf.core.event.types.IDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.StringDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.VariantDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.VariantDefinition;
import org.eclipse.linuxtools.ctf.core.tests.shared.CtfTestTraces;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>VariantDeclarationTest</code> contains tests for the class
 * <code>{@link VariantDeclaration}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class VariantDeclarationTest {

    private static final int TRACE_INDEX = 0;

    private VariantDeclaration fixture;

    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(VariantDeclarationTest.class);
    }

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new VariantDeclaration();
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the VariantDeclaration() constructor test.
     */
    @Test
    public void testVariantDeclaration() {
        assertNotNull(fixture);
        assertEquals(false, fixture.isTagged());
        String left = "[declaration] variant["; //$NON-NLS-1$
        assertEquals(left, fixture.toString().substring(0, left.length()));
    }

    /**
     * Run the void addField(String,Declaration) method test.
     */
    @Test
    public void testAddField() {
        fixture.setTag(""); //$NON-NLS-1$
        String tag = ""; //$NON-NLS-1$
        IDeclaration declaration = new StringDeclaration();
        fixture.addField(tag, declaration);
    }

    /**
     * Run the VariantDefinition createDefinition(DefinitionScope,String) method
     * test.
     *
     * @throws CTFReaderException Should not happen
     */
    @Test
    public void testCreateDefinition() throws CTFReaderException {
        fixture.setTag(""); //$NON-NLS-1$
        IDefinitionScope definitionScope = createDefinitionScope();
        String fieldName = ""; //$NON-NLS-1$
        VariantDefinition result = fixture.createDefinition(definitionScope,
                fieldName);

        assertNotNull(result);
    }

    private static IDefinitionScope createDefinitionScope() throws CTFReaderException {
        assumeTrue(CtfTestTraces.tracesExist());
        VariantDeclaration declaration = new VariantDeclaration();
        declaration.setTag(""); //$NON-NLS-1$
        VariantDeclaration variantDeclaration = new VariantDeclaration();
        variantDeclaration.setTag(""); //$NON-NLS-1$
        VariantDefinition variantDefinition = new VariantDefinition(
                variantDeclaration, CtfTestTraces.getTestTrace(TRACE_INDEX), ""); //$NON-NLS-1$
        IDefinitionScope definitionScope = new StructDefinition(
                new StructDeclaration(1L), variantDefinition, ""); //$NON-NLS-1$
        String fieldName = ""; //$NON-NLS-1$

        VariantDefinition result = new VariantDefinition(declaration,
                definitionScope, fieldName);
        return result;
    }

    /**
     * Run the boolean hasField(String) method test.
     */
    @Test
    public void testHasField() {
        fixture.setTag(""); //$NON-NLS-1$
        String tag = ""; //$NON-NLS-1$
        boolean result = fixture.hasField(tag);

        assertEquals(false, result);
    }

    /**
     * Run the boolean isTagged() method test.
     */
    @Test
    public void testIsTagged() {
        fixture.setTag(""); //$NON-NLS-1$
        boolean result = fixture.isTagged();

        assertEquals(true, result);
    }

    /**
     * Run the boolean isTagged() method test.
     */
    @Test
    public void testIsTagged_null() {
        fixture.setTag((String) null);
        boolean result = fixture.isTagged();

        assertEquals(false, result);
    }

    /**
     * Run the void setTag(String) method test.
     */
    @Test
    public void testSetTag() {
        fixture.setTag(""); //$NON-NLS-1$
        String tag = ""; //$NON-NLS-1$
        fixture.setTag(tag);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        fixture.setTag(""); //$NON-NLS-1$
        String result = fixture.toString();
        String left = "[declaration] variant["; //$NON-NLS-1$
        String right = result.substring(0, left.length());

        assertEquals(left, right);
    }
}