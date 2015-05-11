/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.gdbtrace.core.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.tracecompass.internal.gdbtrace.core.GdbTraceCorePlugin;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class GdbTraceCorePluginTest {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // Plug-in instantiation
    private final static Plugin fPlugin = GdbTraceCorePlugin.getDefault();

    // ------------------------------------------------------------------------
    // Test cases
    // ------------------------------------------------------------------------

    @Test
    public void testPluginId() {
        assertEquals("Plugin ID", "org.eclipse.tracecompass.gdbtrace.core", GdbTraceCorePlugin.PLUGIN_ID);
    }

    @Test
    public void testGetDefault() {
        Plugin plugin = GdbTraceCorePlugin.getDefault();
        assertEquals("getDefault()", plugin, fPlugin);
    }
}
