/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

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
    ArrayDeclaration2Test.class,
    ArrayDefinition2Test.class,
    DefinitionTest.class,
    EnumDeclarationTest.class,
    EnumDefinitionTest.class,
    EventDeclarationTest.class,
    EventHeaderDeclarationTest.class,
    FloatDeclarationTest.class,
    FloatDefinitionTest.class,
    IntegerDeclarationTest.class,
    IntegerDefinitionTest.class,
    IntegerEndiannessTest.class,
    SequenceDeclaration2Test.class,
    SequenceDefinition2Test.class,
    StringDeclarationTest.class,
    StringDefinitionTest.class,
    StructDeclarationTest.class,
    StructDefinitionTest.class,
    VariantDeclarationTest.class,
    VariantDefinitionTest.class,
})
public class TestAll {

}
