/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.scope;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.linuxtools.ctf.core.event.scope.LexicalScope;
import org.junit.Test;

/**
 * Lexical test
 *
 * @author Matthew Khouzam
 */
public class LexicalScopeTest {

    /**
     * Root test
     */
    @Test
    public void testRoot(){
        LexicalScope scope = LexicalScope.ROOT;
        assertNotNull(scope);
    }

    /**
     * Test a more complex node
     */
    @Test
    public void testComplexNode(){
        LexicalScope scope = LexicalScope.STREAM_EVENT_CONTEXT;
        assertEquals("context", scope.getName());
        assertEquals("stream.event.context", scope.toString());
    }
}
