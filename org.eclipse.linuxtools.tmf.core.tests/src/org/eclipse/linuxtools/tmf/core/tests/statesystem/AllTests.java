/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.statesystem;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.linuxtools.internal.tmf.core.Activator;

/**
 * Test suite for org.eclipse.linuxtools.tmf.core.statesystem
 */
public class AllTests {

    /**
     * @return The state system test suite
     */
    public static Test suite() {
        final TestSuite suite = new TestSuite("Test suite for " + Activator.PLUGIN_ID + ".statesystem"); //$NON-NLS-1$ //$NON-NLS-2$;
        //$JUnit-BEGIN$
        suite.addTestSuite(StateSystemPushPopTest.class);
        //$JUnit-END$
        return suite;
    }
}