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

package org.eclipse.tracecompass.gdbtrace.ui.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.tracecompass.internal.gdbtrace.ui.GdbTraceUIPlugin;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class GdbTraceUIPluginTest {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // Plug-in instantiation
    private final static Plugin fPlugin = GdbTraceUIPlugin.getDefault();

    // ------------------------------------------------------------------------
    // Test cases
    // ------------------------------------------------------------------------

    @Test
    public void testPluginId() {
        assertEquals("Plugin ID", "org.eclipse.tracecompass.gdbtrace.ui", GdbTraceUIPlugin.PLUGIN_ID);
    }

    @Test
    public void testGetDefault() {
        Plugin plugin = GdbTraceUIPlugin.getDefault();
        assertEquals("getDefault()", plugin, fPlugin);
    }
}
