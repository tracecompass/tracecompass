/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial generation with CodePro tools
 *   Alexandre Montplaisir - Clean up, consolidate redundant tests
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEventType;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>CtfTmfEventTypeTest</code> contains tests for the class
 * <code>{@link CtfTmfEventType}</code>.
 *
 * @author ematkho
 * @version 1.0
 */
public class CtfTmfEventTypeTest {

    /**
     * Launch the test.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(CtfTmfEventTypeTest.class);
    }

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
     * Run the CtfTmfEventType(String,String,ITmfEventField) constructor test.
     */
    @Test
    public void testCtfTmfEventType() {
        String eventName = ""; //$NON-NLS-1$
        ITmfEventField content = new TmfEventField("", new ITmfEventField[] {}); //$NON-NLS-1$
        CtfTmfEventType result = new CtfTmfEventType( eventName, content);

        assertNotNull(result);
        assertEquals("", result.toString()); //$NON-NLS-1$
        assertEquals("", result.getName()); //$NON-NLS-1$
        assertEquals("Ctf Event", result.getContext()); //$NON-NLS-1$
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        ITmfEventField emptyField = new TmfEventField("", new ITmfEventField[] {}); //$NON-NLS-1$
        CtfTmfEventType fixture = new CtfTmfEventType("", emptyField); //$NON-NLS-1$

        String result = fixture.toString();

        assertEquals("", result); //$NON-NLS-1$
    }
}