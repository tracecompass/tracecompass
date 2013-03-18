/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.junit.Test;

/**
 * Test the TMF core plug-in activator
 */
public class TmfCorePluginTest {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // Plug-in instantiation
    static final Activator fPlugin = new Activator();

    // ------------------------------------------------------------------------
    // Test cases
    // ------------------------------------------------------------------------

    /**
     * Test the plugin ID.
     */
    @Test
    public void testTmfCorePluginId() {
        assertEquals("Plugin ID", "org.eclipse.linuxtools.tmf.core", Activator.PLUGIN_ID);
    }

    /**
     * Test the getDefault() static method.
     */
    @Test
    public void testGetDefault() {
        Activator plugin = Activator.getDefault();
        assertEquals("getDefault()", plugin, fPlugin);
    }

}
