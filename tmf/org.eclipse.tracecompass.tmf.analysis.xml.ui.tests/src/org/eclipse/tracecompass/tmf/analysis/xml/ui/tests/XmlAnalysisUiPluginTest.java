/*******************************************************************************
 * Copyright (c) 2013, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.ui.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.Activator;
import org.junit.Test;

/**
 * Test the XML Analysis UI plug-in activator
 *
 * @author Geneviève Bastien
 */
public class XmlAnalysisUiPluginTest {
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
        assertEquals("Plugin ID", "org.eclipse.tracecompass.tmf.analysis.xml.ui", Activator.PLUGIN_ID);
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
