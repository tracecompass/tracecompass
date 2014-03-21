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

import static org.junit.Assert.assertNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StringDeclaration;
import org.junit.Test;

/**
 * The class <code>DefinitionTest</code> contains tests for the class
 * <code>{@link Definition}</code>.
 *
 * @author Matthew Khouzam
 * @version $Revision: 1.0 $
 */
public class DefinitionTest {

    /**
     * Since Definition is abstract, we'll minimally extend it here to
     * instantiate it.
     */
    static class DefTest extends Definition {

        @NonNull
        private static final StringDeclaration STRINGDEF = new StringDeclaration();

        public DefTest(IDefinitionScope definitionScope, @NonNull String fieldName) {
            super(DefTest.STRINGDEF, definitionScope, fieldName);
        }

        @Override
        @NonNull
        public IDeclaration getDeclaration() {
            return DefTest.STRINGDEF;
        }

    }

    /**
     * Test a definition
     */
    @Test
    public void testToString() {
        Definition fixture = new DefTest(null, "Hello");
        String result = fixture.toString();

        assertNotNull(result);
    }
}