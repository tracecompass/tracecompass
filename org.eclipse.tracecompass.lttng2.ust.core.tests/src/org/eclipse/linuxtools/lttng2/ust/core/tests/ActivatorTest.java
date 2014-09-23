/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.ust.core.tests;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <b><u>ActivatorTest</u></b>
 * <p>
 * Test suite for the Activator class
 * <p>
 */
@SuppressWarnings("javadoc")
public class ActivatorTest extends TestCase {

    // ------------------------------------------------------------------------
    // JUnit
    // ------------------------------------------------------------------------

    @BeforeClass
    public static void setUpBeforeClass() {
    }

    @AfterClass
    public static void tearDownAfterClass() {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    @Override
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    @Override
    public void tearDown() throws Exception {
    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    /**
     * Test method for {@link org.eclipse.linuxtools.lttng2.kernel.core.Activator#Activator()}.
     */
    @Test
    public void testActivator() {
        assertTrue(true);
    }

    /**
     * Test method for {@link org.eclipse.linuxtools.lttng2.kernel.core.Activator#getDefault()}.
     */
    @Test
    public void testGetDefault() {
        assertTrue(true);
    }

    /**
     * Test method for {@link org.eclipse.linuxtools.lttng2.kernel.core.Activator#start(org.osgi.framework.BundleContext)}.
     */
    @Test
    public void testStartBundleContext() {
        assertTrue(true);
    }

    /**
     * Test method for {@link org.eclipse.linuxtools.lttng2.kernel.core.Activator#stop(org.osgi.framework.BundleContext)}.
     */
    @Test
    public void testStopBundleContext() {
        assertTrue(true);
    }

}
