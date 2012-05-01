package org.eclipse.linuxtools.ctf.core.tests.types;

import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * The class <code>TestAll</code> builds a suite that can be used to run all of
 * the tests within its package as well as within any subpackages of its
 * package.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ StructDefinitionTest.class, IntegerDeclarationTest.class,
        EnumDefinitionTest.class, SequenceDeclarationTest.class,
        StructDeclarationTest.class, DefinitionTest.class,
        IntegerDefinitionTest.class, SequenceDefinitionTest.class,
        ArrayDefinitionTest.class, EnumDeclarationTest.class,
        StringDeclarationTest.class, ArrayDeclarationTest.class,
        FloatDefinitionTest.class, FloatDeclarationTest.class,
        VariantDefinitionTest.class, VariantDeclarationTest.class,
        StringDefinitionTest.class, EventDeclarationTest.class, })
public class TestAll {

    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        JUnitCore.runClasses(new Class[] { TestAll.class });
    }
}
