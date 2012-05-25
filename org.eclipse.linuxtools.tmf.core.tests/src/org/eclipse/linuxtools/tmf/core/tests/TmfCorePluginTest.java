/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests;

import junit.framework.TestCase;

import org.eclipse.linuxtools.internal.tmf.core.Activator;

/**
 * <b><u>TmfCorePluginTest</u></b>
 * <p>
 * Test the TMF core plug-in activator
 */
@SuppressWarnings({ "nls", "restriction" })
public class TmfCorePluginTest extends TestCase {

	// ------------------------------------------------------------------------
    // Attributes
	// ------------------------------------------------------------------------

	// Plug-in instantiation
	static final Activator fPlugin = new Activator();
	
	// ------------------------------------------------------------------------
    // Housekeping
	// ------------------------------------------------------------------------

	/**
	 * @param name the test name
	 */
	public TmfCorePluginTest(String name) {
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

	public void testTmfCorePluginId() throws Exception {
		assertEquals("Plugin ID", "org.eclipse.linuxtools.tmf.core", Activator.PLUGIN_ID);
	}

	public void testGetDefault() throws Exception {
		Activator plugin = Activator.getDefault();
		assertEquals("getDefault()", plugin, fPlugin);
	}

}
