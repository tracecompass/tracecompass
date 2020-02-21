/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
