/*******************************************************************************
 * Copyright (c) 2011, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests;

import junit.framework.TestCase;

import org.eclipse.linuxtools.internal.ctf.core.Activator;

/**
 * <b><u>CtfCorePluginTest</u></b>
 * <p>
 * Test the CTF core plug-in activator
 */
public class CtfCorePluginTest extends TestCase {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // Plug-in instantiation
    static final Activator fPlugin = Activator.getDefault();

    // ------------------------------------------------------------------------
    // Housekeping
    // ------------------------------------------------------------------------

    /**
     * @param name
     *            the test name
     */
    public CtfCorePluginTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // ------------------------------------------------------------------------
    // Test cases
    // ------------------------------------------------------------------------

    public static void testCtfCorePluginId() {
        assertEquals(
                "Plugin ID", "org.eclipse.linuxtools.ctf", Activator.PLUGIN_ID); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static void testGetDefault() {
        Activator plugin = Activator.getDefault();
        assertEquals("getDefault()", plugin, fPlugin); //$NON-NLS-1$
    }

    public static void testLog() {
        try {
            Activator.getDefault().log("Some message"); //$NON-NLS-1$
        } catch (Exception e) {
            fail();
        }
    }

}
