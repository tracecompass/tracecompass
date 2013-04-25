/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.eclipse.linuxtools.internal.ctf.core.Activator;
import org.junit.Test;

/**
 * <b><u>CtfCorePluginTest</u></b>
 * <p>
 * Test the CTF core plug-in activator
 */
@SuppressWarnings("javadoc")
public class CtfCorePluginTest {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // Plug-in instantiation
    private final static Activator fPlugin = Activator.getDefault();


    // ------------------------------------------------------------------------
    // Test cases
    // ------------------------------------------------------------------------

    @Test
    public void testCtfCorePluginId() {
        assertEquals(
                "Plugin ID", "org.eclipse.linuxtools.ctf", Activator.PLUGIN_ID);
    }

    @Test
    public void testGetDefault() {
        Activator plugin = Activator.getDefault();
        assertEquals("getDefault()", plugin, fPlugin);
    }

    @Test
    public void testLog() {
        try {
            Activator.log("Some message");
        } catch (Exception e) {
            fail();
        }
    }

}
