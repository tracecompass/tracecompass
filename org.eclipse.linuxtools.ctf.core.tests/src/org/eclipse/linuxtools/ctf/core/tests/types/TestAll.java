package org.eclipse.linuxtools.ctf.core.tests.types;

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
@Suite.SuiteClasses({
    ArrayDeclarationTest.class,
    ArrayDefinitionTest.class,
    DefinitionTest.class,
    EnumDeclarationTest.class,
    EnumDefinitionTest.class,
    EventDeclarationTest.class,
    FloatDeclarationTest.class,
    FloatDefinitionTest.class,
    IntegerDeclarationTest.class,
    IntegerDefinitionTest.class,
    SequenceDeclarationTest.class,
    SequenceDefinitionTest.class,
    StringDeclarationTest.class,
    StringDefinitionTest.class,
    StructDeclarationTest.class,
    StructDefinitionTest.class,
    VariantDeclarationTest.class,
    VariantDefinitionTest.class,
})
public class TestAll {

}
