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

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfContent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>CtfTmfContentTest</code> contains tests for the class
 * <code>{@link CtfTmfContent}</code>.
 *
 * @author ematkho
 * @version 1.0
 */
public class CtfTmfContentTest {

    private CtfTmfContent fixture;

    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(CtfTmfContentTest.class);
    }

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new CtfTmfContent("", new ITmfEventField[] {}); //$NON-NLS-1$
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }


    /**
     * Run the CtfTmfContent(String,ITmfEventField[]) constructor test.
     */
    @Test
    public void testCtfTmfContent() {
        String name = ""; //$NON-NLS-1$
        ITmfEventField[] fields = new ITmfEventField[] {};
        CtfTmfContent result = new CtfTmfContent(name, fields);

        assertNotNull(result);
        assertEquals("", result.toString()); //$NON-NLS-1$
        assertEquals("", result.getName()); //$NON-NLS-1$
        assertEquals(null, result.getValue());
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String result = fixture.toString();
        assertEquals("", result); //$NON-NLS-1$
    }
}
