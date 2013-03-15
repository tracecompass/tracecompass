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

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinitionScope;
import org.junit.Test;

/**
 * The class <code>DefinitionTest</code> contains tests for the class
 * <code>{@link Definition}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
public class DefinitionTest {

    /**
     * Since Definition is abstract, we'll minimally extend it here to
     * instantiate it.
     */
    class DefTest extends Definition {

        public DefTest(IDefinitionScope definitionScope, String fieldName) {
            super(definitionScope, fieldName);
        }

        @Override
        public void read(BitBuffer input) {
            /* Just a test, no need to implement anything */
        }

        @Override
        public IDeclaration getDeclaration() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    @Test
    public void testToString() {
        Definition fixture = new DefTest(null, "Hello");
        String result = fixture.toString();

        assertNotNull(result);
    }
}